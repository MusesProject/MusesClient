/*
 * MUSES High-Level Object Oriented Model
 * Copyright MUSES project (European Commission FP7) - 2013
 */
package eu.musesproject.client.usercontexteventhandler;

/*
 * #%L
 * musesclient
 * %%
 * Copyright (C) 2013 - 2014 HITEC
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms stoof the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import eu.musesproject.client.R;
import eu.musesproject.client.actuators.ActuatorController;
import eu.musesproject.client.connectionmanager.*;
import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;
import eu.musesproject.client.contextmonitoring.sensors.SettingsSensor;
import eu.musesproject.client.db.entity.ActionProperty;
import eu.musesproject.client.db.entity.Configuration;
import eu.musesproject.client.db.entity.Property;
import eu.musesproject.client.db.entity.SensorConfiguration;
import eu.musesproject.client.db.handler.DBManager;
import eu.musesproject.client.db.handler.ResourceCreator;
import eu.musesproject.client.decisionmaker.DecisionMaker;
import eu.musesproject.client.model.RequestType;
import eu.musesproject.client.model.actuators.ActuationInformationHolder;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.Decision;
import eu.musesproject.client.model.decisiontable.Request;
import eu.musesproject.client.model.decisiontable.Resource;
import eu.musesproject.client.securitypolicyreceiver.RemotePolicyReceiver;
import eu.musesproject.client.ui.MainActivity;
import eu.musesproject.client.ui.NotificationController;
import eu.musesproject.client.utils.MusesUtils;
import eu.musesproject.contextmodel.ContextEvent;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class UserContextEventHandler. Singleton
 *
 * @author Christoph
 * @version 28 feb 2014
 */
public class UserContextEventHandler implements RequestTimeoutTimer.RequestTimeoutHandler {
	private static final String TAG = UserContextEventHandler.class.getSimpleName();
	public static final String TAG_RQT = "REQUEST_TIMEOUT";
	public static final String TAG_RQT2 = "REQUEST_TIMEOUT2";
	public static final String TAG_DB = "DATABASE_TEST_CODE";
	public static final String APP_TAG = "APP_TAG";

	private static UserContextEventHandler userContextEventHandler = null;
	public static final String PREF_KEY_USER_AUTHENTICATED = "PREF_KEY_USER_AUTHENTICATED";
	public static final String PREF_KEY_USER_AUTHENTICATED_REMOTELY = "PREF_KEY_USER_AUTHENTICATED_REMOTELY";

	private Context context;

	private SharedPreferences prefs;

	// connection fields
	private ConnectionManager connectionManager;
	private IConnectionCallbacks connectionCallback;
	public int serverStatus;
	private int serverDetailedStatus;

    private boolean isAuthenticatedRemotely;
    private boolean isUserAuthenticated;

	private DecisionMaker decisionMaker;

	private static Map<Integer, RequestHolder> mapOfPendingRequests;//String key is the hashID of the request object
	private static Map<Integer, JSONObject> pendingJSONRequest;// to be able to handle 'data send failed'... so we can resend data
	private static Map<Integer, JSONObject> failedJSONRequest;// to be able to handle 'data send failed'... so we can resend data

	private String imei;
	private String userName;
	private String tmpLoginUserName;
	private String tmpLoginPassword;
	private DBManager dbManager;

	private UserContextEventHandler() {
		connectionManager = new ConnectionManager();
		connectionCallback = new ConnectionCallback();

		serverStatus = Statuses.CURRENT_STATUS;
		serverDetailedStatus = Statuses.OFFLINE;
		isAuthenticatedRemotely = false;
		isUserAuthenticated = false;

		decisionMaker = new DecisionMaker();

		mapOfPendingRequests = new HashMap<Integer, RequestHolder>();
        pendingJSONRequest = new HashMap<Integer, JSONObject>();
        failedJSONRequest = new HashMap<Integer, JSONObject>();
	}

	/**
	 * Method to get the current server status (online, offline)
	 *
	 * @return int. {@link Statuses} online: 1; offline:0
	 */
	public int getServerStatus() {
		return serverStatus;
	}

	public void setServerStatus(int serverStatus) {
		this.serverStatus = serverStatus;

		// update the notification icon
		NotificationController.getInstance(context).updateOnlineStatus();
	}

	public static UserContextEventHandler getInstance() {
		if (userContextEventHandler == null) {
            userContextEventHandler = new UserContextEventHandler();
		}
		return userContextEventHandler;
	}

	/**
	 * connects to the MUSES server
	 */
	public void connectToServer() {
		Configuration config = getServerConfigurationFromDB();
		String url = "https://" + config.getServerIP() + ":" + config.getServerPort() + config.getServerContextPath() + config.getServerServletPath();

		connectionManager.connect(
				url,
				config.getServerCertificate(),
				config.getPollTimeout(),
				config.getSleepPollTimeout(),
				connectionCallback,
				context
		);
		/* No need to do now, but shall be done if it changes.. */
		connectionManager.setPollTimeOuts(config.getPollTimeout(), config.getSleepPollTimeout());
	}

	private Configuration getServerConfigurationFromDB() {
		if(dbManager == null) {
			dbManager = new DBManager(context);
		}

		dbManager.openDB();
		Configuration config = dbManager.getConfigurations();
		/* Check if config is available */
		if (config.getServerIP() == null) {
			/* DB Connection config is empty, set initial config */
			dbManager.insertConnectionProperties();
		}
		config = dbManager.getConfigurations();
		dbManager.closeDB();
		return config;
	}

	/**
	 *  Method to first check the local decision maker for a decision to the corresponding
	 *      {@link eu.musesproject.client.contextmonitoring.service.aidl.Action}
	 *
	 *  Sends request to the server if there is no local stored decision /
	 *      perform default procedure if the server is not reachable
	 *
	 * @param action {@link Action}
	 * @param properties {@link Map}<String, String>
	 * @param contextEvents {@link ContextEvent}
	 */
	public void send(Action action, Map<String, String> properties, List<ContextEvent> contextEvents) {
		Log.d(MusesUtils.TEST_TAG, "UCEH - send(action, prop, context_events)");
		Log.d(APP_TAG, "UCEH - send(action, prop, context_events) with actionType=>" + action.getActionType());
		Log.d(TAG, "called: send(Action action, Map<String, String> properties, List<ContextEvent> contextEvents)");

		Decision decision = retrieveDecision(action, properties, contextEvents);

		if(decision != null) { // local decision found
			Log.d(APP_TAG, "Info DC, Local decision found => " + decision.getName() +", now calling actuator to showFeedback");
			Log.d(TAG_RQT, "Showing feedback for action: "+action.getActionType());
			ActuatorController.getInstance(context).showFeedback(new ActuationInformationHolder(decision, action, properties));
            if(action.isRequestedByMusesAwareApp() && action.isMusesAwareAppRequiresResponse()) {
			    ActuatorController.getInstance(context).sendFeedbackToMUSESAwareApp(decision);
            }
		}
		else { // if there is no local decision, send a request to the server
			if(serverStatus == Statuses.ONLINE && isUserAuthenticated) { // if the server is online, request a decision
				// temporary store the information so that the decision can be made after the server responded with
				// an database update (new policies are sent from the server to the client and stored in the database)
				// In addition, add a timeout to every request
				final RequestHolder requestHolder = new RequestHolder(action, properties, contextEvents);
				Log.d(TAG_RQT, "1. send: request_id: " + requestHolder.getId());
				Handler handler = new Handler(Looper.getMainLooper());
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						requestHolder.setRequestTimeoutTimer(new RequestTimeoutTimer(UserContextEventHandler.this, requestHolder.getId()));
						requestHolder.getRequestTimeoutTimer().start();
					}
				}, 0 );
				mapOfPendingRequests.put(requestHolder.getId(), requestHolder);
				Log.d(TAG_RQT2, "Adding action: "+ requestHolder.getAction().getActionType()+ " current size is: "+ mapOfPendingRequests.size());
				// create the JSON request and send it to the server
				JSONObject requestObject = JSONManager.createJSON(getImei(), getUserName(), requestHolder.getId(), RequestType.ONLINE_DECISION, action, properties, contextEvents);
				Log.d(APP_TAG, "Info DC, No Local decision found, Server is ONLINE, sending user data JSON(actions,properties,contextevnts) to server");
				sendRequestToServer(requestObject);
			}
			else if(serverStatus == Statuses.ONLINE && !isUserAuthenticated) {
				storeContextEvent(action, properties, contextEvents);
			}
			else if(serverStatus == Statuses.OFFLINE && isUserAuthenticated) {
                // TODO do not show the default policies at this point
//				ActuatorController.getInstance(context).showFeedback(new DecisionMaker().getDefaultDecision());
				storeContextEvent(action, properties, contextEvents);
			}
			else if(serverStatus == Statuses.OFFLINE && !isUserAuthenticated) {
				storeContextEvent(action, properties, contextEvents);
			}
		}
	}

	/**
	 * Method that handles the necessary steps to retrieve a decision from the decision maker
	 * 1. Generate the {@link Resource}
	 * 2. Generate the {@link Request}
	 * 3. Make sure that the {@link DecisionMaker} is initialised
	 * 4. Request a decision from the {@link DecisionMaker}
	 *
	 * @param action
	 * @param properties
	 * @param contextEvents
	 * @return
	 */
	private Decision retrieveDecision(Action action, Map<String, String> properties, List<ContextEvent> contextEvents) {
		if(action == null || properties == null || contextEvents == null) {
			return null;
		}
		Resource resource = ResourceCreator.create(action, properties);
		Request request = new Request(action, resource);
		if(decisionMaker == null) {
			decisionMaker = new DecisionMaker();
		}
		return decisionMaker.makeDecision(request, contextEvents, properties);
	}

	/**
	 * Method that takes an {@link eu.musesproject.client.model.decisiontable.Action}
	 * which contains the decision taken by the user on the MUSES UI.
	 * This behavior will be send to the server
	 *
	 * @param action
	 */
	public void sendUserBehavior(Action action, int decisionId) {
        // display next Feedback dialog if there is any
        ActuatorController.getInstance(context).removeFeedbackFromQueue();

        // send the current user feedback to the server
		if(serverStatus == Statuses.ONLINE && isUserAuthenticated) {
			Log.d(MusesUtils.TEST_TAG, "UCEH - sendUserBehavior(Action action)");
			Log.d(APP_TAG, "Info U, sending user behavior to server with action:"+action.getActionType());
			JSONObject userBehaviorJSON = JSONManager.createUserBehaviorJSON(getImei(), getUserName(), action.getActionType(), decisionId);
			sendRequestToServer(userBehaviorJSON);
		}
		else {
			// TODO store it offline
		}
	}

	/**
	 * Method to log in to MUSES.
	 * Necessary to establish server communication and for
	 * using this application
	 *
	 * @param userName
	 * @param password
	 */
	public void login(String userName, String password) {
		Log.d(MusesUtils.TEST_TAG, "UCEH - login(String userName, String password)");
		Log.d(TAG, "called: login(String userName, String password)");
		this.userName = userName;
		tmpLoginUserName = userName;
		tmpLoginPassword = password;

		if(dbManager == null) {
			dbManager = new DBManager(context);
		}

		String deviceId;
		dbManager.openDB();
		if((deviceId = dbManager.getDevId()) == null || deviceId.isEmpty()) {
			deviceId = getImei();
		}
		dbManager.closeDB();

		if(serverStatus == Statuses.ONLINE) {
			Log.d(APP_TAG, "Info U, Authenticating user login to server with username: "+tmpLoginUserName+" password: "+tmpLoginPassword + " deviceId: " +deviceId);
			JSONObject requestObject = JSONManager.createLoginJSON(tmpLoginUserName, tmpLoginPassword, deviceId);
			sendRequestToServer(requestObject);
		}
		else {
			Log.d(APP_TAG, "Info U, Authenticating login in localdatabase with username:"+tmpLoginUserName+" password:"+tmpLoginPassword + " deviceId: " + deviceId);
			dbManager.openDB();
			isUserAuthenticated = dbManager.isUserAuthenticated(getImei(), tmpLoginUserName, tmpLoginPassword);
			dbManager.closeDB();
			ActuatorController.getInstance(context).sendLoginResponse(isUserAuthenticated, context.getString(R.string.default_msg_local_login), -1);
			if (isUserAuthenticated){
				sendConfigSyncRequest();
			}
		}

		// start monitoring
		manageMonitoringComponent();
	}

	/**
	 * Method to try to login with existing credentials in the database
	 */
	public void autoLogin() {
		Log.d(MusesUtils.LOGIN_TAG, "UCEH - autoLogin()");
		if(prefs == null) {
			prefs = context.getSharedPreferences(MainActivity.PREFERENCES_KEY,
					Context.MODE_PRIVATE);
		}
		this.userName = prefs.getString(MainActivity.USERNAME, "");
		String password = prefs.getString(MainActivity.PASSWORD, "");

		if(userName.isEmpty() || password.isEmpty()) {
			Log.d("auto_login_test", "cannot auto login");
			Log.d(MusesUtils.LOGIN_TAG, "cannot auto login");
			return; // user wasn't logged in before
		}

        // try to log in locally
		dbManager.openDB();
		isUserAuthenticated = dbManager.isUserAuthenticated(getImei(), userName, password);
		boolean sensorConfigExists = dbManager.hasSensorConfig();
		dbManager.closeDB();

        //try to log in remotely
        if(serverStatus == Statuses.ONLINE) {
        	tmpLoginUserName = userName;
        	tmpLoginPassword = password;
        	JSONObject requestObject = JSONManager.createLoginJSON(userName, password, getImei());
            sendRequestToServer(requestObject);
        }

		updateServerOnlineAndUserAuthenticated();

        // decide, whether to start the context monitoring or to request for a proper configuration
		if(sensorConfigExists) {
			manageMonitoringComponent();
		}
		else {
			sendConfigSyncRequest();
		}
	}

	public void manageMonitoringComponent() {
		Log.d(MusesUtils.TEST_TAG, "UCEH - manageMonitoringComponent()");
		if(isUserAuthenticated) {
			UserContextMonitoringController.getInstance(getContext()).startContextObservation();
		}
	}

	/**
	 * Method to request a configuration update
	 */
	private void sendConfigSyncRequest() {
		Log.d(MusesUtils.TEST_TAG, "UCEH - sendConfigSyncRequest()");
		JSONObject configSyncRequest = JSONManager.createConfigSyncJSON(getImei(), SettingsSensor.getOSVersion(), getUserName());
		Log.d(MusesUtils.TEST_TAG, configSyncRequest.toString());
		sendRequestToServer(configSyncRequest);
	}

	/**
	 * Method to logout the user, so that no more events are send to the server in his/her name
	 */
	public void logout() {
		Log.d(MusesUtils.TEST_TAG, "UCEH - logout()");
		if(serverStatus == Statuses.ONLINE) {
			JSONObject logoutJSON = JSONManager.createLogoutJSON(getUserName(), getImei());
			sendRequestToServer(logoutJSON);
		}
		else {
			// we cannot logout to the server, so send a logout response for the GUI immediately
			ActuatorController.getInstance(context).sendLoginResponse(false, context.getString(R.string.logout_successfully_msg), -1);
		}

		isUserAuthenticated = false;
        isAuthenticatedRemotely = false;
		updateServerOnlineAndUserAuthenticated();

		NotificationController.getInstance(context).removeNotification();
	}

	/**
	 * Method to store a context event in the database if
	 * there is no connection to the server
	 *
	 * @param action {@link Action}
	 * @param properties {@link Map}<String, String>
	 * @param contextEvents {@link ContextEvent}
	 */
	public void storeContextEvent(Action action, Map<String, String> properties, List<ContextEvent> contextEvents) {
		Log.d(MusesUtils.TEST_TAG, "UCEH - storeContextEvent()");
		Log.d(TAG, "called: storeContextEvent(Action action, Map<String, String> properties, List<ContextEvent> contextEvents)");
		// TODO maybe remove this: (properties != null) && (contextEvents != null) because a user behavior doesn't contain this information
		if((action != null) && (properties != null) && (contextEvents != null)) {
			if(dbManager == null) {
				dbManager = new DBManager(context);
			}
			dbManager.openDB();

			int actionId = (int) dbManager.addOfflineAction(DBEntityParser.transformActionToEntityAction(action));
			for(Map.Entry<String, String> entry : properties.entrySet()) {
				// transform to action property
				ActionProperty actionProperty = new ActionProperty();
				actionProperty.setActionId(actionId);
				actionProperty.setKey(entry.getKey());
				actionProperty.setValue(entry.getValue());

				dbManager.addOfflineActionProperty(actionProperty);
			}

			for(ContextEvent contextEvent : contextEvents){
				long contextEventId = dbManager.addContextEvent(DBEntityParser.transformContextEvent(actionId, contextEvent));
				for(Property property : DBEntityParser.transformProperty(contextEventId, contextEvent)) {
					dbManager.addProperty(property);
				}
			}
			dbManager.closeDB();
		}
	}

	/**
	 * Method to send locally stored data to the server
	 */
	public void sendOfflineStoredContextEventsToServer() {
		Log.d(MusesUtils.TEST_TAG, "UCEH - sendOfflineStoredContextEventsToServer()");
		Log.d(TAG, "called: sendOfflineStoredContextEventsToServer()");
        Log.d(TAG_DB, "send offline context events called");
		/*
		 * 1. check if the user is authenticated
		 * 2. check if the dbManager object is null
		 * 3. get a list of all stored actions
		 * 4. for each action do:
		 * 4.1 get all related properties of that action
		 * 4.2 get all context events of that action
		 * 4.3 for each context event:
		 * 4.3.1 get all related properties to that action
		 * 4.4. create a json for
		 * 4.5. send this json to the server
		 */

		//1. check if the user is authenticated
		if(isUserAuthenticated) {
			Log.d(APP_TAG, "Info SS, Sending offline stored context events to server if user authenticated.");
			// 2. check if the dbManager object is null
			if (dbManager == null) {
				dbManager = new DBManager(context);
			}

			// 3. get a list of all stored actions
            dbManager.openDB();
            Log.d(TAG_DB, "action table size: "+dbManager.getOfflineActionList().size());
			for (eu.musesproject.client.db.entity.Action entityAction : dbManager.getOfflineActionList()) {
				Action action = DBEntityParser.transformAction(entityAction);

				//  4.1 get all related properties of that action
				List<ActionProperty> entityActionProperties = dbManager.getOfflineActionPropertiesOfAction(entityAction.getId());
				Map<String, String> actionProperties = DBEntityParser.transformActionPropertyToMap(entityActionProperties);

				//4.2 get all context events of that action
				List<ContextEvent> contextEvents = new ArrayList<ContextEvent>();
				for(eu.musesproject.client.db.entity.ContextEvent dbContextEvent : dbManager.getStoredContextEventByActionId(entityAction.getId())) {
					ContextEvent contextEvent = DBEntityParser.transformEntityContextEvent(dbContextEvent);

					// 4.3.1 get all related properties to that action
					for(Property property: dbManager.getPropertiesOfContextEvent(contextEvent.getId())) {
						contextEvent.addProperty(property.getKey(), property.getValue());
					}

					contextEvents.add(contextEvent);
				}

				// 4.4. create a json for
				JSONObject requestObject = JSONManager.createJSON(getImei(), getUserName(), -1, RequestType.ONLINE_DECISION, action, actionProperties, contextEvents);

				// 4.5. send this json to the server
				sendRequestToServer(requestObject);
			}
            dbManager.closeDB();
		}
	}

	/**
	 * Info SS
	 *
	 * Method to send a request to the server
	 *
	 * @param requestJSON {@link org.json.JSONObject}
	 */
	public void sendRequestToServer(JSONObject requestJSON) {
		Log.d(MusesUtils.TEST_TAG, "UCEH - sendRequestToServer(JSONObject requestJSON)");
		Log.d(TAG, "called: sendRequestToServer(JSONObject requestJSON)");
		if (requestJSON != null && !requestJSON.toString().isEmpty()) {
			if(serverStatus == Statuses.ONLINE) {
                String sendData  = requestJSON.toString();
                Log.d(TAG, "sendData:"+sendData);

                int jsonRequestID = sendData.hashCode();
                pendingJSONRequest.put(jsonRequestID, requestJSON);
                connectionManager.sendData(sendData, jsonRequestID);
            }
		}
	}

	/**
	 * Should be called when the background service is created
	 *
	 * @param context {@link Context}
	 */
	public void setContext(Context context) {
		this.context = context;

		// update the flags
		prefs = context.getSharedPreferences(MainActivity.PREFERENCES_KEY, Context.MODE_PRIVATE);
		isAuthenticatedRemotely = prefs.getBoolean(PREF_KEY_USER_AUTHENTICATED_REMOTELY, false);
		isUserAuthenticated = prefs.getBoolean(PREF_KEY_USER_AUTHENTICATED, false);
	}

	public Context getContext(){
		return this.context;
	}

	public void updateServerOnlineAndUserAuthenticated() {
		Log.d(MusesUtils.TEST_TAG, "UCEH - updateServerOnlineAndUserAuthenticated Server="+(serverStatus==Statuses.ONLINE) + " auth=" +isUserAuthenticated);
		isUserAuthenticated = isAuthenticatedRemotely;

		SharedPreferences.Editor prefEditor = prefs.edit();
		prefEditor.putBoolean(PREF_KEY_USER_AUTHENTICATED, isUserAuthenticated);
		prefEditor.putBoolean(PREF_KEY_USER_AUTHENTICATED_REMOTELY, isAuthenticatedRemotely);
		prefEditor.commit();
	}

	private class ConnectionCallback implements IConnectionCallbacks {


		@Override
		public int receiveCb(String receivedData) {
			Log.d(TAG, "called: receiveCb(String receivedData) receivedData:"+receivedData);
			if((receivedData != null) && (!receivedData.equals(""))) {
				if(dbManager == null) {
					dbManager = new DBManager(context);
				}

				// identify the request type
				String requestType = JSONManager.getRequestType(receivedData);
				Log.d(MusesUtils.TEST_TAG, "UCEH - receiveCb(); requestType=" +requestType);
				Log.d(APP_TAG, "UCEH - receiveCb(); requestType=" + requestType);

				if(requestType.equals(RequestType.UPDATE_POLICIES)) {
					RemotePolicyReceiver.getInstance().updateJSONPolicy(receivedData, context);

					// look for the related request
					int requestId = JSONManager.getRequestId(receivedData);
					Log.d(TAG_RQT, "request_id from the json is " + requestId);
					if(mapOfPendingRequests != null && mapOfPendingRequests.containsKey(requestId)) { // this should ne
						RequestHolder requestHolder = mapOfPendingRequests.get(requestId);
						requestHolder.getRequestTimeoutTimer().cancel();
						mapOfPendingRequests.remove(requestId);
						Log.d(TAG_RQT2, "Removing action: " +JSONManager.getActionType(receivedData));
						send(requestHolder.getAction(), requestHolder.getActionProperties(), requestHolder.getContextEvents());
                        Log.d(APP_TAG, "UCEH - receiveCb(); Condition is" + JSONManager.getPolicyCondition(receivedData) + " for request id:" + JSONManager.getRequestId(receivedData) + " for action:" + requestHolder.getAction().getActionType());
					}
				}
				else if(requestType.equals(RequestType.AUTH_RESPONSE)) {
					isAuthenticatedRemotely = JSONManager.getAuthResult(receivedData);
                    isUserAuthenticated = isAuthenticatedRemotely;
                    String authMessage = JSONManager.getAuthMessage(receivedData);
                    updateServerOnlineAndUserAuthenticated();
					if(isAuthenticatedRemotely) {
						dbManager.openDB();
						dbManager.insertCredentials(getImei(), tmpLoginUserName, tmpLoginPassword);
						dbManager.closeDB();
						// clear the credentials in the fields
						userName = tmpLoginUserName;
						tmpLoginUserName = "";
						tmpLoginPassword = "";

						setServerStatus(Statuses.ONLINE);
						sendOfflineStoredContextEventsToServer();
                        resendFailedJSONRequests();
						updateServerOnlineAndUserAuthenticated();
						sendConfigSyncRequest();
					}
					ActuatorController.getInstance(context).sendLoginResponse(isAuthenticatedRemotely, authMessage, -1);
				}
				else if(requestType.equals(RequestType.LOGOUT_RESPONSE)) {
					String authMessage = JSONManager.getAuthMessage(receivedData);
					ActuatorController.getInstance(context).sendLoginResponse(false, authMessage, -1);
				}
				else if(requestType.equals(RequestType.CONFIG_UPDATE)) {
                	/*
                	 *  sensor configuration
                	 *  1.1 load config items from JSON
                	 *  1.2 insert config items in db
                	 *  1.3 notify sensors about the new configuration
                	 */
					// 1.1 load config items from JSON
					List<SensorConfiguration> configList = JSONManager.getSensorConfig(receivedData);
					// 1.2 insert config items in db
					boolean sensorConfigAlreadyExists;
					dbManager.openDB();
					sensorConfigAlreadyExists = dbManager.hasSensorConfig();
					dbManager.closeDB();
					if(!sensorConfigAlreadyExists) {
						dbManager.openDB();
						for(SensorConfiguration configItem : configList) {
							dbManager.insertSensorConfiguration(configItem);
						}
						dbManager.closeDB();
						// 1.3 notify sensors about the new configuration
						UserContextMonitoringController.getInstance(getContext()).onSensorConfigurationChanged();
					}

                	/*
                	 *  trials configuration
                	 *  2.1 load config from JSON
                	 *  2.2 insert into the database
                	 */
					// 2.1
					boolean isSilentModeActivated = JSONManager.isSilentModeActivated(receivedData);

                	/*
                	 *  connection configuration
                	 *  3.1 load config from JSON
                	 *  3.2 insert new config in the db
                	 *  3.3 update the connection manager
                	 */
					// 3.1 load config from JSON
					Configuration connectionConfig = JSONManager.getConnectionConfiguration(receivedData, getContext());
					connectionConfig.setSilentMode(isSilentModeActivated ? 1 : 0);
					Log.d(MusesUtils.TEST_TAG, "UCEH - isSilentModeActivated="+isSilentModeActivated);
					// 2.2 & 3.2 insert new config in the db
					if(!connectionConfig.toString().isEmpty()) {
						dbManager.openDB();
						dbManager.insertConfiguration(connectionConfig);
						dbManager.closeDB();
						// 3.3 update the connection manager
						connectionManager.setTimeout(connectionConfig.getTimeout());
						connectionManager.setPolling(connectionConfig.getPollingEnabled());
						connectionManager.setPollTimeOuts(connectionConfig.getPollTimeout(), connectionConfig.getSleepPollTimeout());
					}
				}
			}
			return 0;
		}

		@Override
		public int statusCb(int status, int detailedStatus, int dataId) {
			Log.d(TAG, "called: statusCb(int status, int detailedStatus)"+status+", "+detailedStatus);
			// detect if server is back online after an offline status
			if(status == Statuses.ONLINE && detailedStatus == DetailedStatuses.SUCCESS) {
				if(serverStatus == Statuses.OFFLINE) {
					Log.d(APP_TAG, "Server back to ONLINE, sending offline stored events to server");
                    setServerStatus(status);
                    updateServerOnlineAndUserAuthenticated();
                    sendOfflineStoredContextEventsToServer();
                    resendFailedJSONRequests();
                }
			}
            else if(status == Statuses.ONLINE && detailedStatus == DetailedStatuses.SUCCESS_NEW_SESSION) {
				if(serverStatus == Statuses.OFFLINE) {
					Log.d(APP_TAG, "Server back to ONLINE, sending offline stored events to server");
					setServerStatus(status);
    				}
				// Since new session not authenticated remotely
				isAuthenticatedRemotely = false;
				updateServerOnlineAndUserAuthenticated();
                autoLogin();
            }
			else if(status == Statuses.OFFLINE) {
				setServerStatus(status);
				// Can still be authenticated, but server not reachable.
				// Depends on new session or not when ONLINE
				updateServerOnlineAndUserAuthenticated();
			}
            else if(status == Statuses.DATA_SEND_OK) {
                if(detailedStatus == DetailedStatuses.SUCCESS) {
                    dbManager.openDB();
                    dbManager.resetStoredContextEventTables();
                    dbManager.closeDB();

                    pendingJSONRequest.remove(dataId);
                    Log.d(MusesUtils.TEST_TAG, "data send ok.removed item from pendingJSONRequest, size=" + pendingJSONRequest.size());
                }
            }
            else if(status == Statuses.DATA_SEND_FAILED) {
                failedJSONRequest.put(dataId, pendingJSONRequest.get(dataId));
                Log.d(MusesUtils.TEST_TAG, "data send failed.failedJSONRequest size=" + failedJSONRequest.size());
            }
            else if(status == Statuses.NEW_SESSION_CREATED && detailedStatus == DetailedStatuses.SUCCESS_NEW_SESSION) {
                isAuthenticatedRemotely = false;
				updateServerOnlineAndUserAuthenticated();
                autoLogin();
            }
			else if(status == Statuses.CONNECTION_FAILED && detailedStatus == DetailedStatuses.NO_INTERNET_CONNECTION) {
				setServerStatus(status);
				updateServerOnlineAndUserAuthenticated();
			}
			else if(status == Statuses.DISCONNECTED && detailedStatus == DetailedStatuses.NO_INTERNET_CONNECTION) {
				setServerStatus(status);
				updateServerOnlineAndUserAuthenticated();
			}

            if(status == Statuses.ONLINE) {
				setServerStatus(status);
                updateServerOnlineAndUserAuthenticated();
            }

			if(detailedStatus == DetailedStatuses.UNKNOWN_ERROR) {
				Log.d(MusesUtils.TEST_TAG, "UCEH - UNKNOWN_ERROR callback");
				// fires the unknown error feedback
				ActuatorController.getInstance(context).showFeedback(new ActuationInformationHolder());
			}

			// if the user tries to login and the server responses with an error, this error code will be send in the
			// login callback
			try {
				if (pendingJSONRequest.size() > 0 && JSONManager.getRequestType(pendingJSONRequest.get(dataId).toString()).equals(RequestType.LOGIN)) {
					if ((detailedStatus == DetailedStatuses.INCORRECT_CERTIFICATE)
							|| (detailedStatus == DetailedStatuses.INCORRECT_URL)
							|| (detailedStatus == DetailedStatuses.INTERNAL_SERVER_ERROR)
							|| (detailedStatus == DetailedStatuses.NO_INTERNET_CONNECTION)
							|| (detailedStatus == DetailedStatuses.UNKNOWN_ERROR)
							|| (detailedStatus == DetailedStatuses.NOT_ALLOWED_FROM_SERVER_UNAUTHORIZED)
							|| (detailedStatus == DetailedStatuses.NOT_FOUND)) {
						ActuatorController.getInstance(context).sendLoginResponse(false, "", detailedStatus);
					}
				}
			} catch (NullPointerException e) {
				// json string is empty
			}

			serverDetailedStatus = detailedStatus;

			return 0;
		}
	}

    private void resendFailedJSONRequests() {
        Log.d(MusesUtils.TEST_TAG, "resendFailedJSONRequests failedJSONRequest, size=" + failedJSONRequest.size());
        List<JSONObject> tmpList = new ArrayList<JSONObject>(failedJSONRequest.values());
        failedJSONRequest.clear();
        for (JSONObject jsonObject : tmpList) {
            sendRequestToServer(jsonObject);
        }
    }

    public String getImei() {
		if(imei == null || imei.equals("")) {
			this.imei = new SettingsSensor(getContext()).getIMEI();
		}
		return imei;
	}

    public boolean isUserAuthenticated() {
        return isUserAuthenticated;
    }

	public String getUserName() {
		if(isUserAuthenticated) {
			if(userName == null || userName.equals("")) {
				if(prefs == null) {
					prefs = context.getSharedPreferences(MainActivity.PREFERENCES_KEY,
							Context.MODE_PRIVATE);
				}

				String tmpUserName = prefs.getString(MainActivity.USERNAME, "");
				if(!tmpUserName.equals("")) {
					userName = tmpUserName;
				}
				else {
					// user name is unknown, start the MainActivity, so that the user can login again
					context.startActivity(new Intent(context, MainActivity.class));
					userName = "unknown";
				}
			}

			return userName;
		}
		else {
			// user name is unknown, start the MainActivity, so that the user can login again
			context.startActivity(new Intent(context, MainActivity.class));
			return "unknown";
		}
	}

	public void removeRequestById(int requestId) {
		Log.d(TAG_RQT, "6. removeRequestById map size: " + mapOfPendingRequests.size());
		if(mapOfPendingRequests != null && mapOfPendingRequests.containsKey(requestId)) {
			Log.d(TAG_RQT2, "Removing action with id: " +requestId);
			mapOfPendingRequests.remove(requestId);
			Log.d(TAG_RQT, "7. removeRequestById map size afterwards: " + mapOfPendingRequests.size());
		}
	}

	@Override
	public void handleRequestTimeout(int requestId) {
		Log.d(TAG_RQT, "5. handleRequestTimeout to id: " + requestId);
		// 1. store object temporary
		// 2. remove object from the map that holds all RequestHolder
		// 3. perform default decision

		// -> 1.
		RequestHolder requestHolder = mapOfPendingRequests.get(requestId);
		// -> 2.
		removeRequestById(requestId);
		// -> 3.
		if(decisionMaker == null) {
			decisionMaker = new DecisionMaker();
		}
        // TODO do not show the default policies at this point
//		Decision decision =  decisionMaker.getDefaultDecision(requestHolder.getAction(), requestHolder.getActionProperties(), requestHolder.getContextEvents());
//		ActuatorController.getInstance(context).showFeedback(decision);
//		if(requestHolder.getAction().isRequestedByMusesAwareApp()) {
//			ActuatorController.getInstance(context).sendFeedbackToMUSESAwareApp(decision);
//		}
	}
}
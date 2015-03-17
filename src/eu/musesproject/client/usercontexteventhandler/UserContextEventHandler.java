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
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.Decision;
import eu.musesproject.client.model.decisiontable.Request;
import eu.musesproject.client.model.decisiontable.Resource;
import eu.musesproject.client.securitypolicyreceiver.RemotePolicyReceiver;
import eu.musesproject.client.ui.MainActivity;
import eu.musesproject.client.utils.MusesUtils;
import eu.musesproject.contextmodel.ContextEvent;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class UserContextEventHandler. Singleton
 * @author Christoph
 * @version 28 feb 2014
 */
public class UserContextEventHandler implements RequestTimeoutTimer.RequestTimeoutHandler {
	private static final String TAG = UserContextEventHandler.class.getSimpleName();
	public static final String TAG_RQT = "REQUEST_TIMEOUT";
	public static final String TAG_DB = "DATABASE_TEST_CODE";
	public static final String TAG_MUSES_AWARE = "MUSES_AWARE";
	public static final String APP_TAG = "APP_TAG";
	public static final String APP_TAG2 = "APP_TAG2";

	private static UserContextEventHandler userContextEventHandler = null;
	//	private static final String MUSES_SERVER_URL = "http://192.168.44.101:8888/commain";
//	private static final String MUSES_SERVER_URL = "https://192.168.44.101:8443/server/commain";
//  private static final String MUSES_SERVER_URL = "https://192.168.44.101:8443/server-0.0.1-SNAPSHOT/commain";
//  private static final String MUSES_SERVER_URL = "http://192.168.44.107:8080/server/commain";
//  private static final String MUSES_SERVER_URL = "http://192.168.35.198/server/commain";
	private static final String MUSES_SERVER_URL = "https://172.17.3.5:8443/server/commain";

	private Context context;

	private SharedPreferences prefs;

	// connection fields
	private ConnectionManager connectionManager;
	private IConnectionCallbacks connectionCallback;
	public int serverStatus;
	private int serverDetailedStatus;

    private boolean isAuthenticatedRemotely;
    private boolean isUserAuthenticated;
	public static boolean serverOnlineAndUserAuthenticated;

	private DecisionMaker decisionMaker;

	private Map<Integer, RequestHolder> mapOfPendingRequests;//String key is the hashID of the request object

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
		serverOnlineAndUserAuthenticated = false;

		decisionMaker = new DecisionMaker();

		mapOfPendingRequests = new HashMap<Integer, RequestHolder>();
	}

	/**
	 * Method to get the current server status (online, offline)
	 * @return int. {@link Statuses} online: 1; offline:0
	 */
	public int getServerStatus() {
		return serverStatus;
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
		if (config.getServerIP() == null)
		{
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
		Log.d(APP_TAG, "Action: " + action.getActionType());
		Log.d(TAG, "called: send(Action action, Map<String, String> properties, List<ContextEvent> contextEvents)");
		boolean onlineDecisionRequested = false;

		Decision decision = retrieveDecision(action, properties, contextEvents);

		if(decision != null) { // local decision found
			Log.d(APP_TAG, "Decision is: " + decision.getName());
			Log.d(APP_TAG, "Info DC, Local decision found, now calling actuator to showFeedback");
			ActuatorController.getInstance().showFeedback(decision);
		}
		else { // if there is no local decision, send a request to the server
			if(serverStatus == Statuses.ONLINE && isUserAuthenticated) { // if the server is online, request a decision
				// flag that an online decision is requested
				onlineDecisionRequested = true;

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

				// create the JSON request and send it to the server
				JSONObject requestObject = JSONManager.createJSON(getImei(), getUserName(), requestHolder.getId(), RequestType.ONLINE_DECISION, action, properties, contextEvents);
				Log.d(APP_TAG, "Info DC, No Local decision found, Sever is ONLINE, sending user data JSON(actions,properties,contextevnts) to server");
				sendRequestToServer(requestObject);
			}
			else if(serverStatus == Statuses.ONLINE && !isUserAuthenticated) {
				storeContextEvent(action, properties, contextEvents);
			}
			else if(serverStatus == Statuses.OFFLINE && isUserAuthenticated) {
				Log.d(APP_TAG, "showFeedback2");
				ActuatorController.getInstance().showFeedback(new DecisionMaker().getDefaultDecision());
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
	 * 3. Make sure that the {@link DecisionMaker} is initialized
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
		Log.d(APP_TAG, "Info DC, Calling decision maker");
		if(decisionMaker == null) {
			decisionMaker = new DecisionMaker();
		}
		return decisionMaker.makeDecision(request, contextEvents, properties);
	}

	/**
	 * Method that takes an {@link eu.musesproject.client.model.decisiontable.Action}
	 * which contains the decision taken by the user on the MUSES UI.
	 * This behavior will be send to the server
	 * @param action
	 */
	public void sendUserBehavior(Action action) {
        // display next Feedback dialog if there is any
        ActuatorController.getInstance().removeFeedbackFromQueue();

        // send the current user feedback to the server
		if(serverStatus == Statuses.ONLINE && isUserAuthenticated) {
			Log.d(MusesUtils.TEST_TAG, "UCEH - sendUserBehavior(Action action)");
			Log.d(APP_TAG, "Info U, sending user behavior to server with action");
			JSONObject userBehaviorJSON = JSONManager.createUserBehaviorJSON(getImei(), getUserName(), action.getActionType());
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


		String deviceId;
		dbManager.openDB();
		if((deviceId = dbManager.getDevId()) == null || deviceId.isEmpty()) {
			deviceId = getImei();
		}
		dbManager.closeDB();

        Log.d(APP_TAG, "login, server status: " + (serverStatus == Statuses.ONLINE));
		if(serverStatus == Statuses.ONLINE) {
			Log.d(APP_TAG, "Info U, Authenticating user login to server with username: "+tmpLoginUserName+" password: "+tmpLoginPassword + " deviceId: " +deviceId);
			JSONObject requestObject = JSONManager.createLoginJSON(tmpLoginUserName, tmpLoginPassword, deviceId);
			sendRequestToServer(requestObject);
		}
		else {
			Log.d(APP_TAG, "Info U, Authenticating login with username:"+tmpLoginUserName+" password:"+tmpLoginPassword + " deviceId: " + deviceId + " in localdatabase");
			dbManager.openDB();
			isUserAuthenticated = dbManager.isUserAuthenticated(getImei(), tmpLoginUserName, tmpLoginPassword);
			dbManager.closeDB();
			ActuatorController.getInstance().sendLoginResponse(isUserAuthenticated);
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
		Log.d(MusesUtils.TEST_TAG, "UCEH - autoLogin()");
		if(prefs == null) {
			prefs = context.getSharedPreferences(MainActivity.PREFERENCES_KEY,
					Context.MODE_PRIVATE);
		}
		this.userName = prefs.getString(MainActivity.USERNAME, "");
		String password = prefs.getString(MainActivity.PASSWORD, "");

		if(userName.isEmpty() || password.isEmpty()) {
			Log.d("auto_login_test", "cannot auto login");
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

	private void manageMonitoringComponent() {
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
		JSONObject configSyncRequest = JSONManager.createConfigSyncJSON(getImei(), getUserName());
		Log.d(MusesUtils.TEST_TAG, configSyncRequest.toString());
		sendRequestToServer(configSyncRequest);
	}

	/**
	 * Method to logout the user, so that no more events are send to the server in his/her name
	 */
	public void logout() {
		Log.d(MusesUtils.TEST_TAG, "UCEH - logout()");
		JSONObject logoutJSON = JSONManager.createLogoutJSON(getUserName(), getImei());
		sendRequestToServer(logoutJSON);

		isUserAuthenticated = false;
        isAuthenticatedRemotely = false;
		updateServerOnlineAndUserAuthenticated();
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

			int actionId = (int) dbManager.addAction(DBEntityParser.transformActionToEntityAction(action));
			for(Map.Entry<String, String> entry : properties.entrySet()) {
				// transform to action property
				ActionProperty actionProperty = new ActionProperty();
				actionProperty.setActionId(actionId);
				actionProperty.setKey(entry.getKey());
				actionProperty.setValue(entry.getValue());

				dbManager.addActionProperty(actionProperty);
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
            Log.d(TAG_DB, "action table size: "+dbManager.getActionList().size());
			for (eu.musesproject.client.db.entity.Action entityAction : dbManager.getActionList()) {
				Action action = DBEntityParser.transformAction(entityAction);

				//  4.1 get all related properties of that action
				List<ActionProperty> entityActionProperties = dbManager.getActionPropertiesOfAction(entityAction.getId());
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
		if (requestJSON != null) {
			if(serverStatus == Statuses.ONLINE) {
				String sendData  = requestJSON.toString();
				Log.d(TAG, "sendData:"+sendData);//Demo Debug
				connectionManager.sendData(sendData);
			}
		}
	}

	/**
	 * Should be called when the background service is created
	 * @param context {@link Context}
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext(){
		return this.context;
	}


	public static boolean isServerOnlineAndUserAuthenticated() {
		return serverOnlineAndUserAuthenticated;
	}

	public void updateServerOnlineAndUserAuthenticated() {
		Log.d(MusesUtils.TEST_TAG, "UCEH - updateServerOnlineAndUserAuthenticated Server="+(serverStatus==Statuses.ONLINE) + " auth=" +isUserAuthenticated);
        if(isAuthenticatedRemotely) {
            isUserAuthenticated = true;
        }
		if(serverStatus == Statuses.ONLINE && isUserAuthenticated) {
			serverOnlineAndUserAuthenticated = true;
		}
		else {
			serverOnlineAndUserAuthenticated= false;
		}
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
				Log.d(APP_TAG, "Request type was " + requestType);

				if(requestType.equals(RequestType.UPDATE_POLICIES)) {
					Log.d(APP_TAG, "Updating polices");
					RemotePolicyReceiver.getInstance().updateJSONPolicy(receivedData, context);

					// look for the related request
					int requestId = JSONManager.getRequestId(receivedData);
					Log.d(TAG_RQT, "request_id from the json is " + requestId);
					if(mapOfPendingRequests != null && mapOfPendingRequests.containsKey(requestId)) {
						RequestHolder requestHolder = mapOfPendingRequests.get(requestId);
						requestHolder.getRequestTimeoutTimer().cancel();
						mapOfPendingRequests.remove(requestId);
						send(requestHolder.getAction(), requestHolder.getActionProperties(), requestHolder.getContextEvents());
                        Log.d(APP_TAG, "condition is: " + JSONManager.getPolicyCondition(receivedData) + " for request id:" + JSONManager.getRequestId(receivedData) + " for action:" + requestHolder.getAction().getActionType());
					}
				}
				else if(requestType.equals(RequestType.AUTH_RESPONSE)) {

					isAuthenticatedRemotely = JSONManager.getAuthResult(receivedData);
					Log.d(APP_TAG, "Retreiving auth response from JSON, authenticated: " + isAuthenticatedRemotely);
                    updateServerOnlineAndUserAuthenticated();
					if(isAuthenticatedRemotely) {
						dbManager.openDB();
						dbManager.insertCredentials(getImei(), tmpLoginUserName, tmpLoginPassword);
						dbManager.closeDB();
						// clear the credentials in the fields
						tmpLoginUserName = "";
						tmpLoginPassword = "";

						serverStatus = Statuses.ONLINE;
						sendOfflineStoredContextEventsToServer();
						updateServerOnlineAndUserAuthenticated();
						sendConfigSyncRequest();
					}
					ActuatorController.getInstance().sendLoginResponse(isAuthenticatedRemotely);
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
					if(connectionConfig != null) {
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
		public int statusCb(int status, int detailedStatus) {
			Log.d(TAG, "called: statusCb(int status, int detailedStatus)"+status+", "+detailedStatus);
			// detect if server is back online after an offline status
			if(status == Statuses.ONLINE && detailedStatus == DetailedStatuses.SUCCESS) {
				if(serverStatus == Statuses.OFFLINE) {
					Log.d(APP_TAG, "Server back to ONLINE, sending offline stored events to server");
                    serverStatus = status;
                    updateServerOnlineAndUserAuthenticated();
                    sendOfflineStoredContextEventsToServer();
                }
			}
            else if(status == Statuses.ONLINE && detailedStatus == DetailedStatuses.SUCCESS_NEW_SESSION) {
				if(serverStatus == Statuses.OFFLINE) {
					Log.d(APP_TAG, "Server back to ONLINE, sending offline stored events to server");
                    serverStatus = status;
    				}
				// Since new session not authenticated remotely
				isAuthenticatedRemotely = false;
                autoLogin();
            }
			else if(status == Statuses.OFFLINE) {
				serverStatus = status;
				// Can still be authenticated, but server not reachable. 
				// Depends on new session or not when ONLINE
				// isAuthenticatedRemotely = false;
				updateServerOnlineAndUserAuthenticated();
			}
            else if(status == Statuses.DATA_SEND_OK) {
                if(detailedStatus == DetailedStatuses.SUCCESS) {
                    dbManager.openDB();
                    dbManager.resetStoredContextEventTables();
                    dbManager.closeDB();
                }
            }
            else if(status == Statuses.NEW_SESSION_CREATED) {
                isAuthenticatedRemotely = false;

                autoLogin();
            }

            if(status == Statuses.ONLINE ) {
                serverStatus = status;
                updateServerOnlineAndUserAuthenticated();
            }

			if(detailedStatus == DetailedStatuses.UNKNOWN_ERROR) {
				// fires the unknown error feedback
				ActuatorController.getInstance().showFeedback(null);
			}

            //Log.d(APP_TAG, "statusCb status: " + (serverStatus == Statuses.ONLINE));
			serverDetailedStatus = detailedStatus;

			return 0;
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

    public void setUserAuthenticated(boolean isUserAuthenticated) {
        this.isUserAuthenticated = isUserAuthenticated;
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
					userName = "unknown"; // TODO look in db too
				}
			}

			return userName;
		}
		else {
			return "unknown";
		}
	}

	public void removeRequestById(int requestId) {
		Log.d(TAG_RQT, "6. removeRequestById map size: " + mapOfPendingRequests.size());
		if(mapOfPendingRequests != null && mapOfPendingRequests.containsKey(requestId)) {
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
		Decision decision =  decisionMaker.getDefaultDecision(requestHolder.getAction(), requestHolder.getActionProperties(), requestHolder.getContextEvents());
		Log.d(APP_TAG, "           4");
		ActuatorController.getInstance().showFeedback(decision);
		if(requestHolder.getAction().isRequestedByMusesAwareApp()) {
			ActuatorController.getInstance().sendFeedbackToMUSESAwareApp(decision, getContext());
		}
	}
}
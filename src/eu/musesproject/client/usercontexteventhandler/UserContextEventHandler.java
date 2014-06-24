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
 * it under the terms of the GNU Affero General Public License as published by
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import eu.musesproject.client.actuators.ActuatorController;
import eu.musesproject.client.connectionmanager.AlarmReceiver;
import eu.musesproject.client.connectionmanager.ConnectionManager;
import eu.musesproject.client.connectionmanager.IConnectionCallbacks;
import eu.musesproject.client.connectionmanager.Statuses;
import eu.musesproject.client.db.entity.Property;
import eu.musesproject.client.db.handler.DBManager;
import eu.musesproject.client.db.handler.ResourceCreator;
import eu.musesproject.client.decisionmaker.DecisionMaker;
import eu.musesproject.client.model.RequestType;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.Decision;
import eu.musesproject.client.model.decisiontable.Request;
import eu.musesproject.client.model.decisiontable.Resource;
import eu.musesproject.client.securitypolicyreceiver.RemotePolicyReceiver;
import eu.musesproject.contextmodel.ContextEvent;

/**
 * The Class UserContextEventHandler. Singleton
 * @author Christoph
 * @version 28 feb 2014
 */
public class UserContextEventHandler {
    private static final String TAG = UserContextEventHandler.class.getSimpleName();

    private static UserContextEventHandler userContextEventHandler = null;
//	private static final String MUSES_SERVER_URL = "http://192.168.44.101:8888/commain";
	private static final String MUSES_SERVER_URL = "https://192.168.44.101:8443/server/commain";
//  private static final String MUSES_SERVER_URL = "https://192.168.44.101:8443/server-0.0.1-SNAPSHOT/commain";
//  private static final String MUSES_SERVER_URL = "http://192.168.44.107:8080/server/commain";
//  private static final String MUSES_SERVER_URL = "http://172.17.3.5:8080/server-0.0.1-SNAPSHOT/commain";
	
	private Context context;

    // connection fields
    private ConnectionManager connectionManager;
    private IConnectionCallbacks connectionCallback;
	public int serverStatus;
	private int serverDetailedStatus;
	private boolean isUserAuthenticated;
	public static boolean serverOnlineAndUserAuthenticated;

	private Action tmpAction;
	private Map<String, String> tmpProperties;
	private List<ContextEvent> tmpContextEvents;
	
	private UserContextEventHandler() {
        connectionManager = new ConnectionManager();
        connectionCallback = new ConnectionCallback();

        serverStatus = Statuses.OFFLINE;
        serverDetailedStatus = Statuses.OFFLINE;
        isUserAuthenticated = false;
        serverOnlineAndUserAuthenticated = false;
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
        connectionManager.connect(	
                MUSES_SERVER_URL,
                AlarmReceiver.DEFAULT_POLL_INTERVAL,
                AlarmReceiver.DEFAULT_SLEEP_POLL_INTERVAL,
                connectionCallback,
                context
        );
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
        Log.d(TAG, "called: send(Action action, Map<String, String> properties, List<ContextEvent> contextEvents)");

        boolean onlineDecisionRequested = false;

        // check for a locally stored decision
        Resource resource = ResourceCreator.create(action, properties);
        Request request = new Request(action, resource);
        Decision decision = new DecisionMaker().makeDecision(request, contextEvents);
//        Decision decision = new DecisionMaker().makeDummyDecision(request, contextEvents);
        if(decision != null) { // local decision found
            ActuatorController.getInstance().showFeedback(decision);
        }
        else { // if there is no local decision, send a request to the server
            if(serverStatus == Statuses.ONLINE && isUserAuthenticated) { // if the server is online, request a decision
            	// flag that an online decision is requested
                onlineDecisionRequested = true;
                
                // temporary store the information so that the decision can be made after the server responded with 
                // an database update (new policies are sent from the server to the client and stored in the database)
                tmpAction = action;
                tmpProperties = properties;
                tmpContextEvents = contextEvents;

                // create the JSON request and send it to the server
                JSONObject requestObject = JSONManager.createJSON(RequestType.ONLINE_DECISION, action, properties, contextEvents);
                sendRequestToServer(requestObject);
            }
            else if(serverStatus == Statuses.OFFLINE || !isUserAuthenticated) { // save request to the database
                storeContextEvent(action, properties, contextEvents);
            }
        }
        // update context events even if a local decision was found.
        // Prevent sending context events again if they are already sent for a online decision
        if((!onlineDecisionRequested) && (serverStatus == Statuses.ONLINE) && isUserAuthenticated) {
            JSONObject requestObject = JSONManager.createJSON(RequestType.LOCAL_DECISION, action, properties, contextEvents);
            sendRequestToServer(requestObject);
        }
	}
	
	public void login(String userName, String password) {
        Log.d(TAG, "called: login(String userName, String password)");
        String deviceId = "";
        
        DBManager dbManager = new DBManager(context);
        dbManager.openDB();
        dbManager.insertCredentials();
        deviceId = dbManager.getDevId();
        dbManager.closeDB();
        
        if(serverStatus == Statuses.ONLINE) {
        	JSONObject requestObject = JSONManager.createLoginJSON(userName, password, deviceId);
        	sendRequestToServer(requestObject);
        }
        else {
        	ActuatorController.getInstance().sendLoginResponse(false);
        }
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
        Log.d(TAG, "called: storeContextEvent(Action action, Map<String, String> properties, List<ContextEvent> contextEvents)");
        if((action == null) && (properties == null) && (contextEvents != null)) {
            DBManager dbManager = new DBManager(context);
            dbManager.openDB();
            for(ContextEvent contextEvent : contextEvents){
                long contextEventId = dbManager.addContextEvent(DBEntityParser.transformContextEvent(contextEvent));
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
        Log.d(TAG, "called: sendOfflineStoredContextEventsToServer()");
        if(isUserAuthenticated) {
        	// load context events from the database
        	DBManager dbManager = new DBManager(context);
        	dbManager.openDB();
        	
        	List<ContextEvent> contextEvents = new ArrayList<ContextEvent>();
        	// get all db entity ContextEvents and Properties from the related tables and
        	// transform those tables to proper objects
        	for(eu.musesproject.client.db.entity.ContextEvent dbContextEvent : dbManager.getAllStoredContextEvents()) {
        		ContextEvent contextEvent = new ContextEvent();
        		contextEvent.setType(dbContextEvent.getType());
        		contextEvent.setTimestamp(Long.valueOf(dbContextEvent.getTimestamp()));
        		
        		List<Property> properties = dbManager.getAllProperties(/*ID as param*/);
        		for(Property property: properties) {
        			contextEvent.addProperty(property.getKey(), property.getValue());
        		}
        		
        		contextEvents.add(contextEvent);
        	}
        	dbManager.closeDB();
        	
        	// transform to JSON
        	JSONObject requestObject = JSONManager.createJSON(RequestType.UPDATE_CONTEXT_EVENTS, null, null, contextEvents);
        	// send to server
        	sendRequestToServer(requestObject);
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
		if(serverStatus == Statuses.ONLINE && isUserAuthenticated) {
			serverOnlineAndUserAuthenticated = true;
		}
		else {
			serverOnlineAndUserAuthenticated= false;
		}
	}
	
	private class ConnectionCallback implements IConnectionCallbacks {

		@Override
		public int receiveCb(String receiveData) {
            Log.d(TAG, "called: receiveCb(String receiveData)");
            if((receiveData != null) && (!receiveData.equals(""))) {
                String requestType = JSONManager.getRequestType(receiveData);

                if(requestType.equals(RequestType.ONLINE_DECISION)) {
                    // TODO get decision from the json
                    // send decision to the actuator controller
                    // dummy data
                    Decision decision = new Decision();
                    decision.setName(Decision.GRANTED_ACCESS);
                    ActuatorController.getInstance().showFeedback(decision);
                }
                else if(requestType.equals(RequestType.UPDATE_POLICIES)) {
                    RemotePolicyReceiver.getInstance().updateJSONPolicy(receiveData, context);
                    if(tmpAction != null && tmpProperties != null && tmpContextEvents != null) {
                    	send(tmpAction, tmpProperties, tmpContextEvents);
                    }
                    // reset temporary data
                    tmpAction = null;
                    tmpProperties = null;
                    tmpContextEvents = null;
                }
                else if(requestType.equals(RequestType.AUTH_RESPONSE)) {
                	isUserAuthenticated = JSONManager.getAuthResult(receiveData);
                	if(isUserAuthenticated) {
                		serverStatus = Statuses.ONLINE;
                		sendOfflineStoredContextEventsToServer();
                		updateServerOnlineAndUserAuthenticated();
                	}
            		ActuatorController.getInstance().sendLoginResponse(isUserAuthenticated);
                }
            }
			return 0;
		}

		@Override
		public int statusCb(int status, int detailedStatus) {
            Log.d(TAG, "called: statusCb(int status, int detailedStatus)");
            // detect if server is back online after an offline status
            if(status == Statuses.ONLINE ) {
                if(serverStatus == Statuses.OFFLINE) {
                    sendOfflineStoredContextEventsToServer();
                    isUserAuthenticated = false;
                    updateServerOnlineAndUserAuthenticated();
                }
                serverStatus = status;
                updateServerOnlineAndUserAuthenticated();
            }
            else if(status == Statuses.OFFLINE) {
			    serverStatus = status;
			    updateServerOnlineAndUserAuthenticated();
            }
			serverDetailedStatus = detailedStatus;
			return 0;
		}
	}
}
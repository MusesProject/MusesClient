/*
 * MUSES High-Level Object Oriented Model
 * Copyright MUSES project (European Commission FP7) - 2013 
 */
package eu.musesproject.client.usercontexteventhandler;

import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.util.Log;
import eu.musesproject.client.connectionmanager.Statuses;
import eu.musesproject.client.db.handler.DBManager;
import eu.musesproject.client.decisionmaker.DecisionMaker;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.Decision;
import org.json.JSONObject;

import android.content.Context;
import eu.musesproject.client.connectionmanager.AlarmReceiver;
import eu.musesproject.client.connectionmanager.ConnectionManager;
import eu.musesproject.client.connectionmanager.IConnectionCallbacks;
import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;
import eu.musesproject.client.contextmonitoring.service.aidl.DummyCommunication;
import eu.musesproject.client.model.actuators.ResponseInfoAP;
import eu.musesproject.client.model.actuators.RiskTreatment;
import eu.musesproject.client.model.decisiontable.Request;
import eu.musesproject.contextmodel.ContextEvent;

/**
 * The Class UserContextEventHandler. Singleton
 * @author Christoph
 * @version 28 feb 2014
 */
public class UserContextEventHandler {
    private static final String TAG = UserContextEventHandler.class.getSimpleName();

    private static UserContextEventHandler userContextEventHandler = null;
	
	private static final String MUSES_SERVER_URL = "http://192.168.44.101:8888/server-0.0.1-SNAPSHOT/commain";
	
	private Context context;
	
    private ConnectionManager connectionManager;
    private IConnectionCallbacks connectionCallback;
	private int serverStatus;
	private int serverDetailedStatus;
	
	private UserContextEventHandler() {
        connectionManager = new ConnectionManager();
        connectionCallback = new ConnectionCallback();

        serverStatus = Statuses.OFFLINE;
        serverDetailedStatus = Statuses.OFFLINE;
	}
	
	public static UserContextEventHandler getInstance() {
		if (userContextEventHandler == null) {
			userContextEventHandler = new UserContextEventHandler();
		}
		return userContextEventHandler;
	}
	
	/**
	 * Info DC
	 * 
	 * Method to provide additional context associated to a request
	 * 
	 * @param request
	 * 
	 * @return 
	 */
	public ContextEvent[] provideAdditionalContext(Request request){
		return new ContextEvent[0];
	}
	
	/**
	 * Info DC
	 * 
	 * Method to retrieve basic context information associated to a request
	 * 
	 * @param request
	 * 
	 * @return 
	 */
	public ContextEvent[] retrieveContextAssociatedToRequest(Request request){
		return new ContextEvent[0];
	}
	
	/**
	 * Info DC
	 * 
	 * Method called by the decision maker, in order to specify that the local decision information is not enough to
	 * take a decision, hence the decision is delegated to the server
	 * 
	 * @param request
	 * 
	 * @return 
	 */
	public void serverDelegatedDecision(Request request){
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
	public void send(Action action, Map<String, String> properties, List<ContextEvent> contextEvents){
        Log.d(TAG, "called: send(Action action, Map<String, String> properties, List<ContextEvent> contextEvents)");
        Request request = new Request(action, null);
        Decision decision = new DecisionMaker().makeDecision(request, contextEvents);
        if(decision != null) {
            // perform decision / send decision to actuator manager
             //TODO This is just Dummy code for the purpose of testing test
            // send to broadcast
            Intent messageIntent = new Intent();
            messageIntent.setAction("eu.musesproject.client.intent.muses.feedback");
            messageIntent.putExtra("message", decision.getName());
            context.sendBroadcast(messageIntent);

        }
        else { // if there is no local decision, send a request to the server
            if((context != null) && (serverStatus != Statuses.OFFLINE)) { // if the server is online
                JSONObject requestObject = JSONManager.createJSON(action, properties, contextEvents);
                sendRequestToServer(requestObject);
            }
            else if((context != null) && (serverStatus == Statuses.OFFLINE)) { // save request to the database
                storeContextEvent(action, properties, contextEvents);
            }
        }
	}
	
	public void login(String userName, String password) {
        Log.d(TAG, "called: login(String userName, String password)");
		// dummy response
		UserContextMonitoringController.getInstance(context).sendLoginResponseToUI(true);
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
        if((action == null) && (properties == null) && (contextEvents != null)) {
            DBManager dbManager = new DBManager(context);
            dbManager.openDB();
            for(ContextEvent contextEvent : contextEvents){
                //dbManager.addContextEvent(contextEvent);
            }
            dbManager.closeDB();
        }
    }

    /**
     * Method to send locally stored data to the server
     */
    public void sendOfflineStoredContextEventsToServer() {
        Log.d(TAG, "called: sendOfflineStoredContextEventsToServer()");
        // load contet events from the database
        DBManager dbManager = new DBManager(context);
        dbManager.openDB();
        // TODO finish when db methods are implemented
        List<ContextEvent> storedContextEvents = null; //dbManager.getAllStoredContextEvents();
        dbManager.closeDB();

        // transform to JSON
        JSONObject requestObject = JSONManager.createJSON(null, null, storedContextEvents);
        // send to server
        sendRequestToServer(requestObject);
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
            String sendData = requestJSON.toString();
            connectionManager.connect(
                    MUSES_SERVER_URL,
                    AlarmReceiver.DEFAULT_POLL_INTERVAL,
                    AlarmReceiver.DEFAULT_SLEEP_POLL_INTERVAL,
                    connectionCallback,
                    context
            );
            connectionManager.sendData(sendData);
            connectionManager.disconnect();
        }
    }

	/**
	 * Should be called when the background service is created
	 * @param context {@link Context}
	 */
	public void setContext(Context context) {
		this.context = context;
	}
	
	private class ConnectionCallback implements IConnectionCallbacks {

		@Override
		public int receiveCb(String receiveData) {
            Log.d(TAG, "called: receiveCb(String receiveData)");
			//TODO workflow to send the result back to Muses UI / aware app
			// dummy data
			ResponseInfoAP infoAP = ResponseInfoAP.DENY;
			RiskTreatment riskTreatment = new RiskTreatment(10,
					"The requested action was denied because of a low trustvalue", 2);
			new DummyCommunication(context).sendResponse(infoAP, riskTreatment);
			return 0;
		}

		@Override
		public int statusCb(int status, int detailedStatus) {
            Log.d(TAG, "called: statusCb(int status, int detailedStatus)");
            // detect if server is back online after an offline status
            if(status == Statuses.ONLINE ) {
                if(serverStatus == Statuses.OFFLINE) {
                    sendOfflineStoredContextEventsToServer();
                }
                serverStatus = status;
            }
            else if(status == Statuses.OFFLINE) {
			    serverStatus = status;
            }
			serverDetailedStatus = detailedStatus;
			return 0;
		}
	}
}
package eu.musesproject.client.contextmonitoring.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import eu.musesproject.client.connectionmanager.RequestHolder;
import eu.musesproject.client.contextmonitoring.sensors.AppSensor;
import eu.musesproject.client.db.entity.SensorConfiguration;
import eu.musesproject.client.model.JSONIdentifiers;
import eu.musesproject.client.model.RequestType;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.client.usercontexteventhandler.JSONManager;
import eu.musesproject.contextmodel.ContextEvent;

/**
 * Created by christophstanik on 3/12/14.
 */
public class JSONManagerTest extends TestCase {
    private Action action;
    private Map<String, String> actionProperties;
    private List<ContextEvent> contextEvents;
    
    private String deviceId;
    private String userName; 
    private String password; 
    private String requestType;
    private long actionTimestamp;
    private String actionType;
    private String userBehavior;
    private String successfulAuthenticationJSON;
    private String unSuccessfulAuthenticationJSON;
    private String responseJSON;
    private String configUpdateJSON;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // create dummy data
        requestType = RequestType.UPDATE_CONTEXT_EVENTS;

        deviceId = "123456";
        userName = "muses";
        password = "muses";
        
        actionTimestamp = System.currentTimeMillis();
        actionType = ActionType.ACCESS;
        action = new Action();
        action.setActionType(actionType);
        action.setTimestamp(actionTimestamp);

        actionProperties = new HashMap<String, String>();
        actionProperties.put("protocol", "https");
        actionProperties.put("url", "https://");
        actionProperties.put("resourceid", "file.png");
        actionProperties.put("method", "post");

        ContextEvent contextEvent = new ContextEvent();
        contextEvent.setTimestamp(actionTimestamp);
        contextEvent.setType(AppSensor.TYPE);
        contextEvent.addProperty(AppSensor.PROPERTY_KEY_APP_NAME, "app");
        contextEvent.addProperty(AppSensor.PROPERTY_KEY_ID, "1");
        contextEvent.addProperty(AppSensor.PROPERTY_KEY_BACKGROUND_PROCESS, "process1,process2,process3");

        contextEvents = new ArrayList<ContextEvent>();
        contextEvents.add(contextEvent);
        
        userBehavior = ActionType.CANCEL;
        
        successfulAuthenticationJSON = "{\"auth-message\":\"Successfully authenticated\",\"auth-result\":\"SUCCESS\",\"requesttype\":\"auth-response\"}";
        unSuccessfulAuthenticationJSON = "{\"auth-message\":\"Incorrect password\",\"auth-result\":\"FAIL\",\"requesttype\":\"auth-response\"}";
        
        responseJSON = "{\"muses-device-policy\":{\"files\":{\"action\":{\"request_id\":-1627519220,\"deny\":{\"id\":0,\"condition\":{\"appname\":\"Wifi Analyzer\"},\"path\":\"Wifi Analyzer\",\"riskTreatment\":\"You are trying to open an application which is considered harmful.\nOther people can gain control over your device.\"},\"type\":\"open_application\"}},\"revision\":1,\"schema-version\":1},\"requesttype\":\"update_policies\"}";
        configUpdateJSON = "{ \"sensor-configuration\":\n {\"sensor-property\":[\n {\"value\":\"avast! Mobile Security\",\"key\":\"trustedav\",\"sensor-type\":\"CONTEXT_SENSOR_DEVICE_PROTECTION\"},\n {\"value\":\"Mobile Security & Antivirus\",\"key\":\"trustedav\",\"sensor-type\":\"CONTEXT_SENSOR_DEVICE_PROTECTION\"},\n {\"value\":\"Avira Antivirus Security\",\"key\":\"trustedav\",\"sensor-type\":\"CONTEXT_SENSOR_DEVICE_PROTECTION\"},\n {\"value\":\"Norton Security & Antivirus\",\"key\":\"trustedav\",\"sensor-type\":\"CONTEXT_SENSOR_DEVICE_PROTECTION\"},\n {\"value\":\"CM Security & Find My Phone\",\"key\":\"trustedav\",\"sensor-type\":\"CONTEXT_SENSOR_DEVICE_PROTECTION\"},\n {\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_DEVICE_PROTECTION\"},\n {\"value\":10,\"key\":\"mindistance\",\"sensor-type\":\"CONTEXT_SENSOR_LOCATION\"},\n {\"value\":400,\"key\":\"mindtime\",\"sensor-type\":\"CONTEXT_SENSOR_LOCATION\"},\n {\"value\":12,\"key\":\"radius\",\"sensor-type\":\"CONTEXT_SENSOR_LOCATION\"},\n {\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_LOCATION\"},\n {\"value\":\"/SWE/\",\"key\":\"path\",\"sensor-type\":\"CONTEXT_SENSOR_FILEOBSERVER\"},\n {\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_FILEOBSERVER\"},\n {\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_APP\"},\n {\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_CONNECTIVITY\"},\n {\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_INTERACTION\"},\n {\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_PACKAGE\"},\n {\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_SETTINGS\"},\n {\"value\":true,\"key\":\"enabled\",\"sensor-type\":\"CONTEXT_SENSOR_NOTIFICATION\"}\n ]},\n \"muses-config\":{\n \"config-name\":\"SILENT configuration\",\n \"silent-mode\":true\n },\n \"requesttype\":\"config_update\"}";
    }

    public void testCreateJSON() throws JSONException {
        RequestHolder requestHolder = new RequestHolder(action, actionProperties, contextEvents);
        JSONObject resultJSON = JSONManager.createJSON(deviceId, userName, requestHolder.getId(), requestType, action, actionProperties, contextEvents);
        assertNotNull(resultJSON);

        assertEquals("request type", "update_context_events", resultJSON.getString(JSONIdentifiers.REQUEST_TYPE_IDENTIFIER));

        JSONObject actionJSON = resultJSON.getJSONObject(JSONIdentifiers.ACTION_IDENTIFIER);
        assertNotNull(actionJSON);
        assertEquals("action identifier in JSON :", actionType, actionJSON.getString(JSONIdentifiers.ACTION_TYPE));
        assertEquals("action timestamp:" , String.valueOf(actionTimestamp), actionJSON.getString(JSONIdentifiers.ACTION_TIMESTAMP));

        JSONObject actionPropertiesJSON = actionJSON.getJSONObject(JSONIdentifiers.PROPERTIES_IDENTIFIER);
        assertNotNull(actionPropertiesJSON);
        assertEquals("property: protocol","https", actionPropertiesJSON.getString("protocol"));
        assertEquals("property: url","https://", actionPropertiesJSON.getString("url"));
        assertEquals("property: resourceid","file.png", actionPropertiesJSON.getString("resourceid"));
        assertEquals("property: method","post", actionPropertiesJSON.getString("method"));

        JSONObject sensorJSON = resultJSON.getJSONObject(JSONIdentifiers.SENSOR_IDENTIFIER);
        assertNotNull(sensorJSON);
        JSONObject appSensorJSONObject = sensorJSON.getJSONObject(AppSensor.TYPE);
        assertNotNull(appSensorJSONObject);
        assertEquals("app name:" , "app", appSensorJSONObject.getString(AppSensor.PROPERTY_KEY_APP_NAME));
        assertEquals("app id:" , "1", appSensorJSONObject.getString(AppSensor.PROPERTY_KEY_ID));
        assertEquals("app background processes:" , "process1,process2,process3", appSensorJSONObject.getString(AppSensor.PROPERTY_KEY_BACKGROUND_PROCESS));
    }
    
    public void testCreateUserBehaviorJSON() throws JSONException {
    	JSONObject resultJSON = JSONManager.createUserBehaviorJSON(deviceId, userName, userBehavior);
        assertNotNull(resultJSON);

    	assertEquals("request type", RequestType.USER_ACTION, resultJSON.getString(JSONIdentifiers.REQUEST_TYPE_IDENTIFIER));
    	assertEquals("device id", deviceId, resultJSON.getString(JSONIdentifiers.AUTH_DEVICE_ID));
        assertEquals("user name", userName, resultJSON.getString(JSONIdentifiers.AUTH_USERNAME));
        
        JSONObject userBehaviorJSON = resultJSON.getJSONObject(JSONIdentifiers.USER_BEHAVIOR);
        assertNotNull(userBehaviorJSON);
        assertEquals("user behavior", userBehavior, userBehaviorJSON.getString(JSONIdentifiers.ACTION_IDENTIFIER));
    }
    
    public void testCreateLoginJSON() throws JSONException {
    	JSONObject resultJSON = JSONManager.createLoginJSON(userName, password, deviceId);
    	assertNotNull(resultJSON);
 
    	assertEquals("request type", RequestType.LOGIN, resultJSON.getString(JSONIdentifiers.REQUEST_TYPE_IDENTIFIER));
    	assertEquals("device id", deviceId, resultJSON.getString(JSONIdentifiers.AUTH_DEVICE_ID));
        assertEquals("password", password, resultJSON.getString(JSONIdentifiers.AUTH_PASSWORD));
        assertEquals("userName", userName, resultJSON.getString(JSONIdentifiers.AUTH_USERNAME));
    }
    
    public void testGetRequestType() {
        RequestHolder requestHolder = new RequestHolder(action, actionProperties, contextEvents);
    	// create a test JSON object
        JSONObject testJSON = JSONManager.createJSON(deviceId, userName, requestHolder.getId(), requestType, action, actionProperties, contextEvents);
		assertNotNull(testJSON);
    	
    	assertEquals(requestType, JSONManager.getRequestType(testJSON.toString()));
    }
    
    public void testGetAuthResult() {
    	assertEquals(true, JSONManager.getAuthResult(successfulAuthenticationJSON));
    	assertEquals(false, JSONManager.getAuthResult(unSuccessfulAuthenticationJSON));
    }
    
    public void testGetRequestId() {
    	assertEquals(-1627519220, JSONManager.getRequestId(responseJSON));
    }
    
    public void testIsSilentModeActivated() {
    	assertEquals(true, JSONManager.isSilentModeActivated(configUpdateJSON));
    }
    
    public void testGetSensorConfig() {
    	List<SensorConfiguration> configList = JSONManager.getSensorConfig(configUpdateJSON);
    	assertEquals(18, configList.size());
    	for (SensorConfiguration item : configList) {
			Log.d("JUnit", "key: " + item.getKey() + " value: " + item.getValue() + " sensorType: " + item.getSensorType());
		}
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
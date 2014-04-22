package eu.musesproject.client.contextmonitoring.test;

import eu.musesproject.client.contextmonitoring.sensors.AppSensor;
import eu.musesproject.client.model.JSONIdentifiers;
import eu.musesproject.client.model.RequestType;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.client.usercontexteventhandler.JSONManager;
import eu.musesproject.contextmodel.ContextEvent;
import junit.framework.TestCase;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by christophstanik on 3/12/14.
 */
public class JSONManagerTest extends TestCase {
    private Action action;
    private Map<String, String> actionProperties;
    private List<ContextEvent> contextEvents;

    private String requestType;
    private long actionTimestamp;
    private String actionType;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // create dummy data
        requestType = RequestType.UPDATE_CONTEXT_EVENTS;

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
    }

    public void testCreateJSON() throws JSONException {
        JSONObject resultJSON = JSONManager.createJSON(requestType, action, actionProperties, contextEvents);
        assertNotNull(resultJSON);

        assertEquals("request type", "update_context_events", resultJSON.getString(JSONIdentifiers.REQUEST_TYPE_IDENTIFIER));

        JSONObject actionJSON = resultJSON.getJSONObject(JSONIdentifiers.ACTION_IDENTIFIER);
        assertEquals("action identifier in JSON :", actionType, actionJSON.getString(JSONIdentifiers.ACTION_TYPE));
        assertEquals("action timestamp:" , String.valueOf(actionTimestamp), actionJSON.getString(JSONIdentifiers.ACTION_TIMESTAMP));

        JSONObject actionPropertiesJSON = actionJSON.getJSONObject(JSONIdentifiers.PROPERTIES_IDENTIFIER);
        assertEquals("property: protocol","https", actionPropertiesJSON.getString("protocol"));
        assertEquals("property: url","https://", actionPropertiesJSON.getString("url"));
        assertEquals("property: resourceid","file.png", actionPropertiesJSON.getString("resourceid"));
        assertEquals("property: method","post", actionPropertiesJSON.getString("method"));

        JSONObject sensorJSON = resultJSON.getJSONObject(JSONIdentifiers.SENSOR_IDENTIFIER);
        JSONObject appSensorJSONObject = sensorJSON.getJSONObject(AppSensor.TYPE);
        assertEquals("app name:" , "app", appSensorJSONObject.getString(AppSensor.PROPERTY_KEY_APP_NAME));
        assertEquals("app id:" , "1", appSensorJSONObject.getString(AppSensor.PROPERTY_KEY_ID));
        assertEquals("app background processes:" , "process1,process2,process3", appSensorJSONObject.getString(AppSensor.PROPERTY_KEY_BACKGROUND_PROCESS));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
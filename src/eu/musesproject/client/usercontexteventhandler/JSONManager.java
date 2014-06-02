package eu.musesproject.client.usercontexteventhandler;

import eu.musesproject.client.model.JSONIdentifiers;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.contextmodel.ContextEvent;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author christophstanik
 *
 * Class to transform action and context information to JSON
 */
public class JSONManager {
	/**
	 * creates the JSON object that will be sent to the server via the {@link eu.musesproject.client.connectionmanager.ConnectionManager}
	 * @param requestType {@link eu.musesproject.client.model.RequestType}
     * @param action {@link eu.musesproject.client.model.decisiontable.Action}
	 * @param properties {@link java.util.Map} < String ,  String >
	 * @param contextEvents {@link eu.musesproject.contextmodel.ContextEvent}
	 * @return {@link org.json.JSONObject}
	 */
	public static JSONObject createJSON(String requestType, Action action, Map<String, String> properties, List<ContextEvent> contextEvents) {
		JSONObject root = new JSONObject();
		try {
            /*
             * request type
             */
            // add request type to root
            root.put(JSONIdentifiers.REQUEST_TYPE_IDENTIFIER, requestType);

            /*
             * action
             */
            // create action
            JSONObject actionJSON = new JSONObject();
            if(action != null) {
                actionJSON.put(JSONIdentifiers.ACTION_TYPE, action.getActionType());
                actionJSON.put(JSONIdentifiers.ACTION_TIMESTAMP, action.getTimestamp());

                if(properties != null) {
                    // create action properties
                    JSONObject actionPropertiesJSON = createPropertiesJSONObject(properties);
                    // add properties to action
                    actionJSON.put(JSONIdentifiers.PROPERTIES_IDENTIFIER, actionPropertiesJSON);
                }
            }
            else {
                // if action is null, create the JSON as an update for the server.
                // So it has an default action that says, that there were ContextEvents
                // in the database which will be uploaded now
                actionJSON.put(JSONIdentifiers.ACTION_TYPE, "update"); // hard coded at this moment
                actionJSON.put(JSONIdentifiers.ACTION_TIMESTAMP, System.currentTimeMillis());
            }

			// add action to root
			root.put(JSONIdentifiers.ACTION_IDENTIFIER, actionJSON);

            /*
             * sensor
             */
			// create a JSON object for every sensor
			JSONObject sensorRootJSON = new JSONObject();
			for (ContextEvent contextEvent : contextEvents) {
				sensorRootJSON.put(contextEvent.getType(), createSensorJSONObject(contextEvent));
			}
			// add sensor root JSON with n sensors to root
			root.put(JSONIdentifiers.SENSOR_IDENTIFIER, sensorRootJSON);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return root;
	}

	/**
	 * creates a JSON object for the action properties
	 * @param properties {@link java.util.Map < String ,  String >}
	 * @return {@link org.json.JSONObject}
	 * @throws org.json.JSONException
	 */
	private static JSONObject createPropertiesJSONObject(Map<String, String> properties) throws JSONException {
		JSONObject propertiesJSONObject = new JSONObject();

		for (Entry<String, String> property : properties.entrySet()) {
			propertiesJSONObject.put(property.getKey(), property.getValue());
		}

		return propertiesJSONObject;
	}

	/**
	 * creates a JSON object for a Sensor.
	 * @param contextEvent {@link eu.musesproject.contextmodel.ContextEvent}
	 * @return {@link org.json.JSONObject}
	 * @throws org.json.JSONException
	 */
	private static JSONObject createSensorJSONObject(ContextEvent contextEvent) throws JSONException {
		JSONObject sensorJSONObject = new JSONObject();
		sensorJSONObject.put(ContextEvent.KEY_TYPE, contextEvent.getType());
		sensorJSONObject.put(ContextEvent.KEY_TIMESTAMP, contextEvent.getTimestamp());
		
		for (Entry<String, String> property : contextEvent.getProperties().entrySet()) {
			sensorJSONObject.put(property.getKey(), property.getValue());
		}
		
		return sensorJSONObject;
	}

    /**
     * Method to get a String that contains the {@link eu.musesproject.client.model.RequestType} from the server
     * @param jsonString String. JSON string from the server
     * @return String that contains a {@link eu.musesproject.client.model.RequestType}
     */
    public static String getRequestType(String jsonString) {
        String requestType = null;

        try {
            JSONObject requestJSON = new JSONObject(jsonString);
            requestType = requestJSON.getString(JSONIdentifiers.REQUEST_TYPE_IDENTIFIER);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return requestType;
    }
}
package eu.musesproject.client.contextmonitoring;

/*
 * #%L
 * musesclient
 * %%
 * Copyright (C) 2013 - 2014 HITEC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.HashMap;
import java.util.Map;

import eu.musesproject.client.contextmonitoring.sensors.AppSensor;
import eu.musesproject.client.contextmonitoring.sensors.FileSensor;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.contextmodel.ContextEvent;

/**
 * Created by christophstanik on 4/14/14.
 *
 * Class to create a User action based on context events and to transform a {@link eu.musesproject.client.contextmonitoring.service.aidl.Action}
 * from a MUSES aware app to an {@link eu.musesproject.client.model.decisiontable.Action} that is readable for
 * the decision maker
 */
public class UserActionGenerator {
    /**
     * Method to create a user action based on the fired context events from the Sensors
     * ({@link eu.musesproject.client.contextmonitoring.sensors.ISensor}) by aggregating the context events to higher
     * level context events
     * @param contextEventTrigger the context event that triggered this method
     * @param lastFiredContextEvents all other available context events as well as the last context event of the trigger
     *                               if it exists
     * @return {@link eu.musesproject.client.model.decisiontable.Action}
     */
    public static Action createUserAction(ContextEvent contextEventTrigger, Map<String, ContextEvent> lastFiredContextEvents) {
        Action action = new Action();
        if(contextEventTrigger.getType().equals(AppSensor.TYPE)) {
        	action.setTimestamp(System.currentTimeMillis());
        	action.setActionType(ActionType.OPEN_APPLICATION);
        	return action;
        }
        else if(contextEventTrigger.getType().equals(FileSensor.TYPE)) {
        	if(contextEventTrigger.getProperties().get(FileSensor.PROPERTY_KEY_FILE_EVENT).equals(FileSensor.OPEN)) {
            	action.setTimestamp(System.currentTimeMillis());
            	action.setActionType(ActionType.OPEN_APPLICATION);
            	return action;
        	}
        }

        return null;
    }

	public static Map<String, String> createUserActionProperties(ContextEvent contextEventTrigger) {
		Map<String, String> properties = new HashMap<String, String>();
        if(contextEventTrigger.getType().equals(AppSensor.TYPE)) {
        	properties.put(AppSensor.PROPERTY_KEY_APP_NAME, contextEventTrigger.getProperties().get(AppSensor.PROPERTY_KEY_APP_NAME));
        	properties.put("package", "");
        	properties.put("version", "");
        	if(!properties.get(AppSensor.PROPERTY_KEY_APP_NAME).equals("Gmail")) {
//    		if(!properties.get(AppSensor.PROPERTY_KEY_APP_NAME).equals("Calendar")) {
        		return null;
        	}
        }
        else if(contextEventTrigger.getType().equals(FileSensor.TYPE)) {
        	if(contextEventTrigger.getProperties().get(FileSensor.PROPERTY_KEY_FILE_EVENT).equals(FileSensor.OPEN)) {
            	properties.put("resourceName", "");
            	properties.put("resourceType", "");
            	properties.put("resourcePath", contextEventTrigger.getProperties().get(FileSensor.PROPERTY_KEY_PATH));
        	}
        }
		return properties;
	}

    /**
     * Method to transform a {@link eu.musesproject.client.contextmonitoring.service.aidl.Action} from a MUSES
     * aware application to an {@link eu.musesproject.client.model.decisiontable.Action}
     * @return {@link eu.musesproject.client.model.decisiontable.Action}
     */
    public static Action transformUserAction(eu.musesproject.client.contextmonitoring.service.aidl.Action musesAwareAction) {
        Action transformedAction = new Action();
        transformedAction.setActionType(transformToActionType(musesAwareAction.getType()));
        transformedAction.setTimestamp(musesAwareAction.getTimestamp());

        return transformedAction;
    }

    private static String transformToActionType(String type) {
        if(type.equals(ActionType.ACCESS)) {
            return ActionType.ACCESS;
        }
        else if(type.equals(ActionType.DELETE)) {
            return ActionType.DELETE;
        }
        else if(type.equals(ActionType.INSTALL)) {
            return ActionType.INSTALL;
        }
        else if(type.equals(ActionType.OPEN)) {
            return ActionType.OPEN;
        }
        else if(type.equals(ActionType.RUN)) {
            return ActionType.RUN;
        }
        else if(type.equals(ActionType.SEND)) {
            return ActionType.SEND;
        }
        else if(type.equals(ActionType.OK)) {
            return ActionType.OK;
        }
        else if(type.equals(ActionType.CANCEL)) {
            return ActionType.CANCEL;
        }
        else if(type.equals(ActionType.OPEN_ASSET)) {
        	return ActionType.OPEN_ASSET;
        }
        else {
            return null;
        }
    }
}
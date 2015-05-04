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

import eu.musesproject.client.contextmonitoring.sensors.*;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.contextmodel.ContextEvent;
import eu.musesproject.contextmodel.PackageStatus;

import java.util.HashMap;
import java.util.Map;

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
     * @return {@link eu.musesproject.client.model.decisiontable.Action}
     */
    public static Action createUserAction(ContextEvent contextEventTrigger) {
        Action action = new Action();
        String type = contextEventTrigger.getType();
        if(type.equals(AppSensor.TYPE)) {
        	action.setTimestamp(System.currentTimeMillis());
        	action.setActionType(ActionType.OPEN_APPLICATION);
        	return action;
        }
        else if(type.equals(RecursiveFileSensor.TYPE)) {
            Map<String, String> prop = contextEventTrigger.getProperties();
        	if(prop.get(RecursiveFileSensor.PROPERTY_KEY_FILE_EVENT).equals(RecursiveFileSensor.OPEN)) {
            	action.setTimestamp(System.currentTimeMillis());
            	action.setActionType(ActionType.OPEN_ASSET);
            	return action;
        	}
        	else if(prop.get(RecursiveFileSensor.PROPERTY_KEY_FILE_EVENT).equals(RecursiveFileSensor.DELETE)) {
            	action.setTimestamp(System.currentTimeMillis());
            	action.setActionType(ActionType.DELETE_ASSET);
            	return action;
        	}
        	else if(prop.get(RecursiveFileSensor.PROPERTY_KEY_FILE_EVENT).equals(RecursiveFileSensor.CREATE)) {
            	action.setTimestamp(System.currentTimeMillis());
            	action.setActionType(ActionType.CREATE_ASSET);
            	return action;
        	}
        	else if(prop.get(RecursiveFileSensor.PROPERTY_KEY_FILE_EVENT).equals(RecursiveFileSensor.CLOSE_WRITE)) {
            	action.setTimestamp(System.currentTimeMillis());
            	action.setActionType(ActionType.SAVE_ASSET);
            	return action;
        	}
        	else if(prop.get(RecursiveFileSensor.PROPERTY_KEY_FILE_EVENT).equals(RecursiveFileSensor.MODIFY) ||
                    prop.get(RecursiveFileSensor.PROPERTY_KEY_FILE_EVENT).equals(RecursiveFileSensor.MOVE_SELF) ||
                    prop.get(RecursiveFileSensor.PROPERTY_KEY_FILE_EVENT).equals(RecursiveFileSensor.MOVED_FROM) ||
                    prop.get(RecursiveFileSensor.PROPERTY_KEY_FILE_EVENT).equals(RecursiveFileSensor.MOVED_TO) ) {
            	action.setTimestamp(System.currentTimeMillis());
            	action.setActionType(ActionType.MODIFY_ASSET);
            	return action;
        	}
        }
        else if(contextEventTrigger.getType().equals(DeviceProtectionSensor.TYPE)) {
    		action.setTimestamp(System.currentTimeMillis());
    		action.setActionType(ActionType.SECURITY_PROPERTY_CHANGED);
    		return action;
        }
        else if(contextEventTrigger.getType().equals(PackageSensor.TYPE)) {
        	if(contextEventTrigger.getProperties().containsKey(PackageSensor.PROPERTY_KEY_PACKAGE_STATUS)) {
	        	if(contextEventTrigger.getProperties().get(PackageSensor.PROPERTY_KEY_PACKAGE_STATUS)
	        			.equalsIgnoreCase(PackageStatus.REMOVED.toString())) {
	        		action.setTimestamp(System.currentTimeMillis());
	        		action.setActionType(ActionType.UNINSTALL);
	        		return action;
	        	}
	        	else if(contextEventTrigger.getProperties().get(PackageSensor.PROPERTY_KEY_PACKAGE_STATUS)
	        			.equalsIgnoreCase(PackageStatus.INSTALLED.toString())) {
	        		action.setTimestamp(System.currentTimeMillis());
	        		action.setActionType(ActionType.INSTALL);
	        		return action;
	        	}
        	}
        }
        else if(contextEventTrigger.getType().equals(PeripheralSensor.TYPE)) {
            action.setTimestamp(contextEventTrigger.getTimestamp());
            action.setActionType(ActionType.USB_DEVICE_CONNECTED);
        }

        return null;
    }

	public static Map<String, String> createUserActionProperties(ContextEvent contextEventTrigger) {
		Map<String, String> properties = new HashMap<String, String>();
        if(contextEventTrigger.getType().equals(AppSensor.TYPE)) {
        	properties.put(AppSensor.PROPERTY_KEY_APP_NAME, contextEventTrigger.getProperties().get(AppSensor.PROPERTY_KEY_APP_NAME));
        	properties.put(AppSensor.PROPERTY_KEY_PACKAGE_NAME, contextEventTrigger.getProperties().get(AppSensor.PROPERTY_KEY_PACKAGE_NAME));
        	properties.put("package", "");
        	properties.put("version", "");
        }
        else if(contextEventTrigger.getType().equals(RecursiveFileSensor.TYPE)) {
        	contextEventTrigger.addProperty("resourceType", "null");
        	return contextEventTrigger.getProperties();
        }
        else {
        	return contextEventTrigger.getProperties();
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
        transformedAction.setMusesAwareAppRequiresResponse(musesAwareAction.isMusesAwareAppRequiresResponse());

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
        else if(type.equals(ActionType.SEND_MAIL)) {
            return ActionType.SEND_MAIL;
        }
        else if(type.equals(ActionType.OK)) {
            return ActionType.OK;
        }
        else if(type.equals(ActionType.CANCEL)) {
            return ActionType.CANCEL;
        }
        else if(type.equals(ActionType.VIRUS_FOUND)) {
        	return ActionType.VIRUS_FOUND;
        }
        else if(type.equals(ActionType.VIRUS_CLEANED)) {
        	return ActionType.VIRUS_CLEANED;
        }
        else if(type.equals(ActionType.ENCRYPT_EVENT)) {
        	return ActionType.ENCRYPT_EVENT;
        }
        else if(type.equals(ActionType.UPDATE)) {
        	return ActionType.UPDATE;
        }
        else if(type.equals(ActionType.SECURITY_PROPERTY_CHANGED)) {
        	return ActionType.SECURITY_PROPERTY_CHANGED;
        }
        else if(type.equals(ActionType.FILE_ATTACHED)) {
        	return ActionType.FILE_ATTACHED;
        }
        else if(type.equals(ActionType.OPEN_ASSET)) {
            return ActionType.OPEN_ASSET;
        }
        else if(type.equals(ActionType.MODIFY_ASSET)) {
        	return ActionType.MODIFY_ASSET;
        }
        else if(type.equals(ActionType.SAVE_ASSET)) {
        	return ActionType.SAVE_ASSET;
        }
        else {
            return null;
        }
    }
}
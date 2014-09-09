package eu.musesproject.client.db.handler;

import java.util.Map;

import eu.musesproject.client.contextmonitoring.sensors.AppSensor;
import eu.musesproject.client.contextmonitoring.sensors.InteractionSensor;
import eu.musesproject.client.model.contextmonitoring.MailProperties;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.client.model.decisiontable.Resource;
import eu.musesproject.client.model.decisiontable.ResourceType;

public class ResourceCreator {

	public static Resource create(Action action, Map<String, String> properties) {
		Resource res = new Resource();
		ResourceType resType = new ResourceType();
		if(action.getActionType() != null) {
			if(action.getActionType().equals(ActionType.OPEN_ASSET)) {
				resType.setName(ResourceType.FILE);
				res.setResourceType(resType);
				res.setDescription(properties.get("resourceName"));
				res.setPath(properties.get("resourcePath"));
			}
			else if(action.getActionType().equals(ActionType.OPEN_APPLICATION)) {
				resType.setName(ResourceType.APP);
				res.setResourceType(resType);
				res.setDescription(properties.get(AppSensor.PROPERTY_KEY_APP_NAME));
			}
			else if(action.getActionType().equals(ActionType.SEND_MAIL)) {
				resType.setName(ResourceType.FILE);
				properties.get(MailProperties.PROPERTY_KEY_ATTACHMENT_COUNT);
			}
			else if(action.getActionType().equals(ActionType.VIRUS_FOUND)) {
			}
		}
		
		return res;
	}
}
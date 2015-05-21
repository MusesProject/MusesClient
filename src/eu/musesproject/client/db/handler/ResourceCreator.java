package eu.musesproject.client.db.handler;

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


import eu.musesproject.client.contextmonitoring.sensors.AppSensor;
import eu.musesproject.client.model.contextmonitoring.MailProperties;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.client.model.decisiontable.Resource;
import eu.musesproject.client.model.decisiontable.ResourceType;

import java.util.Map;

public class ResourceCreator {

	public static Resource create(Action action, Map<String, String> properties) {
		Resource res = new Resource();
		ResourceType resType = new ResourceType();
		if(action.getActionType() != null) {
			if(action.getActionType().equals(ActionType.OPEN_ASSET)) {
				resType.setName(ResourceType.FILE);
				res.setResourceType(resType);
				res.setDescription(properties.get("resourceName"));
				res.setPath(properties.get("path"));
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
			else if(action.getActionType().equals(ActionType.VIRUS_CLEANED)) {
				resType.setName(properties.get("name"));
				res.setResourceType(resType);
				res.setSeverity(properties.get("severity"));
				res.setType(properties.get("clean_type"));
				res.setPath(properties.get("path"));
				res.setName(properties.get("name"));
			}
		}
		
		return res;
	}
}
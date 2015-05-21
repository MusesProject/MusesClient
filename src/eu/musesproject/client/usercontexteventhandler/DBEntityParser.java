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

import eu.musesproject.client.db.entity.ActionProperty;
import eu.musesproject.client.db.entity.ContextEvent;
import eu.musesproject.client.db.entity.Property;
import eu.musesproject.client.model.decisiontable.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by christophstanik on 4/22/14.
 */
public class DBEntityParser {
    /**
     * Method to create an {@link eu.musesproject.client.db.entity.ContextEvent} db entity object
     * @param oldEvent {@link eu.musesproject.contextmodel.ContextEvent}
     * @return {@link eu.musesproject.client.db.entity.ContextEvent} db entity object
     */
    public static ContextEvent transformContextEvent(int actionId, eu.musesproject.contextmodel.ContextEvent oldEvent) {
        ContextEvent contextEvent = new ContextEvent();
        contextEvent.setId(0);
        contextEvent.setActionId(actionId);
        contextEvent.setType(oldEvent.getType());
        contextEvent.setTimestamp(oldEvent.getTimestamp());

        return contextEvent;
    }

    /**
     * Method to create a List of {@link eu.musesproject.client.db.entity.Property}s based on the Map of Properties in
     * the {@link eu.musesproject.contextmodel.ContextEvent}
     * @param contextEventId long id of the related {@link eu.musesproject.client.db.entity.ContextEvent} in the db
     * @param oldEvent {@link eu.musesproject.contextmodel.ContextEvent}
     * @return {@link java.util.List} that contains {@link eu.musesproject.client.db.entity.Property}
     */
    public static List<Property> transformProperty(long contextEventId, eu.musesproject.contextmodel.ContextEvent oldEvent) {
        List<Property> properties = new ArrayList<Property>();

        for(Entry<String, String> entry : oldEvent.getProperties().entrySet()) {
            Property property = new Property();
            property.setId(0);
            property.setContextevent_id((int) contextEventId);
            property.setKey(entry.getKey());
            property.setValue(String.valueOf(entry.getValue()));

            properties.add(property);
        }

        return properties;
    }


    public static Map<String, String> transformActionPropertyToMap(List<ActionProperty> entityActionProperties) {
        Map<String, String> properties = new HashMap<String, String>();

        for(ActionProperty actionProperty : entityActionProperties) {
            properties.put(actionProperty.getKey(), actionProperty.getValue());
        }

        return properties;
    }

    /**
     * Method that transform an {@link eu.musesproject.client.db.entity.Action} object from the database
     * to an {@link eu.musesproject.client.model.decisiontable.Action} object that can be send to the server
     * @param entityAction {@link eu.musesproject.client.db.entity.Action} entity object from the database
     * @return {@link eu.musesproject.client.model.decisiontable.Action}
     */
    public static Action transformAction(eu.musesproject.client.db.entity.Action entityAction) {
        Action action = new Action();
        action.setId(entityAction.getId());
        action.setActionType(entityAction.getActionType());
        action.setDescription(entityAction.getDescription());
        action.setTimestamp(entityAction.getTimestamp());

        return action;
    }

    /**
     * Method that transform an {@link eu.musesproject.client.model.decisiontable.Action} object to an
     * {@link eu.musesproject.client.db.entity.Action} entity object for the database
     * @param action {@link eu.musesproject.client.model.decisiontable.Action}
     * @return {@link eu.musesproject.client.db.entity.Action} entity object for the database
     */
    public static eu.musesproject.client.db.entity.Action transformActionToEntityAction(Action action) {
        eu.musesproject.client.db.entity.Action entityAction = new eu.musesproject.client.db.entity.Action();
        entityAction.setId(action.getId());
        entityAction.setActionType(action.getActionType());
        entityAction.setDescription(action.getDescription());
        entityAction.setTimestamp(action.getTimestamp());

        return entityAction;
    }

    public static eu.musesproject.contextmodel.ContextEvent transformEntityContextEvent(ContextEvent entityContextEvent) {
        eu.musesproject.contextmodel.ContextEvent contextEvent = new eu.musesproject.contextmodel.ContextEvent();
        contextEvent.setId(entityContextEvent.getId());
        contextEvent.setTimestamp(entityContextEvent.getTimestamp());
        contextEvent.setType(entityContextEvent.getType());

        return contextEvent;
    }
}
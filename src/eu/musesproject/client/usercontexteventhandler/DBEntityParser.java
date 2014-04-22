package eu.musesproject.client.usercontexteventhandler;

import eu.musesproject.client.db.entity.ContextEvent;
import eu.musesproject.client.db.entity.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by christophstanik on 4/22/14.
 */
public class DBEntityParser {
    /**
     * Method to create an {@link eu.musesproject.client.db.entity.ContextEvent} db entity object
     * @param oldEvent {@link eu.musesproject.contextmodel.ContextEvent}
     * @return {@link eu.musesproject.client.db.entity.ContextEvent} db entity object
     */
    public static ContextEvent transformContextEvent(eu.musesproject.contextmodel.ContextEvent oldEvent) {
        ContextEvent contextEvent = new ContextEvent();
        contextEvent.setId(0);
        contextEvent.setType(oldEvent.getType());
        contextEvent.setTimestamp(oldEvent.getType());
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

        Map<String, HashMap> entries = new HashMap<String, HashMap>();

        for(Map.Entry<String, HashMap> entry : entries.entrySet()) {
            Property property = new Property();
            property.setId(0);
            property.setContextevent_id((int) contextEventId);
            property.setKey(entry.getKey());
            property.setValue(String.valueOf(entry.getValue()));

            properties.add(property);
        }
        return properties;
    }
}
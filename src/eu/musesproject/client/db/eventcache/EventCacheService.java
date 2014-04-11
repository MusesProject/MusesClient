package eu.musesproject.client.db.eventcache;

import eu.musesproject.contextmodel.ContextEvent;

/**
 * 
 * 
 * @author zardosht
 *
 */
public interface EventCacheService {
	
	void writeEvent(ContextEvent contextEvent);
	ContextEvent[] retrieveContextEvents(String query);
	void clearCache();
	long getCacheSize();

}

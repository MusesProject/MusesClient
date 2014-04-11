package eu.musesproject.client.usercontexteventhandler;

import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.contextmodel.CompositeContextEvent;
import eu.musesproject.contextmodel.ContextEvent;

/**
 * @author zardosht
 *
 */
public interface ContextAggregator extends ContextListener {
	
	CompositeContextEvent aggreagateEvents(ContextEvent[] contextEvents);

}

/**
 * 
 */
package eu.musesproject.client.contextmonitoring;

import eu.musesproject.contextmodel.ContextEvent;


/**
 * @author zardosht
 *
 */
public interface ContextListener {
	void onEvent(ContextEvent contextEvent);
}
/**
 * 
 */
package eu.musesproject.client.usercontexteventhandler;

import eu.musesproject.client.contextmonitoring.ContextListener;


/**
 * @author zardosht
 *
 */
public interface ContextBus {
	
	void addListener(ContextListener contextListner);
	void removeListener(ContextListener contextListener);

}

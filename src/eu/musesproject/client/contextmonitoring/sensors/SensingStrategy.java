/**
 * 
 */
package eu.musesproject.client.contextmonitoring.sensors;

import eu.musesproject.contextmodel.ContextEvent;

/**
 * @author zardosht
 *
 */
public interface SensingStrategy {
	
	ContextEvent sense(ISensor iSensor);

}

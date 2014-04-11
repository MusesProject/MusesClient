/**
 * 
 */
package eu.musesproject.client.actuators;

import eu.musesproject.contextmodel.ContextElement;
import eu.musesproject.contextmodel.ContextProperty;



/**
 * @author zardosht
 *
 */
public interface ComputingEnvironmentContextActuator extends Actuator {

	
	void changeContext(ContextElement contextElement, ContextProperty iContextProperty);
}

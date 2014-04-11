/**
 * 
 */
package eu.musesproject.client.actuators;

import java.net.URI;

import eu.musesproject.client.model.actuators.ActuatorInfo;

/**
 * @author zardosht
 *
 */
public interface ActuatorRegistry {
	
	void installActuator(URI packageURI);
	void uninstallActuator(Actuator actuator);
	void registerActuator(Actuator actuator);
	void deregisterActuator(Actuator actuator);
	ActuatorInfo getActuatorInfo(Actuator actuator);

}

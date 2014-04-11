/**
 * 
 */
package eu.musesproject.client.actuators;

import eu.musesproject.client.model.actuators.ActuatorConfiguration;
import eu.musesproject.client.model.actuators.ActuatorStatus;


/**
 * @author zardosht
 *
 */
public interface ActuatorManager {
	
	void enableActuator(Actuator actuator);
	void disableActuator(Actuator actuator);
	void configureActuator(ActuatorConfiguration actuatorConfiguration);
	ActuatorStatus getActuatorStatus(Actuator actuator);

}

/**
 * 
 */
package eu.musesproject.client.actuators;

import eu.musesproject.client.model.actuators.ActuatorStatus;



/**
 * @author zardosht
 *
 */
public interface ActuatorViewProvider {
	
	Actuator[] listActuators();
	Actuator[] listActiveActuators();
	ActuatorStatus getActuatorStatus(Actuator actuator);

}

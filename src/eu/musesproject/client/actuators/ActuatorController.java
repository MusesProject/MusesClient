/**
 * 
 */
package eu.musesproject.client.actuators;

import eu.musesproject.client.model.actuators.ActuatorInstruction;
import eu.musesproject.client.model.actuators.RiskCommunication;
import eu.musesproject.client.model.actuators.RiskTreatment;
import eu.musesproject.client.model.decisiontable.Decision;


/**
 * @author zardosht
 *
 */
public interface ActuatorController {
	Actuator[] findActuators(Decision decision, RiskCommunication riskCommunication, RiskTreatment riskTreatment);
	ActuatorInstruction[] getActuatorInstructions(RiskTreatment riskTreatment);
	void sendInstructions(Actuator actuator, ActuatorInstruction[] instructions);
}
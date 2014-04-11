/**
 * 
 */
package eu.musesproject.client.actuators;

import eu.musesproject.client.model.actuators.ActuatorInstruction;
import eu.musesproject.contextmodel.AppFeature;
import eu.musesproject.contextmodel.AppOperation;


/**
 * @author zardosht
 *
 */
public class AppActuator implements Actuator, TaskContextActuator {

	
	@Override
	public void perform(ActuatorInstruction instruction) {

	}
	
	public void blockOperation(AppOperation operation){
		
	}
	
	public void suspendOperation(AppOperation operation){
		
	}
	
	public void disableFeature(AppFeature feature){
		
	}
	
	public void enableFeature(AppFeature feature){
		
	}
	

}

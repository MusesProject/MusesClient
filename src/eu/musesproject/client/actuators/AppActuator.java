/**
 * 
 */
package eu.musesproject.client.actuators;

/*
 * #%L
 * musesclient
 * %%
 * Copyright (C) 2013 - 2014 HITEC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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

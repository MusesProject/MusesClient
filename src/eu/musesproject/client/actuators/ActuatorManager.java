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

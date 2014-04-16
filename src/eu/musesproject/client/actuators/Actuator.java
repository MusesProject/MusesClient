/*
 * MUSES High-Level Object Oriented Model
 * Copyright MUSES project (European Commission FP7) - 2013 
 */
package eu.musesproject.client.actuators;

import eu.musesproject.client.model.actuators.ActuatorInstruction;


/**
 * The Class Actuator. It represents the highest-level abstraction of an actuator. test
 * 
 * @author Jean-Marc Seigneur (UNIGE)
 * @version 2 avr. 2013
 */
public interface Actuator {
	void perform(ActuatorInstruction instruction);
}
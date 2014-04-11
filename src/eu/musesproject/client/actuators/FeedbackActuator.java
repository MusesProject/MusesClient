package eu.musesproject.client.actuators;

import eu.musesproject.client.model.actuators.Feedback;


public interface FeedbackActuator extends Actuator{
	
	
	void showFeedback(Feedback feedback);

}

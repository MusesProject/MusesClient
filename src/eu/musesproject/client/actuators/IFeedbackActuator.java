package eu.musesproject.client.actuators;


import eu.musesproject.client.model.decisiontable.Decision;

public interface IFeedbackActuator extends Actuator {
	void showFeedback(Decision decision);
}
/**
 * 
 */
package eu.musesproject.client.actuators;


import eu.musesproject.client.model.decisiontable.Decision;
import eu.musesproject.client.usercontexteventhandler.UserContextEventHandler;

/**
 * @author christophstanik
 *
 * Class that manages the lifecycle of the actuators
 */
public class ActuatorController implements IActuatorController {
    private static ActuatorController actuatorController = null;
    private IUICallback callback;
    private final UserContextEventHandler uceHandler = UserContextEventHandler.getInstance();

    private FeedbackActuator feedbackActuator;

    public ActuatorController() {
        this.feedbackActuator = new FeedbackActuator();
    }

    public static ActuatorController getInstance() {
        if (actuatorController == null) {
            actuatorController = new ActuatorController();
        }
        return actuatorController;
    }

    public void showFeedback(Decision decision) {
        feedbackActuator.showFeedback(decision);
    }

    public void sendLoginResponse(boolean loginResponse) {
        feedbackActuator.sendLoginResponseToUI(loginResponse);
    }

    @Override
    public void registerCallback(IUICallback callback) {
        this.callback = callback;
        feedbackActuator.registerCallback(callback);
    }

    @Override
    public void unregisterCallback(IUICallback callback) {
        this.callback = callback;
        feedbackActuator.unregisterCallback(callback);
    }
}
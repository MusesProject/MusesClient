package eu.musesproject.client.actuators;

import android.util.Log;
import eu.musesproject.client.model.actuators.ActuatorInstruction;
import eu.musesproject.client.model.decisiontable.Decision;

/**
 * Created by christophstanik on 4/15/14.
 *
 * Class to send feedback to MUSES UI
 * Feedback types:
 *  - login successful
 *  - login unsuccessful
 *  - show feedback based on a {@link eu.musesproject.client.model.decisiontable.Decision}
 */
public class FeedbackActuator implements IFeedbackActuator {
    private static final String TAG = FeedbackActuator.class.getSimpleName();

    private IUICallback callback;

    @Override
    public void showFeedback(Decision decision) {
        Log.e(TAG, "called: showFeedback(Decision decision)");
        if(callback != null) {
            if(decision.getName().equals(Decision.GRANTED_ACCESS)){
                callback.onAccept();
            }
            else if(decision.getName().equals(Decision.MAYBE_ACCESS_WITH_RISKTREATMENTS)) {
                callback.onMaybe(decision);
            }
            else if(decision.getName().equals(Decision.UPTOYOU_ACCESS_WITH_RISKCOMMUNICATION)) {
                callback.onUpToUser(decision);
            }
            else if(decision.getName().equals(Decision.STRONG_DENY_ACCESS)) {
                callback.onDeny(decision);
            }
        }
    }

    @Override
    public void perform(ActuatorInstruction instruction) {

    }

    public void sendLoginResponseToUI(boolean result) {
        Log.d(TAG, "called: sendLoginResponseToUI(boolean result)");
        this.callback.onLogin(result);
    }

    public void registerCallback(IUICallback callback) {
        this.callback = callback;
    }

    public void unregisterCallback(IUICallback callback) {
        this.callback = callback;
    }
}
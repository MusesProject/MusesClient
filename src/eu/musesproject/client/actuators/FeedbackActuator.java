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
        if(callback != null && decision.getName() != null) {
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
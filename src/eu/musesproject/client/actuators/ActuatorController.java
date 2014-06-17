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


import android.util.Log;
import eu.musesproject.client.model.decisiontable.Decision;
import eu.musesproject.client.usercontexteventhandler.UserContextEventHandler;

/**
 * @author christophstanik
 *
 * Class that manages the lifecycle of the actuators
 */
public class ActuatorController implements IActuatorController {
    private static final String TAG = ActuatorController.class.getSimpleName();

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
        Log.d(TAG, "called: showFeedback(Decision decision)");
        feedbackActuator.showFeedback(decision);
    }

    public void sendLoginResponse(boolean loginResponse) {
        Log.d(TAG, "called: sendLoginResponse(boolean loginResponse)");
        feedbackActuator.sendLoginResponseToUI(loginResponse);
    }

    public void sendResponseToMusesAwareApp() {

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
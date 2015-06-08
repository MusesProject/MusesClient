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

import android.content.Context;
import android.util.Log;
import eu.musesproject.client.db.handler.DBManager;
import eu.musesproject.client.model.actuators.ActuationInformationHolder;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.Decision;
import eu.musesproject.client.usercontexteventhandler.UserContextEventHandler;

import java.util.Map;

/**
 * @author christophstanik
 *
 * Class that manages the lifecycle of the actuators
 */
public class ActuatorController implements IActuatorController {
    private static final String TAG = ActuatorController.class.getSimpleName();

    private static ActuatorController actuatorController = null;

    private Context context;
    private IUICallback callback;
    private final UserContextEventHandler uceHandler = UserContextEventHandler.getInstance();

    private FeedbackActuator feedbackActuator;
    private ActuatorCommandAPI actuateCMD;
    /** Integer = decisionId*/
    private Map<Integer, ActuationInformationHolder> holderMap;


    private DBManager dbManager;

    public ActuatorController(Context context) {
        this.context = context;
        this.feedbackActuator = new FeedbackActuator(context);
        this.actuateCMD = new ActuatorCommandAPI(context);
        this.dbManager = new DBManager(uceHandler.getContext());
    }

    public static ActuatorController getInstance(Context context) {
        if (actuatorController == null) {
            actuatorController = new ActuatorController(context);
        }
        return actuatorController;
    }

    public void showFeedback(Decision decision, Action action, Map<String, String> properties) {
        Log.d(TAG, "called: showFeedback(Decision decision)");

        //check for silent mode
        dbManager.closeDB();
        dbManager.openDB();
        boolean isSilentModeActive = dbManager.isSilentModeActive();
        dbManager.closeDB();
		if(decision != null && isSilentModeActive) {
        	decision.setName(Decision.GRANTED_ACCESS);
        }

		// show feedback
        feedbackActuator.showFeedback(decision);
    }

    public void showCurrentTopFeedback() {
        feedbackActuator.showCurrentTopFeedback();
    }

    public void removeFeedbackFromQueue() {
        feedbackActuator.removeFeedbackFromQueue();
    }

    public void sendFeedbackToMUSESAwareApp(Decision decision) {
        feedbackActuator.sendFeedbackToMUSESAwareApp(decision, context);
    }

    public void sendLoginResponse(boolean loginResponse, String msg, int detailedStatus) {
        Log.d(TAG, "called: sendLoginResponse(boolean loginResponse)");
        feedbackActuator.sendLoginResponseToUI(loginResponse, msg, detailedStatus);
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
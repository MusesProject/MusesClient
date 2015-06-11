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
import eu.musesproject.server.risktrust.SolvingRiskTreatment;

import java.util.HashMap;
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
    /** String = decisionId*/
    private Map<String, ActuationInformationHolder> holderMap;


    private DBManager dbManager;

    public ActuatorController(Context context) {
        this.context = context;
        this.feedbackActuator = new FeedbackActuator(context);
        this.actuateCMD = new ActuatorCommandAPI(context);
        this.dbManager = new DBManager(uceHandler.getContext());
        this.holderMap = new HashMap<String, ActuationInformationHolder>();
    }

    public static ActuatorController getInstance(Context context) {
        if (actuatorController == null) {
            actuatorController = new ActuatorController(context);
        }
        return actuatorController;
    }

    public void showFeedback(ActuationInformationHolder holder) {
        Log.d(TAG, "called: showFeedback(Decision decision)");
        holderMap.put(holder.getDecision().getDecision_id(), holder);

        //check for silent mode
        dbManager.closeDB();
        dbManager.openDB();
        boolean isSilentModeActive = dbManager.isSilentModeActive();
        dbManager.closeDB();
		if(holder.getDecision() != null) {
            if(isSilentModeActive) {
                holder.getDecision().setName(Decision.GRANTED_ACCESS);
            }
            // show feedback
            feedbackActuator.showFeedback(holder.getDecision());
        }
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

    @Override
    public void perform(String decisionID) {
        Log.d(TAG, "1. perform("+decisionID+")");
        if(actuateCMD == null) {
            actuateCMD = new ActuatorCommandAPI(context);
        }
        ActuationInformationHolder holder = holderMap.get(decisionID);
        if(holder != null) {
            Log.d(TAG, "2. holder exists");
            Decision decision = holder.getDecision();
            Action action = holder.getAction();
            Map<String, String> properties = holder.getActionProperties();

            if(decision != null && action != null && properties != null) {
                Log.d(TAG, "3. decision != null && action != null && properties != null");
                Log.d(TAG, "4. solving int="+ decision.getSolving_risktreatment());
                switch (decision.getSolving_risktreatment()) {
                    case SolvingRiskTreatment.VIRUS_FOUND:
                        // 1. search for the installed trusted antivirus
                        // 2. start the trusted antivirus
                        context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.avast.android.mobilesecurity"));
                        break;
                    case SolvingRiskTreatment.UNSECURE_NETWORK:
                        break;
                    case SolvingRiskTreatment.ATTEMPT_TO_SAVE_A_FILE_IN_A_MONITORED_FOLDER:
                        break;
                    case SolvingRiskTreatment.ANTIVIRUS_IS_NOT_RUNNING:
                        // 1. search for the installed trusted antivirus
                        // 2. start the trusted antivirus
                        context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.avast.android.mobilesecurity"));
                        break;
                    case SolvingRiskTreatment.UNSECURE_WIFI_ENCRYPTION_WITHOUT_WPA2:
                        break;
                    case SolvingRiskTreatment.INSUFFICIENT_SCREEN_LOOK_TIMEOUT:
                        // 10 min hardcoded default value, since the information is not available from the server
                        Log.d(TAG, "5. set screen timeout");
                        actuateCMD.setScreenTimeOut(15000);
                        break;
                    case SolvingRiskTreatment.BLUETOOTH_ENABLED_MIGHT_TURN_INTO_LEAKAGE_PROBLEMS:
                        actuateCMD.disableBluetooth();
                        break;
                }
            }
        }
    }
}
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
import android.content.Intent;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;
import eu.musesproject.client.contextmonitoring.service.aidl.DummyCommunication;
import eu.musesproject.client.model.actuators.ResponseInfoAP;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.Decision;
import eu.musesproject.client.ui.DialogController;
import eu.musesproject.client.ui.NotificationController;

import java.util.LinkedList;
import java.util.Queue;

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
	private static final String APP_TAG = "APP_TAG";

    private static IUICallback callback;

    private Context context;

    Queue<Decision> decisionQueue;

    public FeedbackActuator(Context context) {
        this.context = context;
        decisionQueue = new LinkedList<Decision>();

        NotificationController.getInstance(context).create(decisionQueue.size());
    }

    @Override
    public void showFeedback(Decision decision) {
        Log.d(TAG, "called: showFeedback(Decision decision)");
        if(decision != null && decision.getName() != null) {
            try {
                // check if the decision is already in the queue for the feedback dialogs,
                // in order to avoid duplicate entries
                for (Decision bufferedDecision: decisionQueue) {
                    if(bufferedDecision.getRiskCommunication().getRiskTreatment()[0].getTextualDescription().equals(
                            decision.getRiskCommunication().getRiskTreatment()[0].getTextualDescription())) {
                        Log.d(TAG, "duplicate found");
                        return; // do not add a duplicate feedback
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            decisionQueue.add(decision);
            Log.d(TAG, "new feedback dialog request; queue size:" + decisionQueue.size() + " " + decision.getRiskCommunication().getRiskTreatment()[0].getTextualDescription());

            // just show a new dialog if there is no other currently displayed
            if (decisionQueue.size() == 1) {
                createFeedbackDialog(decision);
            }

            // update the notification bar to visualize if there are dialogs/messages or not
            NotificationController.getInstance(context).create(decisionQueue.size());
        }
    }

    private void showNextFeedback(Decision decision) {
        Log.d(TAG, "showNextFeedback " + decision.getRiskCommunication().getRiskTreatment()[0].getTextualDescription());
        createFeedbackDialog(decision);
    }

    private void createFeedbackDialog(Decision decision) {
        Log.d(TAG, "Info U, Actuator -> FeedbackActuator showing feedback with decision:  " + decision.getName());

        String decisionId = decision.getDecision_id();
        String dialogBody = decision.getRiskCommunication().getRiskTreatment()[0].getTextualDescription();

        Intent dialogIntent = new Intent(context, DialogController.class);
        dialogIntent.putExtra(DialogController.KEY_DECISION_ID, decisionId);
        dialogIntent.putExtra(DialogController.KEY_DIALOG_BODY, dialogBody);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        if(decision.getName().equalsIgnoreCase(Decision.GRANTED_ACCESS)){
            // remove it from the queue, because it does not provide a dialog in which the user can click
            // on a button
            removeFeedbackFromQueue();

            // send user behavior to the server.
            // since, GRANTED is a situation where the user has no visible pop up, we will send an
            // automatic generated behavior, which is GRANTED
            Action action = new Action(Decision.GRANTED_ACCESS, System.currentTimeMillis());
            if(context != null) {
                UserContextMonitoringController.getInstance(context).sendUserBehavior(action, decisionId);
            }
            return;
        }
        else if(decision.getName().equalsIgnoreCase(Decision.MAYBE_ACCESS_WITH_RISKTREATMENTS)) {
            dialogIntent.putExtra(DialogController.KEY_DIALOG_TITLE, Decision.MAYBE_ACCESS_WITH_RISKTREATMENTS);
            dialogIntent.putExtra(DialogController.KEY_DIALOG, DialogController.MAYBE);
        }
        else if(decision.getName().equalsIgnoreCase(Decision.UPTOYOU_ACCESS_WITH_RISKCOMMUNICATION)) {
            dialogIntent.putExtra(DialogController.KEY_DIALOG_TITLE, Decision.UPTOYOU_ACCESS_WITH_RISKCOMMUNICATION);
            dialogIntent.putExtra(DialogController.KEY_DIALOG, DialogController.UP_TO_USER);
        }
        else if(decision.getName().equalsIgnoreCase(Decision.STRONG_DENY_ACCESS) ||
                decision.getName().equalsIgnoreCase(Decision.DEFAULT_DENY_ACCESS)) {
            dialogIntent.putExtra(DialogController.KEY_DIALOG_TITLE, Decision.STRONG_DENY_ACCESS);
            dialogIntent.putExtra(DialogController.KEY_DIALOG, DialogController.DENY);
        }

        context.startActivity(dialogIntent);
    }

    @Override
    public void sendFeedbackToMUSESAwareApp(Decision decision, Context context) {
        try {
            eu.musesproject.server.risktrust.RiskTreatment riskTreatment[] = decision.getRiskCommunication().getRiskTreatment();
            ResponseInfoAP infoAP;
            if(decision.getName().equals(Decision.STRONG_DENY_ACCESS) || decision.getName().equals(Decision.DEFAULT_DENY_ACCESS) ) {
                infoAP = ResponseInfoAP.DENY;
            }
            else {
                infoAP = ResponseInfoAP.ACCEPT;
            }

            new DummyCommunication(context).sendResponse(infoAP, riskTreatment[0]);
        } catch (Exception e) {
            e.printStackTrace();
            // no risk treatment
        }
    }

    @Override
    public void removeFeedbackFromQueue() {
        Log.d(TAG, "1 . remove feedback from queue");
        // removes the last feedback dialog
        if(decisionQueue != null) {
            try {
                decisionQueue.remove();
            } catch (Exception e) {
                Log.d(TAG, "no more entries in the queue");
            }
            if(!decisionQueue.isEmpty()) {
                // triggers to show the next feedback dialog if there is any
                Log.d(TAG, "2. Decision queue size is (after removal): " + decisionQueue.size());
                showNextFeedback(decisionQueue.element());

            }

            // update the notification bar to visualize if there are dialogs/messages or not
            NotificationController.getInstance(context).create(decisionQueue.size());
        }
    }

    @Override
    public void showCurrentTopFeedback() {
        if(decisionQueue != null && decisionQueue.size() > 0) {
            createFeedbackDialog(decisionQueue.peek());
        }
    }

    public void sendLoginResponseToUI(boolean result, String msg, int detailedStatus) {
    	Log.d(APP_TAG, "Info U, Actuator -> FeedbackActuator sending login response with result: " + result);
        Log.d(TAG, "called: sendLoginResponseToUI(boolean result)");
        if(callback!=null) {
            callback.onLogin(result, msg, detailedStatus);
        }
    }

    public void registerCallback(IUICallback iUICallback) {
        callback = iUICallback;
    }

    public void unregisterCallback(IUICallback iUICallback) {
        callback = iUICallback;
    }
}
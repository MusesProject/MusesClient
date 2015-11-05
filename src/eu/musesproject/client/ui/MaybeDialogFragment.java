package eu.musesproject.client.ui;

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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import eu.musesproject.client.R;
import eu.musesproject.client.actuators.ActuatorController;
import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;

/**
 * Created by christophstanik on 5/17/15.
 */
public class MaybeDialogFragment extends DialogFragment implements View.OnClickListener {
    public static final String TAG = MaybeDialogFragment.class.getSimpleName();

    public interface IOpportunityDialog {
        void show(String decisionId);
    }

    private boolean hasOpportunity;

    private TextView dialogHeader;
    private TextView dialogBody;
    private Button actionButton; // help me, fix it, ok
    private Button cancelButton;

    private String decisionId;
    private String title;
    private String[] splitBody;
    private String body;

    private IOpportunityDialog opportunityDialog;

    public static MaybeDialogFragment newInstance(IOpportunityDialog opportunityDialog, boolean hasOpportunity, String title, String body, String decisionId) {
        MaybeDialogFragment maybeDialogFragment = new MaybeDialogFragment();
        maybeDialogFragment.hasOpportunity = hasOpportunity;
        maybeDialogFragment.opportunityDialog = opportunityDialog;
        maybeDialogFragment.title = title;
        maybeDialogFragment.body = body;
        maybeDialogFragment.decisionId = decisionId;

        return maybeDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DebugFileLog.write(TAG + "| onCreateDialog");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_maybe, null);

        dialogHeader = (TextView) layout.findViewById(R.id.dialog_maybe_title);
        dialogBody = (TextView) layout.findViewById(R.id.dialog_maybe_body);
        actionButton = (Button) layout.findViewById(R.id.dialog_opportunity_button_action);
        cancelButton = (Button) layout.findViewById(R.id.dialog_maybe_button_cancel);

        dialogHeader.setText(title);

        if(body == null || body.isEmpty()) {
            // if there is no message that we can show to the user, just dismiss the dialog
            Log.d(TAG, "no message found for the dialog");
            dismiss();
            onDestroy();
        }

        try {
            splitBody = body.split("\\n");
        } catch (NullPointerException e) {
            Log.d(TAG, "cannot split string, therefore make the details text the same as the title");
            splitBody = new String[2];
            splitBody[0] = body;
            splitBody[1] = body;
        }
        dialogBody.setText(splitBody[0]);

        actionButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        if(!hasOpportunity) {
            actionButton.setText(getString(R.string.button_details));
        }

        // dialog design theme
        ContextThemeWrapper context = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar);

        return new AlertDialog.Builder(context).setView(layout).create();
    }

    @Override
    public void onClick(View v) {
        Action action = null;
        switch (v.getId()) {
            case R.id.dialog_opportunity_button_action:
                if(hasOpportunity) {
                    // send the behavior to the server
                    action = new Action(ActionType.OPPORTUNITY, System.currentTimeMillis());
                    UserContextMonitoringController.getInstance(getActivity()).sendUserBehavior(action, decisionId);

                    // remove the feedback and close the dialog
                    ActuatorController.getInstance(getActivity()).removeFeedbackFromQueue();
                    ActuatorController.getInstance(getActivity()).perform(decisionId);
                    opportunityDialog.show(decisionId);
                }
                else {
                    try {
                        dialogBody.setText(splitBody[1]);
                        actionButton.setVisibility(View.INVISIBLE);
                    } catch (Exception e) {
                        Log.e(TAG, "cannot handle the detail button. Text might be missing");
                    }
                }
                break;
            case R.id.dialog_maybe_button_cancel:
                // send the behavior to the server
                action = new Action(ActionType.CANCEL, System.currentTimeMillis());
                UserContextMonitoringController.getInstance(getActivity()).sendUserBehavior(action, decisionId);

                // remove the feedback and close the dialog
                this.dismiss();
                ActuatorController.getInstance(getActivity()).removeFeedbackFromQueue();
                ActuatorController.getInstance(getActivity()).perform(decisionId);
                getActivity().finish();
                break;
        }
    }
}
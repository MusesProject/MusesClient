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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import eu.musesproject.client.R;
import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;

/**
 * Created by christophstanik on 5/17/15.
 */
public class OpportunityDialogFragment extends DialogFragment implements View.OnClickListener {
    public static final String TAG = OpportunityDialogFragment.class.getSimpleName();

    private String decisionId;

    private EditText timeEdit;
    private EditText eurosEdit;
    private EditText descriptionEdit;
    private Button sendButton;


    public static OpportunityDialogFragment newInstance(String decisionId) {
        OpportunityDialogFragment opportunityDialogFragment = new OpportunityDialogFragment();
        opportunityDialogFragment.decisionId = decisionId;

        return opportunityDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_opportunity, null);

        timeEdit = (EditText) layout.findViewById(R.id.dialog_op_edit_time);
        eurosEdit = (EditText) layout.findViewById(R.id.dialog_op_edit_revenue_loss_euros);
        descriptionEdit = (EditText) layout.findViewById(R.id.dialog_op_edit_revenue_loss_description);
        sendButton = (Button) layout.findViewById(R.id.dialog_op_button_send);
        sendButton.setOnClickListener(this);

        ContextThemeWrapper context = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar);

        return new AlertDialog.Builder(context).setView(layout).create();
    }

    @Override
    public void onClick(View v) {
        Action action = null;
        switch (v.getId()) {
            case R.id.dialog_op_button_send:
                String time = "";
                String euros = "";
                String description = "";
                try {
                    time = timeEdit.getText().toString();
                    euros = eurosEdit.getText().toString();
                    description = descriptionEdit.getText().toString();
                } catch (Exception e) {
                    Log.e(TAG, "cannot retrieve input from the edit text fields");
                }

                if(time.isEmpty() || euros.isEmpty() || description.isEmpty()) {
                    try {
                        Toast.makeText(getActivity(), R.string.toast_enter_all_fields, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, "cannot show toast message");
                    }
                }
                else {
                    // send the behavior to the server
                    action = new Action(ActionType.OPPORTUNITY, System.currentTimeMillis());
                    UserContextMonitoringController.getInstance(getActivity()).sendUserBehavior(action, decisionId);
                    UserContextMonitoringController.getInstance(getActivity()).sendOpportunity(decisionId, time, euros, description);
                    try {
                        if (Integer.valueOf(euros) <= 10000) {
                            String dialogTitle = getActivity().getString(R.string.feedback_dialog_title_deny);
                            String dialogBody = "";
                            Intent dialogIntent = new Intent(getActivity(), DialogController.class);
                            dialogIntent.putExtra(DialogController.KEY_DECISION_ID, -1);
                            dialogIntent.putExtra(DialogController.KEY_DIALOG_BODY, dialogBody);
                            dialogIntent.putExtra(DialogController.KEY_DIALOG_HAS_OPPORTUNITY, false);
                            dialogIntent.putExtra(DialogController.KEY_DIALOG_TITLE, dialogTitle);
                            dialogIntent.putExtra(DialogController.KEY_DIALOG, DialogController.DENY);
                            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    | Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(dialogIntent);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "can't cast euros to string");
                    }

                    this.dismiss();
                    getActivity().finish();
                }
                break;
        }
    }
}
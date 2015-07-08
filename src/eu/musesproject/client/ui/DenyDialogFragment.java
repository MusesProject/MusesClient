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
public class DenyDialogFragment extends DialogFragment implements View.OnClickListener {
    public static final String TAG = DenyDialogFragment.class.getSimpleName();

    private TextView dialogHeader;
    private TextView dialogBody;
    private Button detailsButton;
    private Button cancelButton;

    private String decisionId;
    private String title;
    private String[] splitBody;
    private String body;

    private int actuationIdentifier;

    public static DenyDialogFragment newInstance(String title, String body, String decisionId) {
        DenyDialogFragment denyDialogFragment = new DenyDialogFragment();
        denyDialogFragment.decisionId = decisionId;
        denyDialogFragment.title = title;
        denyDialogFragment.body = body;

        return denyDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DebugFileLog.write(TAG + "| onCreateDialog");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_deny, null);

        dialogHeader = (TextView) layout.findViewById(R.id.dialog_deny_title);
        dialogBody = (TextView) layout.findViewById(R.id.dialog_deny_body);
        detailsButton = (Button) layout.findViewById(R.id.dialog_deny_button_details);
        cancelButton = (Button) layout.findViewById(R.id.dialog_deny_button_cancel);

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

        detailsButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        ContextThemeWrapper context = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar);

        return new AlertDialog.Builder(context).setView(layout).create();
    }

    @Override
    public void onClick(View v) {
        Action action = null;
        switch (v.getId()) {
            case R.id.dialog_deny_button_details:
                // send the behavior to the server
                action = new Action(ActionType.DETAILS, System.currentTimeMillis());
                UserContextMonitoringController.getInstance(getActivity()).sendUserBehavior(action, decisionId);

                // update the dialog UI
                detailsButton.setVisibility(View.INVISIBLE);

                dialogHeader.setText(title);
                try {
                    dialogBody.setText(splitBody[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.d(TAG, "no second screen text found, showing the first one instead");
                    dialogBody.setText(splitBody[0]);
                }
                break;
            case R.id.dialog_deny_button_cancel:
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
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
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import eu.musesproject.client.R;
import eu.musesproject.client.actuators.ActuatorController;

/**
 * Created by christophstanik on 5/17/15.
 */
public class DenyDialogFragment extends DialogFragment implements View.OnClickListener {
    private TextView dialogHeader;
    private TextView dialogBody;
    private Button detailsButton;
    private Button cancelButton;

    private String title;
    private String body;

    private int actuationIdentifier;

    public static DenyDialogFragment newInstance(String title, String body, int actuationIdentifier) {
        DenyDialogFragment denyDialogFragment = new DenyDialogFragment();
        denyDialogFragment.title = title;
        denyDialogFragment.body = body;
        denyDialogFragment.actuationIdentifier = actuationIdentifier;

        return denyDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_deny, null);

        dialogHeader = (TextView) layout.findViewById(R.id.dialog_deny_title);
        dialogBody = (TextView) layout.findViewById(R.id.dialog_deny_body);
        detailsButton = (Button) layout.findViewById(R.id.dialog_deny_button_details);
        cancelButton = (Button) layout.findViewById(R.id.dialog_deny_button_cancel);

        dialogHeader.setText(title);
        dialogBody.setText(body);

        detailsButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        ContextThemeWrapper context = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar);

        return new AlertDialog.Builder(context).setView(layout).create();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_deny_button_details:
                detailsButton.setVisibility(View.INVISIBLE);

                dialogHeader.setText(title);
                dialogBody.setText(body);
                break;
            case R.id.dialog_deny_button_cancel:
                this.dismiss();
                ActuatorController.getInstance(getActivity()).removeFeedbackFromQueue();
                getActivity().finish();
                break;
        }
    }
}
package eu.musesproject.client.ui;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;

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

/**
 * Created by christophstanik on 5/17/15.
 */
public class DialogController extends Activity {
    public static final String KEY_DIALOG       = "dialog_policy";
    public static final String KEY_DECISION_ID  = "decision_id";
    public static final String KEY_DIALOG_TITLE = "dialog_title";
    public static final String KEY_DIALOG_BODY  = "dialog_body";
    public static final String KEY_DIALOG_CMD   = "dialog_actuation_command";

    public static final int DENY          = 0;
    public static final int MAYBE         = 1;
    public static final int UP_TO_USER    = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        int policy = bundle.getInt(KEY_DIALOG);
        int decisionId = bundle.getInt(KEY_DECISION_ID);
        String dialogTitle = bundle.getString(KEY_DIALOG_TITLE);
        String dialogBody = bundle.getString(KEY_DIALOG_BODY);
        int actuationCommand = bundle.getInt(KEY_DIALOG_CMD);

        Log.d("FeedbackActuator", "policy:"+policy + " | title:"+dialogTitle + " | body:"+dialogBody);

        DialogFragment targetDialogFragment = null;
        switch (policy) {
            case DENY:
                targetDialogFragment = createDenyDialog(dialogTitle, dialogBody, decisionId, actuationCommand);
                break;
            case MAYBE:
                targetDialogFragment = createMaybeDialog(dialogTitle, dialogBody, decisionId, actuationCommand);
                break;
            case UP_TO_USER:
                targetDialogFragment = createUpToUserDialog(dialogTitle, dialogBody, decisionId, actuationCommand);
                break;
        }
        if(targetDialogFragment != null) {
            targetDialogFragment.setCancelable(false);
//            targetDialogFragment.setShowsDialog(true);
            targetDialogFragment.show(getFragmentManager(), "dialog");
        }
    }

    private DialogFragment createDenyDialog(String title, String body, int decisionId, int actuationCommand) {
        return DenyDialogFragment.newInstance(title, body, decisionId, actuationCommand);
    }

    private DialogFragment createMaybeDialog(String title, String body, int decisionId, int actuationCommand) {
        return MaybeDialogFragment.newInstance(title,body, decisionId, actuationCommand);
    }

    private DialogFragment createUpToUserDialog(String title, String body, int decisionId, int actuationCommand) {
        return UpToUserDialogFragment.newInstance(title, body, decisionId, actuationCommand);
    }
}
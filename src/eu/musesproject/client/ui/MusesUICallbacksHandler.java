package eu.musesproject.client.ui;
/*
 * #%L
 * MUSES Client
 * %%
 * Copyright (C) 2013 - 2014 Sweden Connectivity
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import eu.musesproject.client.actuators.IUICallback;
import eu.musesproject.client.connectionmanager.DetailedStatuses;
import eu.musesproject.client.model.JSONIdentifiers;

public class MusesUICallbacksHandler implements IUICallback {


	private static final String TAG = MusesUICallbacksHandler.class.getSimpleName();
	private Handler mHandler;
	// CallBack messages
	public static final int LOGIN_SUCCESSFUL = 0;
	public static final int LOGIN_UNSUCCESSFUL = 1;
	public static final int ACTION_RESPONSE_ACCEPTED = 2;
	public static final int ACTION_RESPONSE_DENIED = 3;
	public static final int ACTION_RESPONSE_MAY_BE = 4;
	public static final int ACTION_RESPONSE_UP_TO_USER = 5;
	
	public MusesUICallbacksHandler(Context context, Handler handler) {
		mHandler = handler;
	}
	
	@Override
	public void onLogin(boolean result, String detailedMsg, int errorCode) {
		Log.d(TAG, "onLogin result: " + result);
        Message msg;
		if (result) {
			msg = mHandler.obtainMessage(LOGIN_SUCCESSFUL);
		} else {
			if((errorCode == DetailedStatuses.INCORRECT_CERTIFICATE) || (errorCode == DetailedStatuses.INCORRECT_URL)
																		|| (errorCode == DetailedStatuses.INTERNAL_SERVER_ERROR) 
																		|| (errorCode == DetailedStatuses.NO_INTERNET_CONNECTION)
																		|| (errorCode == DetailedStatuses.UNKNOWN_ERROR)
																		|| (errorCode == DetailedStatuses.NOT_FOUND) 
																		|| (errorCode == DetailedStatuses.NOT_ALLOWED_FROM_SERVER_UNAUTHORIZED)) {
				msg = mHandler.obtainMessage(errorCode);
			} else{
				msg = mHandler.obtainMessage(LOGIN_UNSUCCESSFUL);
			}
		}

        Bundle bundle = new Bundle();
        bundle.putString(JSONIdentifiers.AUTH_MESSAGE, detailedMsg);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

}
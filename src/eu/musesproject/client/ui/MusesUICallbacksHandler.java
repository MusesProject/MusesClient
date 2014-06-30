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
import eu.musesproject.client.model.decisiontable.Decision;
import eu.musesproject.server.risktrust.RiskTreatment;

public class MusesUICallbacksHandler implements IUICallback {


	private static final String TAG = MusesUICallbacksHandler.class.getSimpleName();
	private Context context;
	private Handler mHandler;
	// CallBack messages
	public static final int LOGIN_SUCCESSFUL = 0;
	public static final int LOGIN_UNSUCCESSFUL = 1;
	public static final int ACTION_RESPONSE_ACCEPTED = 2;
	public static final int ACTION_RESPONSE_DENIED = 3;
	public static final int ACTION_RESPONSE_MAY_BE = 4;
	public static final int ACTION_RESPONSE_UP_TO_USER = 5;
	
	public MusesUICallbacksHandler(Context context, Handler handler) {
		this.context = context;
		mHandler = handler;
	}
	
	@Override
	public void onLogin(boolean result) {
		Log.d(TAG, "onLogin result: " + result);
		if (result) {
			Message msg = mHandler.obtainMessage(LOGIN_SUCCESSFUL);
			mHandler.sendMessage(msg);	
			
		} else {
			Message msg = mHandler.obtainMessage(LOGIN_UNSUCCESSFUL);
			mHandler.sendMessage(msg);	
		}
	}

	@Override
	public void onAccept() {
		Log.d(TAG, "onAccept: " );
    	Message msg = mHandler.obtainMessage(ACTION_RESPONSE_ACCEPTED);
		mHandler.sendMessage(msg);		
	}

	@Override
	public void onDeny(Decision decision) {
		Log.d(TAG, "onDeny: " + decision.toString());
		String textualDecp = "this is a test risk treatment ...";
		RiskTreatment[] r = decision.getRiskCommunication().getRiskTreatment();
		if (r[0].getTextualDescription() != null) {
			textualDecp = r[0].getTextualDescription();
		}
    	Message msg = mHandler.obtainMessage(ACTION_RESPONSE_DENIED);
		Bundle bundle = new Bundle();
		bundle.putString("name",decision.getName());
		bundle.putString("risk_textual_decp", textualDecp);
		msg.setData(bundle);
		mHandler.sendMessage(msg);		
	}

	@Override
	public void onMaybe(Decision decision) {
		Log.d(TAG, "onMaybe: " + decision.toString());
		String textualDecp = "this is a test risk treatment ...";
		RiskTreatment[] r = decision.getRiskCommunication().getRiskTreatment();
		if (r[0].getTextualDescription() != null) {
			textualDecp = r[0].getTextualDescription();
		}
    	Message msg = mHandler.obtainMessage(ACTION_RESPONSE_MAY_BE);
		Bundle bundle = new Bundle();
		bundle.putString("name",decision.getName());
		bundle.putString("risk_textual_decp",textualDecp);
		msg.setData(bundle);
		mHandler.sendMessage(msg);		
	}

	@Override
	public void onUpToUser(Decision decision) {
		Log.d(TAG, "onUpToUser: " + decision.toString());
		String textualDecp = "this is a test risk treatment ...";
		RiskTreatment[] r = decision.getRiskCommunication().getRiskTreatment();
		if (r[0].getTextualDescription() != null) {
			textualDecp = r[0].getTextualDescription();
		}    	
		Message msg = mHandler.obtainMessage(ACTION_RESPONSE_UP_TO_USER);
		Bundle bundle = new Bundle();
		bundle.putString("name",decision.getName());
		bundle.putString("risk_textual_decp",textualDecp);
		msg.setData(bundle);
		mHandler.sendMessage(msg);		
	}

}
package eu.musesproject.client.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.IUserContextMonitoringControllerCallback;
import eu.musesproject.client.model.actuators.RiskTreatment;

public class MusesUICallbacksHandler implements IUserContextMonitoringControllerCallback {


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
	public void onAccept(RiskTreatment riskTreatment) {
		Log.d(TAG, "onAccept: " + riskTreatment.toString());
    	Message msg = mHandler.obtainMessage(ACTION_RESPONSE_ACCEPTED);
		Bundle bundle = new Bundle();
		bundle.putString("message",riskTreatment.toString());
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	@Override
	public void onDeny(RiskTreatment riskTreatment) {
		Log.d(TAG, "onDeny: " + riskTreatment.toString());
    	Message msg = mHandler.obtainMessage(ACTION_RESPONSE_DENIED);
		Bundle bundle = new Bundle();
		bundle.putString("message",riskTreatment.toString());
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	@Override
	public void onMaybe(RiskTreatment riskTreatment) {
		Log.d(TAG, "onMaybe: " + riskTreatment.toString());
    	Message msg = mHandler.obtainMessage(ACTION_RESPONSE_MAY_BE);
		Bundle bundle = new Bundle();
		bundle.putString("message",riskTreatment.toString());
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	@Override
	public void onUpToUser(RiskTreatment riskTreatment) {
		Log.d(TAG, "onUpToUser: " + riskTreatment.toString());
    	Message msg = mHandler.obtainMessage(ACTION_RESPONSE_UP_TO_USER);
		Bundle bundle = new Bundle();
		bundle.putString("message",riskTreatment.toString());
		msg.setData(bundle);
		mHandler.sendMessage(msg);
    }

}
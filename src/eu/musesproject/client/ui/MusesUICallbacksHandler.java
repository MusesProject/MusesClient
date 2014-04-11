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
	
	public MusesUICallbacksHandler(Context context, Handler handler) {
		this.context = context;
		mHandler = handler;
	}
	
	@Override
	public void onLogin(boolean result) {
		Log.d(TAG, "onLogin result: " + result);
		if (result) {
			Message msg = mHandler.obtainMessage(LoginView.LOGIN_SUCCESSFUL);
			mHandler.sendMessage(msg);	
			
		} else {
			Message msg = mHandler.obtainMessage(LoginView.LOGIN_UNSUCCESSFUL);
			mHandler.sendMessage(msg);	
		}
	}
	
	@Override
	public void onAccept(RiskTreatment riskTreatment) {
		Log.d(TAG, "onAccept: " + riskTreatment.toString());
    	Message msg = mHandler.obtainMessage(LoginView.ACTION_ACCEPTED);
		Bundle bundle = new Bundle();
		bundle.putString("message",riskTreatment.toString());
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	@Override
	public void onDeny(RiskTreatment riskTreatment) {
		Log.d(TAG, "onDeny: " + riskTreatment.toString());
    	Message msg = mHandler.obtainMessage(LoginView.ACTION_DENIED);
		Bundle bundle = new Bundle();
		bundle.putString("message",riskTreatment.toString());
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}
	
}
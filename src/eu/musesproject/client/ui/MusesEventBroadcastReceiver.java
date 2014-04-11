package eu.musesproject.client.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MusesEventBroadcastReceiver extends BroadcastReceiver{

	private static final String TAG = MusesEventBroadcastReceiver.class.getSimpleName();
	public static final String MUSES_FEEDBACK_INTENT = "eu.musesproject.client.intent.muses.feedback";
	private static final boolean D = true;
	@Override
	public void onReceive(Context context, Intent intent) {
		String message = intent.getStringExtra("message");
    	if (D) Log.e(TAG, "A broadcast received .." + message);
    	//MainActivity.showResultDialog(message);
	}
}
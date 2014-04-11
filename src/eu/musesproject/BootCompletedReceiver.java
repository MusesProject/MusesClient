package eu.musesproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = BootCompletedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent arg1) {
        Log.w(TAG, "starting MUSES background service...");
        context.startService(new Intent(context, MUSESBackgroundService.class));
    }
}

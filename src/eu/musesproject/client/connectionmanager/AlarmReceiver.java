/*
 * version 1.0 - MUSES prototype software
 * Copyright MUSES project (European Commission FP7) - 2013 
 * 
 */
package eu.musesproject.client.connectionmanager;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;


/**
 * Class Alarm receiver listens for systems notifications like screen on/off , wakelock
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */

public class AlarmReceiver extends BroadcastReceiver {
	
	private static final String TAG = "AlarmReceiver";
	private static final boolean D = false;
	public static int POLL_INTERVAL = 10000; // Default value
	public static int SLEEP_POLL_INTERVAL = 60000; // Default value
	public static int exponentialCounter = 4;
	public static int DEFAULT_POLL_INTERVAL = 10000;
	public static int DEFAULT_SLEEP_POLL_INTERVAL = 60000;
	public static int LAST_SENT_POLL_INTERVAL;
	@SuppressLint("Wakelock")
	@Override
	public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        if (NetworkChecker.isInternetConnected) {
        	// start poll
        	Polling pollingBackgroundThread = new Polling();
        	pollingBackgroundThread.execute();
        }
        
        if (D) Log.e(TAG, "Alarm..");
        wl.release();
	}

	/**
	 * Increase poll time
	 * @return void 
	 */
	
	public static void increasePollTime(){
		
		if (exponentialCounter > 0) {
			POLL_INTERVAL = POLL_INTERVAL*2;
			SLEEP_POLL_INTERVAL = SLEEP_POLL_INTERVAL*2;
			exponentialCounter--;
		} 
		
		
	}
	
	/**
	 * Resets poll time
	 * @return void
	 */
	public static void resetExponentialPollTime(){
		POLL_INTERVAL = DEFAULT_POLL_INTERVAL;
		SLEEP_POLL_INTERVAL = DEFAULT_SLEEP_POLL_INTERVAL;
		exponentialCounter = 3;
	}
	
	/**
	 * Set alarm to current poll interval according to the phone mode sleep/active
	 * @param context
	 * @return void
	 */
	
    public void setAlarm(Context context) {
    	int wakeTime = 0;
    	if (PhoneModeReceiver.SLEEP_MODE_ACTIVE) {
    		wakeTime = SLEEP_POLL_INTERVAL;
    	}else {
    		wakeTime = POLL_INTERVAL;
    	}
    	
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent wakeUpAlarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingintent = PendingIntent.getBroadcast(context, 0, wakeUpAlarmIntent, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), wakeTime, pendingintent); // Millisec * Second * Minute
    }

    /**
     * Cancels alarm
     * @param context
     * @return void
     */
    public void cancelAlarm(Context context) {
        Intent wakeUpAlarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, wakeUpAlarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
    
    /**
     * Asynk class to poll in a seperate thread
     * @author yasir
     * @version Jan 27, 2014
     */
    
    private class Polling extends AsyncTask<String, Void, String> {

    	@Override
    	protected String doInBackground(String... params) {
            ConnectionManager connectionManager = new ConnectionManager();
            connectionManager.poll();
    		return null;
    	}

    }

    
    
}

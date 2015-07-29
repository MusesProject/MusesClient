package eu.musesproject.client.connectionmanager;

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

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
	public static int POLL_INTERVAL = 5000; // Default value
	public static int SLEEP_POLL_INTERVAL = 60000; // Default value
	private static int exponentialCounter = 4;
	public static int DEFAULT_POLL_INTERVAL = 5000;
	public static int DEFAULT_SLEEP_POLL_INTERVAL = 60000;
	private static int CURRENT_POLL_INTERVAL;
	private static boolean POLL_INTERVAL_UPDATED = false;
	private static boolean SLEEP_MODE_ACTIVE;
	public static int POLLING_ENABLED = 1;
	private static ConnectionManager CONNECTIONMANAGER= null;
	
	public static void setManager(ConnectionManager connectionManager) {
		CONNECTIONMANAGER = connectionManager;
	}

	@SuppressLint("Wakelock")
	@Override
	public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        
        if (CONNECTIONMANAGER != null)
        {
        	CONNECTIONMANAGER.periodicPoll();
        } else {
        	Log.d(TAG, "ConnectionManager is null!!");
        }
        
        /* Check if timeouts have changed, if so update */
        applyTimeoutChanges(context);
        Log.d(TAG, "Alarm... called from android system .. current poll-interval: " +CURRENT_POLL_INTERVAL);
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
			POLL_INTERVAL_UPDATED = true;
		} 
		Log.d(TAG, "Alarm... increasing poll timeout, current poll-interval now: "
					+POLL_INTERVAL+" and sleep poll-interval: "+SLEEP_POLL_INTERVAL);
	}
	
	/**
	 * Resets poll time
	 * @return void
	 */
	public static void resetExponentialPollTime(){
		if (POLL_INTERVAL != DEFAULT_POLL_INTERVAL){
			POLL_INTERVAL_UPDATED = true;
		}
		POLL_INTERVAL = DEFAULT_POLL_INTERVAL;
		SLEEP_POLL_INTERVAL = DEFAULT_SLEEP_POLL_INTERVAL;
		exponentialCounter = 4;
		Log.d(TAG, "Alarm... resetting poll timeout, current poll-interval now: "
				+POLL_INTERVAL+" and sleep poll-interval: "+SLEEP_POLL_INTERVAL);

	}
	
	/**
	 * If poll interval has been changed, setAlarm
	 * @param context
	 * @return void
	 */
	 private void applyTimeoutChanges(Context context){
		 if (POLL_INTERVAL_UPDATED)	{
			 POLL_INTERVAL_UPDATED = false;
			 cancelAlarm(context);
			 setAlarm(context);
		 }
		 Log.d(TAG, "Alarm... applying timeout changes..");

	 }
	 
	 
	 
	
	
	/**
	 * Set alarm to current poll interval according to the phone mode sleep/active
	 * @param context
	 * @return void
	 */
	
    public void setAlarm(Context context) {
    	if (SLEEP_MODE_ACTIVE) {
    		CURRENT_POLL_INTERVAL = SLEEP_POLL_INTERVAL;
    	}else {
    		CURRENT_POLL_INTERVAL = POLL_INTERVAL;
    	}
    	
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent wakeUpAlarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingintent = PendingIntent.getBroadcast(context, 0, wakeUpAlarmIntent, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), CURRENT_POLL_INTERVAL, pendingintent); // Millisec * Second * Minute
		Log.d(TAG, "Alarm... setting alarm with current poll-interval: "+CURRENT_POLL_INTERVAL);

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
        Log.d(TAG, "Alarm... cancelling alarm.");
    }

	public void setPollInterval(int pollInterval, int sleepPollInterval) {
		POLL_INTERVAL = pollInterval;
		SLEEP_POLL_INTERVAL = sleepPollInterval;
		POLL_INTERVAL_UPDATED = true;
		Log.d(TAG, "Alarm... setting poll timeout, current poll-interval now: "
				+POLL_INTERVAL+" and sleep poll-interval: "+SLEEP_POLL_INTERVAL);
	}

	

	public void setDefaultPollInterval(int pollInterval,
			int sleepPollInterval) {
		DEFAULT_POLL_INTERVAL = pollInterval;
		DEFAULT_SLEEP_POLL_INTERVAL = sleepPollInterval;
		Log.d(TAG, "Alarm... setting default poll interval.");
		
	}

	public static int getCurrentPollInterval() {
		return CURRENT_POLL_INTERVAL;
	}

	public static void setPollMode(boolean sleepModeActive) {
		Log.d(TAG, "Alarm... resetting poll_mode, sleep_moed_active: "+sleepModeActive);
		SLEEP_MODE_ACTIVE = sleepModeActive;
		POLL_INTERVAL_UPDATED = true;
	}
    
   
}

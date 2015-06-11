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
	private static int POLL_INTERVAL = 60000; // Default value
	private static int SLEEP_POLL_INTERVAL = 60000; // Default value
	private static int exponentialCounter = 4;
	private static int DEFAULT_POLL_INTERVAL = 60000;
	private static int DEFAULT_SLEEP_POLL_INTERVAL = 60000;
	private static int CURRENT_POLL_INTERVAL;
	private static boolean POLL_INTERVAL_UPDATED = false;
	private static boolean SLEEP_MODE_ACTIVE;
	
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
        Log.d(TAG, "Alarm..");
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
		
		
	}
	
	/**
	 * Resets poll time
	 * @return void
	 */
	public static void resetExponentialPollTime(){
		if (POLL_INTERVAL != DEFAULT_POLL_INTERVAL)
		{
			POLL_INTERVAL_UPDATED = true;
		}
		POLL_INTERVAL = DEFAULT_POLL_INTERVAL;
		SLEEP_POLL_INTERVAL = DEFAULT_SLEEP_POLL_INTERVAL;
		exponentialCounter = 3;
		
	}
	
	/**
	 * If poll interval has been changed, setAlarm
	 * @param context
	 * @return void
	 */
	 private void applyTimeoutChanges(Context context){
		 if (POLL_INTERVAL_UPDATED)
		 {
			 POLL_INTERVAL_UPDATED = false;
			 cancelAlarm(context);
			 setAlarm(context);
		 }
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

	public void setPollInterval(int pollInterval, int sleepPollInterval) {
		POLL_INTERVAL = pollInterval;
		SLEEP_POLL_INTERVAL = sleepPollInterval;
		POLL_INTERVAL_UPDATED = true;
	}

	

	public void setDefaultPollInterval(int pollInterval,
			int sleepPollInterval) {
		DEFAULT_POLL_INTERVAL = pollInterval;
		DEFAULT_SLEEP_POLL_INTERVAL = sleepPollInterval;
		
	}

	public static int getCurrentPollInterval() {
		
		return CURRENT_POLL_INTERVAL;
	}

	public static void setPollMode(boolean sleepModeActive) {
		
		SLEEP_MODE_ACTIVE = sleepModeActive;
		POLL_INTERVAL_UPDATED = true;
	}
    
   
}

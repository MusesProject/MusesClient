/*
 * version 1.0 - MUSES prototype software
 * Copyright MUSES project (European Commission FP7) - 2013 
 * 
 */

package eu.musesproject.client.connectionmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * This class is responsible to set alarm and cancel alarm in order to handle polling
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */

public class PhoneModeReceiver {
	
	protected static final String TAG = "PhoneModeReceiver";
	public static boolean D = false;
	public static boolean SLEEP_MODE_ACTIVE = false;
	private Context context;
	
	/**
	 * Constructor
	 * @param context
	 */
	
	public PhoneModeReceiver(Context context) {
		this.context = context;
	}
	
	/**
	 * Register for screen off/on broadcast from the system
	 * @return void
	 */
	
	public void register(){
		context.registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		context.registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
	}
	
	
	/**
	 * Implements the on Receive method to receive broadcast
	 *  
	 */
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
				// Set alarm normal mode
				if(D) Log.v(TAG, "Screeen On");
				AlarmReceiver alarmReceiver = new AlarmReceiver();
				alarmReceiver.cancelAlarm(context);
				alarmReceiver.setAlarm(context);
				SLEEP_MODE_ACTIVE = false;
			}
			
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
				// Set alarm sleep mode
				if(D) Log.v(TAG, "Screeen Off");
				AlarmReceiver alarmReceiver = new AlarmReceiver();
				alarmReceiver.cancelAlarm(context);
				alarmReceiver.setAlarm(context);
				SLEEP_MODE_ACTIVE = true;
			}
		}
		
	};
	
	
}

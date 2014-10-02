package eu.musesproject.client.contextmonitoring.sensors;

/*
 * #%L
 * musesclient
 * %%
 * Copyright (C) 2013 - 2014 HITEC
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

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.stericson.RootTools.RootTools;

import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.client.db.entity.SensorConfiguration;
import eu.musesproject.client.db.handler.DBManager;
import eu.musesproject.contextmodel.ContextEvent;

/**
 * @author christophstanik
 * 
 *         Class to collect information about the device configuration
 */

public class DeviceProtectionSensor implements ISensor {
	private static final String TAG = DeviceProtectionSensor.class.getSimpleName();

	// sensor identifier
	public static final String TYPE = "CONTEXT_SENSOR_DEVICE_PROTECTION";

    // time in milliseconds when the sensor polls information
    private static int OBSERVATION_INTERVALL = 10000;
    
	// context property keys
	public static final String PROPERTY_KEY_ID 							= "id";
	public static final String PROPERTY_KEY_IS_ROOTED 					= "isrooted";
	public static final String PROPERTY_KEY_IS_ROOT_PERMISSION_GIVEN 	= "isrootpermissiongiven";
	public static final String PROPERTY_KEY_IP_ADRESS 					= "ipaddress";
	public static final String PROPERTY_KEY_IS_PASSWORD_PROTECTED 		= "ispasswordprotected";
	public static final String PROPERTY_KEY_SCREEN_TIMEOUT_IN_SECONDS 	= "screentimeoutinseconds";
	public static final String PROPERTY_KEY_IS_TRUSTED_AV_INSTALLED 	= "istrustedantivirusinstalled";
	public static final String PROPERTY_KEY_MUSES_DATABASE_EXISTS 		= "musesdatabaseexists";
	public static final String PROPERTY_KEY_ACCESSIBILITY_ENABLED 		= "accessibilityenabled";
	
	// config keys
    public static final String CONFIG_KEY_TRUSTED_AV = "trustedav";

	private Context context;
	private ContextListener listener;

	// history of fired context events
	List<ContextEvent> contextEventHistory;

	// holds a value that indicates if the sensor is enabled or disabled
	private boolean sensorEnabled;

	// list with names of trusted anti virus applications
	private List<String> trustedAVs;

	public DeviceProtectionSensor(Context context) {
		this.context = context;
		contextEventHistory = new ArrayList<ContextEvent>(CONTEXT_EVENT_HISTORY_SIZE);

		init();
	}

	private void init() {
		sensorEnabled = false;

		trustedAVs = new ArrayList<String>();
		RootTools.debugMode = false;
	}

	@Override
	public void enable() {
		if (!sensorEnabled) {
			sensorEnabled = true;

			new CreateContextEventAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	private void createContextEvent() {
		// create context event
		ContextEvent contextEvent = new ContextEvent();
		contextEvent.setType(TYPE);
		contextEvent.setTimestamp(System.currentTimeMillis());
		contextEvent.addProperty(PROPERTY_KEY_ID, String.valueOf(contextEventHistory != null ? (contextEventHistory .size() + 1) : -1));
//		contextEvent.addProperty(PROPERTY_KEY_IS_ROOTED, String.valueOf(checkDeviceRooted()));
//		contextEvent.addProperty(PROPERTY_KEY_IS_ROOT_PERMISSION_GIVEN, String.valueOf(checkRootPermissionGiven()));
		contextEvent.addProperty(PROPERTY_KEY_IP_ADRESS, getIPAddress(true));
		contextEvent.addProperty(PROPERTY_KEY_IS_PASSWORD_PROTECTED, String.valueOf(isPasswordProtected()));
		contextEvent.addProperty(PROPERTY_KEY_SCREEN_TIMEOUT_IN_SECONDS, String.valueOf(getScreenTimeout()));
		contextEvent.addProperty(PROPERTY_KEY_IS_TRUSTED_AV_INSTALLED, String.valueOf(isTrustedAntiVirInstalled()));
		contextEvent.addProperty(PROPERTY_KEY_MUSES_DATABASE_EXISTS, String.valueOf(musesDatabaseExist(context, DBManager.DATABASE_NAME)));
		contextEvent.addProperty(PROPERTY_KEY_ACCESSIBILITY_ENABLED, String.valueOf(isAccessibilityForMusesEnabled()));

		
		if(contextEventHistory.size() > 0) {
            ContextEvent previousContext = contextEventHistory.get(contextEventHistory.size() - 1);
            // fire new context event if a connectivity context field changed
            if(!identicalContextEvent(previousContext, contextEvent)) {
        		// add context event to the context event history
                contextEventHistory.add(contextEvent);
                if(contextEventHistory.size() > CONTEXT_EVENT_HISTORY_SIZE) {
                    contextEventHistory.remove(0);
                }
                
                if (contextEvent != null && listener != null) {
                	debug(contextEvent);
    				listener.onEvent(contextEvent);
    			}
            }
		}
		else {
			contextEventHistory.add(contextEvent);
			if (contextEvent != null && listener != null) {
				debug(contextEvent);
				listener.onEvent(contextEvent);
			}
		}

	}
	
	public void debug(ContextEvent contextEvent) {
		for (Entry<String, String> set : contextEvent.getProperties().entrySet()) {
			Log.d(TAG, set.getKey() + " = " + set.getValue());
		}

		Log.d(TAG, " ");
	}
	
	private boolean identicalContextEvent(ContextEvent oldEvent, ContextEvent newEvent) {
        Map<String, String> oldProperties = oldEvent.getProperties();
        oldProperties.remove(PROPERTY_KEY_ID);
        Map<String, String> newProperties = newEvent.getProperties();
        newProperties.remove(PROPERTY_KEY_ID);
        for (Entry<String, String> set : newProperties.entrySet()) {
        	String oldValue = oldProperties.get(set.getKey());
        	String newValue = newProperties.get(set.getKey());
//        	Log.d(TAG, oldValue + " " + newValue);
        	if(!oldValue.equals(newValue)) {
//            	Log.d(TAG, "FALSE: " + oldValue + " " + newValue);
        		return false;
        	}
        }

        return true;
    }
	
	public boolean checkDeviceRooted() {
		return RootTools.isRootAvailable();
	}

	public boolean checkRootPermissionGiven() {
		try{
			return RootTools.isAccessGiven();
		}catch (Exception e) {
			return false;
		}
	}

	public String getIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress().toUpperCase();
						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (useIPv4) {
							if (isIPv4)
								return sAddr;
						} else {
							if (!isIPv4) {
								int delim = sAddr.indexOf('%');
								return delim < 0 ? sAddr : sAddr.substring(0,
										delim);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return "";
	}

	public boolean isPasswordProtected() {
		KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

		return keyguardManager.isKeyguardSecure();
	}

	private int getScreenTimeout() {
		return (Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 3000) / 1000);
	}

	public boolean isTrustedAntiVirInstalled() {
		PackageManager packageManager = context.getPackageManager();
		List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
		for (ApplicationInfo appInfo : installedApps) {
			String appName = appInfo.loadLabel(packageManager).toString();

			for (String trustedAVName : trustedAVs) {
				if (trustedAVName.equals(appName)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean musesDatabaseExist(Context context, String dbName) {
	    File dbFile = context.getDatabasePath(dbName);
	    return dbFile.exists();
	}

	private boolean isAccessibilityForMusesEnabled() {
		int accessibilityEnabled = 0;
		try {
	        accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
	    } catch (SettingNotFoundException e) {
	        Log.d(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
	    }
		
		if (accessibilityEnabled==1){
	        String settingValue = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
	         
	         for (String name : settingValue.split(":")) {
	        	 if(name.contains(InteractionSensor.class.getName())) {
	        		 return true;
	        	 }
	        	 Log.d(TAG, name);
			}
		}
		else {
	        return false;
		}
		
		return false;
	}
	
	@Override
	public void configure(List<SensorConfiguration> config) {
		for (SensorConfiguration item : config) {
			Log.d(TAG, "DEVICE CONFIG TEST: key=" + item.getKey() + ", value="+item.getValue());
			if(item.getKey().equals(CONFIG_KEY_TRUSTED_AV)) {
				trustedAVs.add(item.getValue());
			}
		}
	}

	@Override
	public void disable() {
		if (sensorEnabled) {
			sensorEnabled = false;
		}
	}

	@Override
	public void addContextListener(ContextListener listener) {
		this.listener = listener;
	}

	@Override
	public void removeContextListener(ContextListener listener) {
		this.listener = listener;
	}

	@Override
	public ContextEvent getLastFiredContextEvent() {
		if (contextEventHistory.size() > 0) {
			return contextEventHistory.get(contextEventHistory.size() - 1);
		} else {
			return null;
		}
	}

	private class CreateContextEventAsync extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			while (sensorEnabled) {
				createContextEvent();
				
				try {
					TimeUnit.MILLISECONDS.sleep(OBSERVATION_INTERVALL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}
}
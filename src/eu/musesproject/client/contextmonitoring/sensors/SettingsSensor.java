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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.client.db.entity.SensorConfiguration;
import eu.musesproject.contextmodel.ContextEvent;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author christophstanik
 *
 * Class to collect information about the decive configuration
 */

public class SettingsSensor implements ISensor {
    private static final String TAG = SettingsSensor.class.getSimpleName();

    // sensor identifier
    public static final String TYPE = "CONTEXT_SENSOR_SETTINGS";
    
    // context property keys
    public static final String PROPERTY_KEY_ID 					= "id";
    public static final String PROPERTY_KEY_OS_VERSION 			= "osversion";
    public static final String PROPERTY_KEY_SDK_VERSION 		= "sdkversion";
    public static final String PROPERTY_KEY_IMEI		 		= "imei";
    public static final String PROPERTY_KEY_DEVICE_MODEL_NAME   = "devicemodel";
    public static final String PROPERTY_KEY_MUSES_APP_VERSION   = "musesversion";

    private Context context;
    private ContextListener listener;

    // history of fired context events
    List<ContextEvent> contextEventHistory;

    // holds a value that indicates if the sensor is enabled or disabled
    private boolean sensorEnabled;
    private PackageManager packageManager;

    public SettingsSensor(Context context) {
        this.context = context;
        contextEventHistory = new ArrayList<ContextEvent>(CONTEXT_EVENT_HISTORY_SIZE);
        
        init();
    }
    
    private void init() {
        sensorEnabled = false;
        packageManager  = context.getPackageManager();
    	// create an initial context event since the information
    	// gathered by this sensor does not change often
    	createContextEvent();
    }

    @Override
    public void enable() {
    	if (!sensorEnabled) {
    		sensorEnabled = true;
    	}
    }

    private void createContextEvent() {
        // create context event
        ContextEvent contextEvent = new ContextEvent();
        contextEvent.setType(TYPE);
        contextEvent.setTimestamp(System.currentTimeMillis());
        contextEvent.addProperty(PROPERTY_KEY_ID, String.valueOf(contextEventHistory != null ? (contextEventHistory.size() + 1) : -1));
        contextEvent.addProperty(PROPERTY_KEY_OS_VERSION, getOSVersion());
        contextEvent.addProperty(PROPERTY_KEY_SDK_VERSION, getSDKVersion());
        contextEvent.addProperty(PROPERTY_KEY_IMEI, getIMEI());
        contextEvent.addProperty(PROPERTY_KEY_DEVICE_MODEL_NAME, getDeviceName());
        contextEvent.addProperty(PROPERTY_KEY_MUSES_APP_VERSION, getMusesAppVersion());
        contextEvent.generateId();
        Log.d("test_version_name", "version=" + getMusesAppVersion());

        if (listener != null) {
            listener.onEvent(contextEvent);
        }
    }

    @Override
    public void disable() {
    	if(sensorEnabled) {
    		sensorEnabled = false;
    	}
    }
    
    public static String getOSVersion() {
    	return android.os.Build.VERSION.RELEASE;
    }
    
    public String getSDKVersion() {
    	return String.valueOf(android.os.Build.VERSION.SDK_INT);
    }
    
    public String getIMEI() {
    	String imei = null; 
    	
    	final TelephonyManager telephonyManager = (TelephonyManager) context
    			.getSystemService(Context.TELEPHONY_SERVICE);
    	
		if (telephonyManager != null) {
			imei =telephonyManager.getDeviceId();
			
		}
		if (imei == null || imei.length() == 0) {
            imei = Secure.getString(context.getContentResolver(),
                    Secure.ANDROID_ID);
            imei = new BigInteger(imei, 16).longValue()+"";
        }
		return imei;
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
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
        if(contextEventHistory.size() > 0) {
            return contextEventHistory.get(contextEventHistory.size() - 1);
        }
        else {
            return null;
        }
    }
    
	@Override
	public void configure(List<SensorConfiguration> config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSensorType() {
		return TYPE;
	}

    public String getMusesAppVersion() {
        try {
            PackageInfo info = packageManager.getPackageInfo(context.getPackageName(), 0);
            String versionName = info.versionName;

            return  versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "unknown";
        }
    }
}
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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.stericson.RootTools.RootTools;

import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.contextmodel.ContextEvent;

/**
 * @author christophstanik
 *
 * Class to collect information about the decive configuration
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class DeviceProtectionSensor implements ISensor {
    private static final String TAG = DeviceProtectionSensor.class.getSimpleName();

    // sensor identifier
    public static final String TYPE = "CONTEXT_SENSOR_DEVICE_PROTECTION";
    
    // context property keys
    public static final String PROPERTY_KEY_ID 					= "id";
    public static final String PROPERTY_KEY_OS_VERSION 			= "osversion";
    public static final String PROPERTY_KEY_SDK_VERSION 		= "sdkversion";
    public static final String PROPERTY_KEY_IMEI		 		= "imei";

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
        mockTrustedDevices();
    	// create an initial context event since the information
    	// gathered by this sensor does not change often
    	createContextEvent();
    }

    private void mockTrustedDevices() {
        trustedAVs.add("avast! Mobile Security");
        trustedAVs.add("Mobile Security & Antivirus");
        trustedAVs.add("Avira Antivirus Security");
        trustedAVs.add("Norton Security & Antivirus");
        trustedAVs.add("CM Security & Find My Phone");
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
    	
        if (listener != null) {
            listener.onEvent(contextEvent);
        }
    }
    

    public boolean checkDeviceRooted() {
        return RootTools.isRootAvailable();
    }

    public boolean checkRootPermissionGiven() {
        return RootTools.isAccessGiven();
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
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    public boolean isPasswordProtected() {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        
        return keyguardManager.isKeyguardSecure();
    }

    public boolean isTrustedAntiVirInstalled() {
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for(ApplicationInfo appInfo : installedApps) {
            String appName = appInfo.loadLabel(packageManager).toString();

            for(String trustedAVName : trustedAVs) {
                if(trustedAVName.equals(appName)) {
//                    Log.w(getClass().getSimpleName(), "trusted AV name: " + appName);
                    return true;
                }
            }
        }
        return false;
    }
    

    @Override
    public void disable() {
    	if(sensorEnabled) {
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
        if(contextEventHistory.size() > 0) {
            return contextEventHistory.get(contextEventHistory.size() - 1);
        }
        else {
            return null;
        }
    }
}
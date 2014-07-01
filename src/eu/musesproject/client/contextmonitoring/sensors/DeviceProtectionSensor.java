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
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.stericson.RootTools.RootTools;

import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.contextmodel.ContextEvent;

/**
 * @author christophstanik
 * 
 *         Class to collect information about the device configuration
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class DeviceProtectionSensor implements ISensor {
	private static final String TAG = DeviceProtectionSensor.class.getSimpleName();

	// sensor identifier
	public static final String TYPE = "CONTEXT_SENSOR_DEVICE_PROTECTION";

	// context property keys
	public static final String PROPERTY_KEY_ID = "id";
	public static final String PROPERTY_KEY_IS_ROOTED = "isrooted";
	public static final String PROPERTY_KEY_IS_ROOT_PERMISSION_GIVEN = "isrootpermissiongiven";
	public static final String PROPERTY_KEY_IP_ADRESS = "ipaddress";
	public static final String PROPERTY_KEY_IS_PASSWORD_PROTECTED = "ispasswordprotected";
	public static final String PROPERTY_KEY_SCREEN_TIMEOUT_IN_SECONDS = "screentimeoutinseconds";
	public static final String PROPERTY_KEY_IS_TRUSTED_AV_INSTALLED = "istrustedantivirusinstalled";

	private Context context;
	private ContextListener listener;

	// history of fired context events
	List<ContextEvent> contextEventHistory;

	// holds a value that indicates if the sensor is enabled or disabled
	private boolean sensorEnabled;

	// list with names of trusted anti virus applications
	private List<String> trustedAVs;

    private boolean initialContextEventFired;
    
	public DeviceProtectionSensor(Context context) {
		this.context = context;
		contextEventHistory = new ArrayList<ContextEvent>(CONTEXT_EVENT_HISTORY_SIZE);

		init();
	}

	private void init() {
		sensorEnabled = false;

		trustedAVs = new ArrayList<String>();
		mockTrustedDevices();
		
        initialContextEventFired = false;
	}
	
	private void createInitialContextEvent() {
        // create a list of installed ups and hold it
		if(!initialContextEventFired) {
			// create an initial context event since the information
			// gathered by this sensor does not change often
			new CreateContextEventAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			
			initialContextEventFired = true;
		}
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
			
            createInitialContextEvent();
		}
	}

	private void createContextEvent() {
		// create context event
		ContextEvent contextEvent = new ContextEvent();
		contextEvent.setType(TYPE);
		contextEvent.setTimestamp(System.currentTimeMillis());
		contextEvent.addProperty(PROPERTY_KEY_ID, String.valueOf(contextEventHistory != null ? (contextEventHistory .size() + 1) : -1));
		contextEvent.addProperty(PROPERTY_KEY_IS_ROOTED, String.valueOf(checkDeviceRooted()));
		contextEvent.addProperty(PROPERTY_KEY_IS_ROOT_PERMISSION_GIVEN, String.valueOf(checkRootPermissionGiven()));
		contextEvent.addProperty(PROPERTY_KEY_IP_ADRESS, getIPAddress(true));
		contextEvent.addProperty(PROPERTY_KEY_IS_PASSWORD_PROTECTED, String.valueOf(isPasswordProtected()));
		contextEvent.addProperty(PROPERTY_KEY_SCREEN_TIMEOUT_IN_SECONDS, String.valueOf(getScreenTimeout()));
		contextEvent.addProperty(PROPERTY_KEY_IS_TRUSTED_AV_INSTALLED, String.valueOf(isTrustedAntiVirInstalled()));
		
		Log.d(TAG, "context event created");

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
								int delim = sAddr.indexOf('%');
								return delim < 0 ? sAddr : sAddr.substring(0,
										delim);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		} // for now eat exceptions
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
			if (sensorEnabled) {
				createContextEvent();
			}
			return null;
		}
	}
}
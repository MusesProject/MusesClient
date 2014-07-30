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

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.client.db.entity.SensorConfiguration;
import eu.musesproject.contextmodel.ContextEvent;
import eu.musesproject.contextmodel.PackageStatus;

/**
 * @author christophstanik
 *
 * Class to collect information about app installation status of the device.
 * The class collects information about:
 *  - new app installed
 *  - already installed app updated
 *  - app deleted
 *  - list of all installed apps
 */
public class PackageSensor implements ISensor {
    private static final String TAG = PackageSensor.class.getSimpleName();


    // sensor identifier
    public static final String TYPE = "CONTEXT_SENSOR_PACKAGE";

    private static final String INITIAL_STARTUP 				= "init";

    // context property keys
    public static final String PROPERTY_KEY_ID 					= "id";
    public static final String PROPERTY_KEY_PACKAGE_STATUS		= "packagestatus";
    public static final String PROPERTY_KEY_PACKAGE_NAME		= "packagename";
    public static final String PROPERTY_KEY_APP_NAME 			= "appname";
    public static final String PROPERTY_KEY_APP_VERSION			= "appversion";
    public static final String PROPERTY_KEY_INSTALLED_APPS   	= "installedapps";

    private boolean sensorEnabled;

    private Context context;
    private ContextListener listener;

    // stores the context events before the latest at position 0 and the latest at position 1
    private List<ContextEvent> contextEventHistory;

    // broadcast receiver fields
    final PackageBroadcastReceiver packageReceiver = new PackageBroadcastReceiver();
    final IntentFilter packageAddedIntentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
    final IntentFilter packageRemovedIntentFilter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
    final IntentFilter packageUpdatedIntentFilter = new IntentFilter(Intent.ACTION_PACKAGE_REPLACED);
    
    private boolean initialContextEventFired;


    public PackageSensor(Context context) {
        this.context = context;
        init();
    }
    
	private void init() {
        sensorEnabled = false;
        contextEventHistory = new ArrayList<ContextEvent>(CONTEXT_EVENT_HISTORY_SIZE);
        initialContextEventFired = false;
    }
	
	private void createInitialContextEvent() {
        // create a list of installed ups and hold it
		if(!initialContextEventFired) {
			String[] params = new String[4];
			params[0] = INITIAL_STARTUP;
			params[1] = INITIAL_STARTUP;
			params[2] = INITIAL_STARTUP;
			params[3] = "-1";
			
			new CreateContextEventAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
			
			initialContextEventFired = true;
		}
	}

    @Override
    public void enable() {
        if(!sensorEnabled) {
            Log.d(TAG, "start package sensor");
            sensorEnabled = true;
            registerPackageBroadcastReceiver();
            createInitialContextEvent();
        }
    }

    @Override
    public void disable() {
        if(sensorEnabled) {
            Log.d(TAG, "stop package sensor");
            sensorEnabled = false;
            unregisterPackageBroadcastReceiver();
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

    private void createContextEvent(String packageStatus, String packageName, String appName, String appVersion) {
        ContextEvent contextEvent = new ContextEvent();
        contextEvent.setType(TYPE);
        contextEvent.setTimestamp(System.currentTimeMillis());
        contextEvent.addProperty(PROPERTY_KEY_ID, String.valueOf(contextEventHistory != null ? (contextEventHistory.size() + 1) : -1));
        contextEvent.addProperty(PROPERTY_KEY_PACKAGE_STATUS, packageStatus);
        contextEvent.addProperty(PROPERTY_KEY_PACKAGE_NAME, packageName);
        contextEvent.addProperty(PROPERTY_KEY_APP_NAME, appName);
        contextEvent.addProperty(PROPERTY_KEY_APP_VERSION, appVersion);
        contextEvent.addProperty(PROPERTY_KEY_INSTALLED_APPS, getInstalledApps());
        
        // just for debugging/testing purpose
//        Iterator it = contextEvent.getProperties().entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pairs = (Map.Entry)it.next();
//            Log.d("TESt", "package sensor result: " + pairs.getKey() + " = " + pairs.getValue());
//            it.remove(); // avoids a ConcurrentModificationException
//        }
        
        // add context event to the context event history
        contextEventHistory.add(contextEvent);
        if(contextEventHistory.size() > CONTEXT_EVENT_HISTORY_SIZE) {
            contextEventHistory.remove(0);
        }

        if(listener != null) {
            listener.onEvent(contextEvent);
        }
    }

    private void registerPackageBroadcastReceiver() {
        packageAddedIntentFilter.addDataScheme("package");
        packageRemovedIntentFilter.addDataScheme("package");
        packageUpdatedIntentFilter.addDataScheme("package");

        context.registerReceiver(packageReceiver, packageAddedIntentFilter);
        context.registerReceiver(packageReceiver, packageRemovedIntentFilter);
        context.registerReceiver(packageReceiver, packageUpdatedIntentFilter);
    }

    private String getInstalledApps() {
        String installedAppsFormatted = "";

        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        PackageInfo pInfo; 
        for(ApplicationInfo appInfo : installedApps) {
            String appName = appInfo.loadLabel(packageManager).toString();
            String packageName = appInfo.packageName;
            String appVersion;
            try {
				appVersion = String.valueOf(packageManager.getPackageInfo(packageName, 0).versionCode);
			} catch (NameNotFoundException e) {
				appVersion = "- 1";
			}
            installedAppsFormatted += appName + "," + packageName + "," + appVersion;
            installedAppsFormatted += ";";
        }
        // remove last separation item
        installedAppsFormatted = installedAppsFormatted.subSequence(0, installedAppsFormatted.length() - 1).toString();

        return installedAppsFormatted;
    }

    private void unregisterPackageBroadcastReceiver() {
        try {
            context.unregisterReceiver(packageReceiver);
        }
        catch(IllegalArgumentException e) {
            Log.d(TAG, "package receiver is already unregistered");
        }
    }

    public class PackageBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
            	// retrieve the package name
            	Uri uri = intent.getData();
            	String packageName = uri != null ? uri.getSchemeSpecificPart() : "unknown";
              
            	// procedure to retrieve the application name
            	PackageManager pm = context.getPackageManager();
            	ApplicationInfo ai;
            	try {
            		ai = pm.getApplicationInfo(packageName, 0);
            	} catch (final NameNotFoundException e) {
            		ai = null;
            	}
            	String appName = (String) (ai != null ? pm.getApplicationLabel(ai) : "unknown");
              
            	// procedure to retrieve the version code of the application
            	String appVersion;
            	try {
            		appVersion = String.valueOf(pm.getPackageInfo(packageName, 0).versionCode);
            	} catch (NameNotFoundException e) {
            	  appVersion = "- 1";
            	}
                  
            	// identify the package status
                final String action = intent.getAction();
                PackageStatus packageStatus = null;
                if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                    packageStatus = PackageStatus.INSTALLED;
                } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                	packageStatus = PackageStatus.REMOVED;
                } else if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
                	packageStatus = PackageStatus.UPDATED;
                }
                String packageStatusName = packageStatus != null ? packageStatus.toString() : "unknown";
                
                // prepare the context event creation
                String[] params = new String[4];
                params[0] = packageStatusName;
                params[1] = packageName;
                params[2] = appName;
                params[3] = appVersion;
                
                new CreateContextEventAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
            }
        }
    }
    

	private class CreateContextEventAsync extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			if (sensorEnabled) {
				if(params.length == 4) {
					createContextEvent(params[0], params[1], params[2], params[3]);
				}
			}
			return null;
		}
	}


	@Override
	public void configure(List<SensorConfiguration> config) {
		// TODO Auto-generated method stub
		
	}
}
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

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import eu.musesproject.client.R;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.contextmodel.ContextEvent;

/**
 * @author christophstanik
 *
 * Class to collect information about the currently used app by the user.
 * The class collects information about:
 *  - foreground app name
 *  - background processes name
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AppSensor implements ISensor {
    private static final String TAG = AppSensor.class.getSimpleName();

    // sensor identifier
    public static final String TYPE = "CONTEXT_SENSOR_APP";

    // time in milliseconds when the sensor polls information
    private static int OBSERVATION_INTERVALL = 500;

    // maximal number of how many background services are stored if a context event is fired
    private static final int MAX_SHOWN_BACKGROUND_SERVICES = 100;

    // context property keys
    public static final String PROPERTY_KEY_ID 					= "id";
    public static final String PROPERTY_KEY_APP_NAME 			= "appname";
    public static final String PROPERTY_KEY_BACKGROUND_PROCESS 	= "backgroundprocess";

    private Context context;
    private ContextListener listener;

    // stores all fired context events of this sensor
    private List<ContextEvent> contextEventHistory;

    // get current application information
    private ActivityManager activityManager;

    // holds a value that indicates if the sensor is enabled or disabled
    private boolean sensorEnabled;
    
    public AppSensor(Context context) {
        this.context = context;
        init();
    }

    // initializes all necessary default values
    private void init() {
        sensorEnabled = false;
        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        contextEventHistory = new ArrayList<ContextEvent>(CONTEXT_EVENT_HISTORY_SIZE);
    }

    /**
     * creates the context event for this sensor and saves it in the
     * context event history
     * @param runningServices list of background services
     * @param appName name of the currently active application
     */
    private void createContextEvent(String appName, List<RunningServiceInfo> runningServices) {
        Log.d(TAG, "APP - context event created: " +appName);

        // get the running services
        List<String> runningServicesNames = new ArrayList<String>();
        for (RunningServiceInfo runningServiceInfo : runningServices) {
            runningServicesNames.add(runningServiceInfo.process);
        }

        // create the context event
        ContextEvent contextEvent = new ContextEvent();
        contextEvent.setType(TYPE);
        contextEvent.setTimestamp(System.currentTimeMillis());
        contextEvent.addProperty(PROPERTY_KEY_ID, String.valueOf(contextEventHistory != null ? (contextEventHistory.size() + 1) : -1));
        contextEvent.addProperty(PROPERTY_KEY_APP_NAME, appName);
        contextEvent.addProperty(PROPERTY_KEY_BACKGROUND_PROCESS, runningServicesNames.toString());

        // add context event to the context event history
        contextEventHistory.add(contextEvent);
        if(contextEventHistory.size() > CONTEXT_EVENT_HISTORY_SIZE) {
            contextEventHistory.remove(0);
        }

        if(listener != null) {
            //Log.d(TAG, "called: listener.onEvent(contextEvent);");
            listener.onEvent(contextEvent);
        }
    }

    @Override
    public void enable() {
        //Log.d(TAG, "app sensor enable");
        if (!sensorEnabled) {
            //Log.d(TAG, "start app tracking");
            sensorEnabled = true;
            new AppObserver().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void disable() {
        if(sensorEnabled) {
            //Log.d(TAG, "stop app tracking");
            sensorEnabled = false;
        }
    }

    /**
     * Observes the users application usage. Creates a context event whenever a new application is started.
     */
    private class AppObserver extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
        	String previousApp = "";

            while (sensorEnabled) {
                // get the first item in the list, because it is the foreground task
                RunningTaskInfo foregroundTaskInfo = activityManager.getRunningTasks(1).get(0);

                String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
                PackageManager pm = context.getPackageManager();
                PackageInfo foregroundAppPackageInfo;
                String foregroundTaskAppName = "";
                List<ActivityManager.RunningServiceInfo> runningServices = null;
                try {
                    foregroundAppPackageInfo = pm.getPackageInfo(foregroundTaskPackageName, 0);
                	foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo.loadLabel(pm).toString();
                	runningServices = activityManager.getRunningServices(MAX_SHOWN_BACKGROUND_SERVICES);

                    // fill previousApp with the first one in session
                    // and set the start time of the first application
                    if(previousApp.equals("")) {
                        createContextEvent(foregroundTaskAppName, runningServices);
                        previousApp = foregroundTaskAppName;
                    }

                    // if the foreground application changed, create a context event
                    if(!foregroundTaskAppName.equals(previousApp)) {
                        if(!foregroundTaskAppName.equals(context.getResources().getString(R.string.app_name))) {
                        	createContextEvent(foregroundTaskAppName, runningServices);
                        	previousApp = foregroundTaskAppName;
                        }
                    }

                    Thread.sleep(OBSERVATION_INTERVALL);
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
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
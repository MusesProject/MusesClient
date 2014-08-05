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
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.client.db.entity.SensorConfiguration;
import eu.musesproject.client.model.contextmonitoring.InteractionObservedApps;
import eu.musesproject.contextmodel.ContextEvent;

/**
 * The intention of this class is to be able to detect incoming emails.
 * This class is just working with Android API 18 or higher.
 * @author christophstanik
 */
public class NotificationSensor implements
		ISensor {
    private static final String TAG = NotificationSensor.class.getName();

    // sensor identifier
    public static final String TYPE = "CONTEXT_SENSOR_NOTIFICATION";
    
    // context property keys
    public static final String PROPERTY_KEY_ID 					= "id";
    public static final String PROPERTY_KEY_PACKAGE_NAME 		= "package";
    public static final String PROPERTY_KEY_TICKER_TEXT			= "tickertext";
    
    private Context context;
    private Intent notificationSensorIntent;
    private ContextListener listener;

    // history of fired context events
    List<ContextEvent> contextEventHistory;
    
    // holds a value that indicates if the sensor is enabled or disabled
    private boolean sensorEnabled;

    public NotificationSensor(Context context) {
    	this.context = context;
        init();
    }

    // initializes all necessary default values
    private void init() {
        notificationSensorIntent = new Intent(context, NotificationSensor.NotificationService.class);
        contextEventHistory = new ArrayList<ContextEvent>(CONTEXT_EVENT_HISTORY_SIZE);
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
	public void enable() {
        if (!sensorEnabled) {
            context.startService(notificationSensorIntent);
            sensorEnabled = true;
        }
	}

	@Override
	public void disable() {
		if(sensorEnabled) {
			context.stopService(notificationSensorIntent);
            sensorEnabled = false;
        }
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static class NotificationService extends NotificationListenerService {

		public NotificationService() {
		}
		
		@Override
		public void onNotificationPosted(StatusBarNotification sbn) {
			if(sbn.getPackageName().equals(InteractionObservedApps.OBSERVED_GMAIL)) {
				
			}
			Log.d(TAG, "notification posted 1/4: " + sbn.getPackageName());
			Log.d(TAG, "notification posted 2/4: " + sbn.getNotification().tickerText);
			Log.d(TAG, "notification posted 3/4:" + sbn.getNotification().toString());
		}

		@Override
		public void onNotificationRemoved(StatusBarNotification sbn) {
			Log.d(TAG, "notification removed: " + sbn.getPackageName());
		}
	}

	@Override
	public void configure(List<SensorConfiguration> config) {
		// TODO Auto-generated method stub
		
	}
}
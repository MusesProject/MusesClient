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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.client.db.entity.SensorConfiguration;
import eu.musesproject.contextmodel.ContextEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christophstanik on 4/23/15.
 */
public class PeripheralSensor implements ISensor {
    private static final String TAG = PeripheralSensor.class.getSimpleName();

    // sensor identifier
    public static final String TYPE = "CONTEXT_SENSOR_PERIPHERAL";

    // context property keys
    public static final String PROPERTY_KEY_ID 				  = "id";
    public static final String PROPERTY_KEY_CONNECTED_VIA_USB = "connected_via_usb";

    private Context context;
    private ContextListener listener;

    // stores all fired context events of this sensor
    private List<ContextEvent> contextEventHistory;

    private final PeripheralStateChangedReceiver receiver = new PeripheralStateChangedReceiver();

    private boolean previousConnectionStatusValue = false;

    // holds a value that indicates if the sensor is enabled or disabled
    private boolean sensorEnabled;

    public PeripheralSensor(Context context) {
        this.context = context;
        init();
    }

    // initializes all necessary default values
    private void init() {
        sensorEnabled = false;
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
    public void enable() {
        if (!sensorEnabled) {
            sensorEnabled = true;

            // register broadcast receiver here
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.hardware.usb.action.USB_STATE");
            context.registerReceiver(receiver, filter);
        }
    }

    @Override
    public void disable() {
        if(sensorEnabled) {
            sensorEnabled = false;

            // unregister broadcast receiver here
            context.unregisterReceiver(receiver);
        }
    }

    private void createContextEvent(boolean connectionStatus) {
        if(previousConnectionStatusValue != connectionStatus) {
            ContextEvent contextEvent = new ContextEvent();
            contextEvent.setType(TYPE);
            contextEvent.setTimestamp(System.currentTimeMillis());
            contextEvent.addProperty(PROPERTY_KEY_CONNECTED_VIA_USB, String.valueOf(connectionStatus));
            contextEvent.generateId();

            // add context event to the context event history
            contextEventHistory.add(contextEvent);
            if (contextEventHistory.size() > CONTEXT_EVENT_HISTORY_SIZE) {
                contextEventHistory.remove(0);
            }

            if (listener != null) {
                listener.onEvent(contextEvent);
            }
        }
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
        // nothing to configure here
    }

    @Override
    public String getSensorType() {
        return TYPE;
    }


    public class PeripheralStateChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Bundle extras = intent.getExtras();
                createContextEvent(extras.getBoolean("connected"));
            } catch (Exception e) {
                Log.e(TAG, "not able to read the broadcast event");
            }
        }
    }
}
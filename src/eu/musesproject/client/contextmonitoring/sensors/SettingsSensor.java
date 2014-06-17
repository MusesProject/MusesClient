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

import android.content.Context;
import android.telephony.TelephonyManager;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.contextmodel.ContextEvent;

/**
 * @author christophstanik
 *
 * Class to collect information about the decive configuration
 */

public class SettingsSensor implements ISensor {
    private static final String TAG = SettingsSensor.class.getSimpleName();

    public static final String TYPE = "CONTEXT_SENSOR_SETTINGS";

    private Context context;
    private ContextListener listener;

    // history of fired context events
    List<ContextEvent> contextEventHistory;

    public SettingsSensor(Context context) {
        this.context = context;
        contextEventHistory = new ArrayList<ContextEvent>(CONTEXT_EVENT_HISTORY_SIZE);
    }

    @Override
    public void enable() {

    }

    private void createContextEvent() {
        if (listener != null) {
            listener.onEvent(null);
        }
    }

    @Override
    public void disable() {

    }

    private void getIMEI() {
        final TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.getDeviceId();
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
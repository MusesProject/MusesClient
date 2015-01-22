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

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.client.db.entity.SensorConfiguration;
import eu.musesproject.client.model.contextmonitoring.BluetoothState;
import eu.musesproject.contextmodel.ContextEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author christophstanik
 *
 * Class to collect information about the connection of
 * the device
 */

public class ConnectivitySensor implements ISensor {
    private static final String TAG = ConnectivitySensor.class.getSimpleName();

    // time in seconds when the sensor polls information
    private static final long OBSERVATION_INTERVALL = TimeUnit.SECONDS.toMillis(10);

    // sensor identifier
    public static final String TYPE = "CONTEXT_SENSOR_CONNECTIVITY";

    // context property keys
    public static final String PROPERTY_KEY_ID 				  	= "id";
    public static final String PROPERTY_KEY_MOBILE_CONNECTED   	= "mobileconnected";
    public static final String PROPERTY_KEY_WIFI_ENABLED 	  	= "wifienabled";
    public static final String PROPERTY_KEY_WIFI_CONNECTED 	  	= "wificonnected";
    public static final String PROPERTY_KEY_WIFI_NEIGHBORS 	  	= "wifineighbors";
    public static final String PROPERTY_KEY_HIDDEN_SSID 		= "hiddenssid";
    public static final String PROPERTY_KEY_BSSID 			  	= "bssid";
    public static final String PROPERTY_KEY_NETWORK_ID 		  	= "networkid";
    public static final String PROPERTY_KEY_BLUETOOTH_CONNECTED	= "bluetoothconnected";
    public static final String PROPERTY_KEY_AIRPLANE_MODE 	  	= "airplanemode";
    public static final String PROPERTY_WIFI_ENCRYPTION         = "wifiencryption";

    // application context
    private Context context;
    private ContextListener listener;

    // stores all fired context events of this sensor
    private List<ContextEvent> contextEventHistory;

    // connectivity info API
    private ConnectivityManager connectivityManager ;
    private WifiManager wifiManager;
    private BluetoothAdapter bluetoothAdapter;

    // enable and disable sensor
    private boolean sensorEnabled;


    public ConnectivitySensor(Context context) {
        this.context = context;
        init();
    }

    // initializes all necessary default values
    private void init() {
        sensorEnabled = false;

        contextEventHistory = new ArrayList<ContextEvent>(CONTEXT_EVENT_HISTORY_SIZE);

        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    @Override
    public void enable() {
        if (!sensorEnabled) {
            Log.d(TAG, "start connectivity tracking");
            sensorEnabled = true;
            new ConnectivityObserver().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void disable() {
        if(sensorEnabled) {
            Log.d(TAG, "stop connectivity tracking");
            sensorEnabled = false;
        }
    }

    /** adds the context event to the context event history */
    private void createContextEvent(ContextEvent contextEvent) {
    	contextEvent.generateId();
        Log.d(TAG, "Connectivity  - context event created");

        // add context event to the context event history
        contextEventHistory.add(contextEvent);
        if(contextEventHistory.size() > CONTEXT_EVENT_HISTORY_SIZE) {
            contextEventHistory.remove(0);
        }

        if(listener != null) {
            listener.onEvent(contextEvent);
        }
    }

    private boolean identicalContextEvent(ContextEvent oldEvent, ContextEvent newEvent) {
        if(oldEvent.getProperties().size() != newEvent.getProperties().size()) {
            return false;
        }

        // compare property values
        Set<String> oldValues = new HashSet<String>(oldEvent.getProperties().values());
        Set<String> newValues = new HashSet<String>(newEvent.getProperties().values());

        return oldValues.equals(newValues);
    }

    /**
     * Observes the smartphone's connectivity status. Creates a context event whenever a the connectivity status changes.
     */
    @SuppressLint("NewApi")
	public class ConnectivityObserver extends AsyncTask<Void, Void, Void> {
        @SuppressLint("InlinedApi")
        @Override
        protected Void doInBackground(Void... params) {
            while (sensorEnabled) {
                NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                // WiFi status
                int id = contextEventHistory != null ? (contextEventHistory.size() + 1) : - 1;

                ContextEvent contextEvent = new ContextEvent();
                contextEvent.setType(TYPE);
                contextEvent.setTimestamp(System.currentTimeMillis());
                contextEvent.addProperty(PROPERTY_KEY_ID, String.valueOf(id));
                if (mobileNetworkInfo != null)
                    contextEvent.addProperty(PROPERTY_KEY_MOBILE_CONNECTED, String.valueOf(mobileNetworkInfo.isConnected()));
                contextEvent.addProperty(PROPERTY_KEY_WIFI_ENABLED, String.valueOf(wifiManager.isWifiEnabled()));
                if(wifiManager.isWifiEnabled()) {
                    contextEvent.addProperty(PROPERTY_KEY_WIFI_NEIGHBORS, String.valueOf(wifiManager.getScanResults().size()));
                }
                contextEvent.addProperty(PROPERTY_KEY_WIFI_CONNECTED, String.valueOf(wifiNetworkInfo.isConnected()));

                // wifi encryption status
                List<ScanResult> networkList = wifiManager.getScanResults();
                String wifiEncryption = "unknown";
                if (networkList != null) {
                    for (ScanResult network : networkList) {
                        if (network.BSSID.equals(wifiInfo.getBSSID())){
                            wifiEncryption = network.capabilities;
                            Log.d(TAG, "Connectivity  - wifiencryption: " + wifiEncryption);
                        }
                    }
                }
                contextEvent.addProperty(PROPERTY_WIFI_ENCRYPTION, wifiEncryption);
                contextEvent.addProperty(PROPERTY_KEY_BSSID, String.valueOf(wifiInfo.getBSSID()));
                contextEvent.addProperty(PROPERTY_KEY_HIDDEN_SSID, String.valueOf(wifiInfo.getHiddenSSID()));
                contextEvent.addProperty(PROPERTY_KEY_NETWORK_ID, String.valueOf(wifiInfo.getNetworkId()));

                // bluetooth
                // Has to be rewritten
                BluetoothState bluetoothState = BluetoothState.FALSE;
                if (bluetoothAdapter == null) {
                    bluetoothState = BluetoothState.NOT_SUPPORTED;
                } else {
                    // check for paired devices to get the connected status
                    if (bluetoothAdapter.getBondedDevices().size() > 0) {
                        bluetoothState = BluetoothState.TRUE;
                    }
                }
                contextEvent.addProperty(PROPERTY_KEY_BLUETOOTH_CONNECTED,String.valueOf(bluetoothState));

                // Airplane mode
                boolean airplaneMode = false;
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                	airplaneMode = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
                }
                else {
                	airplaneMode = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
                }
                
                contextEvent.addProperty(PROPERTY_KEY_AIRPLANE_MODE, String.valueOf(airplaneMode));

                // check if something has changed. If something changed fire a context event, do nothing otherwise.
                int connectivityContextListSize = contextEventHistory.size();
                if(connectivityContextListSize > 0) {
                    ContextEvent previousContext = contextEventHistory.get(connectivityContextListSize - 1);
                    // fire new context event if a connectivity context field changed
                    if(!identicalContextEvent(previousContext, contextEvent)) {
                        createContextEvent(contextEvent);
                    }
                }
                else {
                    createContextEvent(contextEvent);
                }

                try {
                    Thread.sleep(OBSERVATION_INTERVALL);
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

	@Override
	public void configure(List<SensorConfiguration> config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSensorType() {
		return TYPE;
	}
}
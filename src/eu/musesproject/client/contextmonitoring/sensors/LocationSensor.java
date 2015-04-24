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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.client.db.entity.SensorConfiguration;
import eu.musesproject.client.model.contextmonitoring.Zone;
import eu.musesproject.contextmodel.ContextEvent;

import java.util.ArrayList;
import java.util.List;

public class LocationSensor implements ISensor, LocationListener {
    private static final String TAG = LocationSensor.class.getSimpleName();

    // sensor identifier
    public static final String TYPE = "CONTEXT_SENSOR_LOCATION";

    // time in milliseconds when the sensor polls information
    private static int OBSERVATION_INTERVAL                 = 600000;

    // context property keys
    public static final String PROPERTY_KEY_ID 			    = "hashid";
    public static final String PROPERTY_KEY_IS_WITHIN_ZONE  = "isWithinZone";

    // config keys
    public static final String CONFIG_KEY_ZONE              = "zone";
    public static final int CONFIG_KEY_DESCRIPTION_POS      = 0;
    public static final int CONFIG_KEY_ZONE_ID_POS          = 1;
    public static final int CONFIG_KEY_RADIUS_POS           = 2;
    public static final int CONFIG_KEY_LATITUDE_POS         = 3;
    public static final int CONFIG_KEY_LONGITUDE_POS        = 4;

    // default values
    public static final String DEFAULT_USER_IS_IN_NO_ZONE   = "no_zone";

    private Context context;
    private ContextListener listener;

    // history of fired context events
    List<ContextEvent> contextEventHistory;

    // holds a value that indicates if the sensor is enabled or disabled
    private boolean sensorEnabled;

    private LocationManager locationManager;
    private Location currentLocation;

    private List<Zone> zones;

    private boolean isNetworkEnabled = false;


    // configuration
    private static final long MIN_TIME_CHANGE_FOR_UPDATES = OBSERVATION_INTERVAL; // in milliseconds
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES =  0; // in meter


    public LocationSensor(Context context) {
        this.context = context;

        init();
    }

    private void init() {
        sensorEnabled = false;
        contextEventHistory = new ArrayList<ContextEvent>(CONTEXT_EVENT_HISTORY_SIZE);
        zones = new ArrayList<Zone>();
    }

    public Location getCurrentLocation() {
        Log.d(TAG, "getCurrentLocation called");
        // Get the location manager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(isNetworkEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_CHANGE_FOR_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this, Looper.getMainLooper());

            currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        // Initialize the location fields
        if (currentLocation != null) {
            createContextEvent();
        }

        return currentLocation;
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
            Log.d(TAG, "ENABLE");

            getCurrentLocation();
        }
    }

    @Override
    public void disable() {
        Log.d(TAG, "DISABLE");
        if (sensorEnabled) {
            sensorEnabled = false;
            locationManager.removeUpdates(this);
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
        for (SensorConfiguration sensorConfiguration : config) {
            if(sensorConfiguration.getKey().equals(CONFIG_KEY_ZONE)) {
                try {
                    String[] configItems = sensorConfiguration.getValue().split(";");
                    Zone zone = new Zone();
                    zone.setDescription(configItems[CONFIG_KEY_DESCRIPTION_POS]);
                    zone.setZoneId(Integer.parseInt(configItems[CONFIG_KEY_ZONE_ID_POS]));
                    zone.setRadius(Float.parseFloat(configItems[CONFIG_KEY_RADIUS_POS]));
                    zone.setLatitude(Double.parseDouble(configItems[CONFIG_KEY_LATITUDE_POS]));
                    zone.setLongitude(Double.parseDouble(configItems[CONFIG_KEY_LONGITUDE_POS]));

                    Log.d(TAG, configItems[CONFIG_KEY_DESCRIPTION_POS] + " lat=" + configItems[CONFIG_KEY_LATITUDE_POS] + " long=" + configItems[CONFIG_KEY_LONGITUDE_POS] + " radius=" +configItems[CONFIG_KEY_RADIUS_POS]);
                    zones.add(zone);
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        currentLocation = location;
        createContextEvent();
    }

    private void createContextEvent() {
        if(currentLocation != null) {
            String isInZones = "";
            for (Zone zone : zones) {
                Location location = new Location("zone_center");
                location.setLongitude(zone.getLongitude());
                location.setLatitude(zone.getLatitude());

                Log.d(TAG, "distance to " + zone.getDescription() + " is " + currentLocation.distanceTo(location) + "m");
                if(currentLocation.distanceTo(location) <= zone.getRadius()) {
                    isInZones += zone.getZoneId() + ",";
                }
            }

            if(isInZones.isEmpty()) {
                isInZones = DEFAULT_USER_IS_IN_NO_ZONE;
            }
            else {
                if(isInZones.contains(",")) {
                    isInZones = isInZones.substring(0, isInZones.length() - 1); // remove ',' as the last char
                }
            }
            Log.d(TAG, "is in zones: " + isInZones);

            ContextEvent contextEvent = new ContextEvent();
            contextEvent.setType(TYPE);
            contextEvent.setTimestamp(System.currentTimeMillis());
            contextEvent.addProperty(PROPERTY_KEY_IS_WITHIN_ZONE, isInZones);
            contextEvent.generateId();

            if (listener != null) {
                listener.onEvent(contextEvent);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public String getSensorType() {
        return TYPE;
    }
}
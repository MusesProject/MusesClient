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
import android.util.Log;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.client.db.entity.SensorConfiguration;
import eu.musesproject.client.utils.MusesUtils;
import eu.musesproject.contextmodel.ContextEvent;

import java.util.ArrayList;
import java.util.List;

public class LocationSensor implements ISensor, LocationListener {
	private static final String TAG = LocationSensor.class.getSimpleName();

	// sensor identifier
	public static final String TYPE = "CONTEXT_SENSOR_LOCATION";

    // context property keys
    public static final String PROPERTY_KEY_ID 						= "id";
    public static final String PROPERTY_KEY_IS_WITHIN_SECURE_ZONE	= "insecurezone";
    
    // config keys
    public static final String CONFIG_KEY_MIN_DIS 				= "mindistance";
    public static final String CONFIG_KEY_MIN_TIME				= "mindtime";
    public static final String CONFIG_KEY_LONGITUDE_SECURE_ZONE	= "locationlong";
    public static final String CONFIG_KEY_LATITUDE_SECURE_ZONE	= "locationlat";
    public static final String CONFIG_KEY_SECURE_ZONE_RADIUS	= "radius";
    

	private Context context;
	private ContextListener listener;

	// history of fired context events
	List<ContextEvent> contextEventHistory;

	// holds a value that indicates if the sensor is enabled or disabled
	private boolean sensorEnabled;

	private LocationManager locationManager;
	private String provider;

	// configuration
	Location allowedZoneCentralPoint;
	float allowedZoneRadius;
	int minTimeBetweenLocationUpdates; // in milliseconds
	int minDistanceBetweenLocationUpdates; // in meter
	boolean isWithinSecureZone;

	public LocationSensor(Context context) {
		this.context = context;
		
		init();
	}

	private void init() {
        sensorEnabled = false;
        contextEventHistory = new ArrayList<ContextEvent>(CONTEXT_EVENT_HISTORY_SIZE);
        
		allowedZoneRadius = 12.0f; // default radius; radius in m
		minTimeBetweenLocationUpdates = 400; // default, value in ms
		minDistanceBetweenLocationUpdates = 10;

		isWithinSecureZone = false; // default value

		// Get the location manager
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		provider = LocationManager.NETWORK_PROVIDER; // default location tracking accuracy -> Wi-Fi & Cell Tower
		Location location = locationManager.getLastKnownLocation(provider);

		// Initialize the location fields
		if (location != null) {
			onLocationChanged(location);
		} else {
			Log.d(TAG, "no location available");
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
	public void enable() {
//		Log.d(MusesUtils.TEST_TAG, "location=enable");
		if (!sensorEnabled && allowedZoneCentralPoint != null) {
			sensorEnabled = true;
			locationManager.requestLocationUpdates(
					provider, 
					minTimeBetweenLocationUpdates, 
					minDistanceBetweenLocationUpdates, 
					this
			);
		}
	}

	@Override
	public void disable() {
		if (sensorEnabled) {
			this.sensorEnabled = false;
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
//		Log.d(MusesUtils.TEST_TAG, "configure(List<SensorConfiguration> config)");
		try {
			for (SensorConfiguration item : config) {
				if(item.getKey().equals(CONFIG_KEY_MIN_DIS)) {
					minDistanceBetweenLocationUpdates = Integer.valueOf(item.getValue());
//					Log.d(MusesUtils.TEST_TAG, "minDistanceBetweenLocationUpdates="+minDistanceBetweenLocationUpdates);
				}
				else if(item.getKey().equals(CONFIG_KEY_MIN_TIME)) {
					minTimeBetweenLocationUpdates = Integer.valueOf(item.getValue());
//					Log.d(MusesUtils.TEST_TAG, "minTimeBetweenLocationUpdates="+minTimeBetweenLocationUpdates);
				}
				else if(item.getKey().equals(CONFIG_KEY_LONGITUDE_SECURE_ZONE)) {
					// just use this config item if longitude and latitude is provided
					for (SensorConfiguration subItem : config) {
						if(item.getKey().equals(CONFIG_KEY_LATITUDE_SECURE_ZONE)) {
							allowedZoneCentralPoint = new Location(provider);
							allowedZoneCentralPoint.setLongitude(Double.valueOf(item.getValue()));
							allowedZoneCentralPoint.setLatitude(Double.valueOf(subItem.getValue()));
//							Log.d(MusesUtils.TEST_TAG, "lat=" + allowedZoneCentralPoint.getLatitude() + " long=" +allowedZoneCentralPoint.getLongitude());
						}
					}
				}
				else if(item.getKey().equals(CONFIG_KEY_SECURE_ZONE_RADIUS)) {
					allowedZoneRadius = Float.valueOf(item.getValue());
//					Log.d(MusesUtils.TEST_TAG, "allowedZoneRadius=" + allowedZoneRadius);
				}
			}
		} catch (Exception e) {
//			Log.e(TAG, "could not map config value to correct type");
		}
	}

	@Override
	public void onLocationChanged(Location location) {
//		Log.d(TAG, "location sensor - : onLocationChanged");
		if (allowedZoneCentralPoint != null) {
			int distance = (int) location.distanceTo(allowedZoneCentralPoint);
			if (distance > allowedZoneRadius) {
//				Log.d(TAG, "location sensor - is within secure zone: " + false + "; distance=" + distance);
				if (isWithinSecureZone) { // just fire a context event if the user went from a secure zone to an insecure
					createContextEvent(false);
				}
			} else {
//				Log.d(TAG, "location sensor - is within secure zone: " + true + "; distance=" + distance);
				if (!isWithinSecureZone) { // just fire a context event if the user went from a insecure zone to a secure
					createContextEvent(true);
				}
			}
		}
	}

	private void createContextEvent(boolean isWithinSecureZone) {
		this.isWithinSecureZone = isWithinSecureZone;

		ContextEvent contextEvent = new ContextEvent();
		contextEvent.setType(TYPE);
		contextEvent.setTimestamp(System.currentTimeMillis());
		contextEvent.addProperty(PROPERTY_KEY_ID, String.valueOf(contextEventHistory != null ? (contextEventHistory.size() + 1) : -1));
		contextEvent.addProperty(PROPERTY_KEY_IS_WITHIN_SECURE_ZONE, String.valueOf(isWithinSecureZone));
		contextEvent.generateId();

		if (listener != null) {
			listener.onEvent(contextEvent);
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
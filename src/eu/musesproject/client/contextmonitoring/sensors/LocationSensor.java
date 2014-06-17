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

import java.util.Map;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.contextmodel.ContextEvent;

public class LocationSensor implements ISensor, LocationListener{
	private static final String TAG = LocationSensor.class.getSimpleName();
	
    private Context context;
    private ContextListener listener;

    // holds a value that indicates if the sensor is enabled or disabled
    private boolean sensorEnabled;

    private LocationManager locationManager;
    private String provider;

    // settings
    Location allowedZoneCentralPoint;
    float allowedZoneRadius;
    int minTimeBetweenLocationUpdates;

    public LocationSensor(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        allowedZoneRadius = 12.0f; // default radius; radius in m
        minTimeBetweenLocationUpdates = 400; // default, value in ms

        // Get the location manager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        provider = LocationManager.NETWORK_PROVIDER; // default location tracking accuracy -> Wi-Fi & Cell Tower
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            onLocationChanged(location);
        }
        else {
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
		this.sensorEnabled = true;
        locationManager.requestLocationUpdates(provider, 400, 1, this);
	}

	@Override
	public void disable() {
		this.sensorEnabled = false;
        locationManager.removeUpdates(this);
	}

	@Override
	public ContextEvent getLastFiredContextEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	public void configure(Map<String, String> config) {

    }
	
	@Override
	public void onLocationChanged(Location location) {
		if(allowedZoneCentralPoint != null) {
            int distance = (int) location.distanceTo(allowedZoneCentralPoint);
            if(distance > allowedZoneRadius) {
                //TODO create context event
            }
        }
		/*
        targetLocation = new Location("POINT_LOCATION");
        targetLocation.setLatitude(targetLat);
        targetLocation.setLongitude(targetLon);
        int distance = (int) location.distanceTo(targetLocation);
        distanceText.setText(distance + " m");

        if(distance > radius) {
            enterTextView.setText("outside");
        }
        else {
            enterTextView.setText("inside");
        }
        */
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
}
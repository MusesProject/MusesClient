package eu.musesproject.client.contextmonitoring.sensors;

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
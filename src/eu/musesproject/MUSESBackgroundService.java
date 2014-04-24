package eu.musesproject;

import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;
import eu.musesproject.client.contextmonitoring.service.aidl.MusesServiceProvider;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * This class is responsible to start the background
 * service which enables the application to run properly.
 * This service initializes the necessary code.
 * 
 * @author christophstanik
 *
 */
public class MUSESBackgroundService extends Service {
	private static final String TAG = MUSESBackgroundService.class.getSimpleName();
	
	private boolean isAppInitialized;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate() {
		isAppInitialized = false;
		UserContextMonitoringController.getInstance(this);
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "on startComment called");
		if(!isAppInitialized) {
			Toast.makeText(this, "MUSES started", Toast.LENGTH_LONG).show();
			isAppInitialized = true;
			UserContextMonitoringController.getInstance(this).startContextObservation();
		}
		startService(new Intent(this, MusesServiceProvider.class));

		return Service.START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		isAppInitialized = false;
		super.onDestroy();
	}
}
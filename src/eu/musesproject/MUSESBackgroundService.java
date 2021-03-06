package eu.musesproject;

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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import eu.musesproject.client.R;
import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;
import eu.musesproject.client.contextmonitoring.service.aidl.MusesServiceProvider;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.client.ui.MainActivity;
import eu.musesproject.client.usercontexteventhandler.UserContextEventHandler;
import eu.musesproject.client.utils.MusesUtils;

import java.util.HashMap;
import java.util.Map;

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

	public static final String INTENT_REBOOT = "INTENT_REBOOT";

	private UserContextEventHandler userContextEventHandler;

	private boolean isAppInitialized;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.d(MusesUtils.LOGIN_TAG, "MUSESBACKGROUNDService - onCreate");

		isAppInitialized = true;
		UserContextMonitoringController.getInstance(this);
		userContextEventHandler = UserContextEventHandler.getInstance();

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null && intent.hasExtra(INTENT_REBOOT) &&
				intent.getBooleanExtra(INTENT_REBOOT, false)) {
			isAppInitialized = false;
			Log.d(MusesUtils.LOGIN_TAG, "App initialized"); // FIXME
		}
		Log.d(MusesUtils.LOGIN_TAG, "MUSESBACKGROUNDService - on startComment called");
		if(!isAppInitialized) {
			Log.d(MusesUtils.LOGIN_TAG, "MUSESBACKGROUNDService - MUSES service started!!");
			isAppInitialized = true;
			Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
			mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mainActivityIntent.putExtra("is_from_service_restart", true);
			startActivity(mainActivityIntent);

			// try to auto login user
			userContextEventHandler.setContext(this);

			// send status of the service
			String actionDescription = getString(R.string.action_description_started);
			Map<String, String> properties = new HashMap<String, String>();
			properties.put("status", actionDescription);
			userContextEventHandler.send(createAction(actionDescription), properties, null);
		}
		userContextEventHandler.setContext(this);
		userContextEventHandler.manageMonitoringComponent();
		startService(new Intent(this, MusesServiceProvider.class));

		return Service.START_STICKY;
	}


	@Override
	public void onDestroy() {
		Log.v(MusesUtils.LOGIN_TAG, "MUSESBACKGROUNDService - onDestroy()");
		isAppInitialized = false;

		// send status of the service
		String actionDescription = getString(R.string.action_description_stopped);
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("status", actionDescription);
		userContextEventHandler.send(createAction(actionDescription), properties, null);

		super.onDestroy();
	}

	private Action createAction(String description) {
		Action action = new Action();
		action.setActionType(ActionType.MUSES_BACKGROUND_SERVICE);
		action.setTimestamp(System.currentTimeMillis());
		action.setDescription(description);

		return action;
	}
}
package eu.musesproject.client.contextmonitoring.service.aidl;

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
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.UserActionGenerator;
import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;
import eu.musesproject.client.model.actuators.ResponseInfoAP;
import eu.musesproject.client.model.contextmonitoring.UISource;
import eu.musesproject.client.ui.DebugFileLog;

import java.util.Map;

public class MusesServiceProvider extends Service {
	private static final String TAG = MusesServiceProvider.class.getSimpleName();
	private IMusesServiceCallback callback;

	@Override
	public IBinder onBind(Intent intent) {
		Log.e(TAG, "Came to Bind");
		try {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				String packageName = bundle.getString("package");
				if (packageName.equals("eu.musesproject.musesawareapp")){
					onStartCommand(null, 0, 0); // call to make service sticky
					return binder;
				}
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
		}
		return null;
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    private final IMusesService.Stub binder = new IMusesService.Stub() {

		@Override
		public void unregisterCallback(IMusesServiceCallback callback)
				throws RemoteException {
			MusesServiceProvider.this.callback = callback;
			Log.v(TAG, "callback unregistered");
			DebugFileLog.write(TAG+ " callback unregistered");
		}	
		
		@Override
		public void sendUserAction(Action action, Map properties)
				throws RemoteException {
            Log.v(TAG, "called: sendUserAction(Action action, Map properties)");
            DebugFileLog.write(TAG+ " called: sendUserAction(Action action, Map properties)");
			ServiceModel.getInstance().setServiceObject(MusesServiceProvider.this);
            eu.musesproject.client.model.decisiontable.Action userAction = UserActionGenerator.transformUserAction(action);
			UserContextMonitoringController.getInstance(MusesServiceProvider.this)
                    .sendUserAction(UISource.MUSES_AWARE_APP_UI, userAction, properties);
		}
		

		@Override
		public void registerCallback(IMusesServiceCallback callback)
				throws RemoteException {
			MusesServiceProvider.this.callback = callback;
		}

		@Override
		public IBinder asBinder() {
			return super.asBinder();
		}

		@Override
		public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
				throws RemoteException {
			try {
				super.onTransact(code, data, reply, flags);
			}catch(RuntimeException re){
		        Log.v(TAG, "Unexpected remote exception", re);
		        DebugFileLog.write(TAG+ " Unexpected remote exception: "+ re.getMessage());
		        throw re;
			}
			return false;
		}
	};
	
	

	/**
	 * Info AP
	 * 
	 * Method to send a response back to the MUSES aware app (caller)
	 * 
	 * @param response {@link ResponseInfoAP}
	 * @param message {@link String}
	 * @throws RemoteException
	 */
	public void sendResponseToMusesAwareApp(ResponseInfoAP response, String message) throws RemoteException {
		Log.d(TAG, "response: " + response + " received");
		DebugFileLog.write(TAG+ " response: " + response.toString() + " received with msg: "+message);
		switch (response) {
		case ACCEPT:
			callback.onAccept(message);
			break;
		case DENY:
			callback.onDeny(message);
			break;
		default: // accept
			callback.onAccept(message);
			break;
		}
	}
}
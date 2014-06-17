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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import eu.musesproject.client.model.actuators.ResponseInfoAP;
import eu.musesproject.server.risktrust.RiskTreatment;

public class DummyCommunication {
	private Context context;
	private MusesServiceProvider musesService;
	
	public DummyCommunication(Context context) {
		this.context = context;
		context.bindService(new Intent(MusesServiceProvider.class.getName()), mServiceConn, Context.BIND_AUTO_CREATE);
	}

	public void sendResponse(ResponseInfoAP infoAP, RiskTreatment riskTreatment) {

		// send to muses aware app
		try {
			musesService = ServiceModel.getInstance().getService();
			musesService.sendResponseToMusesAwareApp(infoAP, riskTreatment.getTextualDescription());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	// No need for this anymore 
	private ServiceConnection mServiceConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d("SERVICE CONNECTED", "SERVICE CONNECTED");
			musesService = ((MusesServiceProvider) service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d("SERVICE DISCONNECTED", "SERVICE DISCONNECTED");
			musesService = null;
		}
	};
}
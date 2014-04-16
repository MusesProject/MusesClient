package eu.musesproject.client.contextmonitoring.service.aidl;

import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.UserActionGenerator;
import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;
import eu.musesproject.client.model.actuators.ResponseInfoAP;
import eu.musesproject.client.model.decisiontable.ActionType;

public class MusesServiceProvider extends Service {
	private static final String TAG = MusesServiceProvider.class.getSimpleName();
	private IMusesServiceCallback callback;
	private MusesServiceProvider musesService;
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.e(TAG, "Came to Bind");
		return binder;
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
			Log.d(TAG, "callback unregistered");
		}	
		
		@Override
		public void sendUserAction(Action action, Map properties)
				throws RemoteException {
            Log.d(TAG, "called: sendUserAction(Action action, Map properties)");
			ServiceModel.getInstance().setServiceObject(MusesServiceProvider.this);
            eu.musesproject.client.model.decisiontable.Action userAction = UserActionGenerator.transformUserAction(action);
			UserContextMonitoringController.getInstance(MusesServiceProvider.this).sendUserAction(userAction, properties);
		}
		

		@Override
		public void registerCallback(IMusesServiceCallback callback)
				throws RemoteException {
			MusesServiceProvider.this.callback = callback;
		}

		@Override
		public IBinder asBinder() {
			// TODO Auto-generated method stub
			return super.asBinder();
		}

		@Override
		public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
				throws RemoteException {
			try {
				super.onTransact(code, data, reply, flags);
			}catch(RuntimeException re){
		        Log.d(TAG, "Unexpected remote exception", re);
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
		Log.d(TAG, "respsone: " + response + " received");
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
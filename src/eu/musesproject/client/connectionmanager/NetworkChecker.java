package eu.musesproject.client.connectionmanager;
/*
 * #%L
 * MUSES Client
 * %%
 * Copyright (C) 2013 - 2014 Sweden Connectivity
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.util.Log;

/**
 * This class is responsible to check device network connectivity
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */

public class NetworkChecker extends BroadcastReceiver{
	
	protected static final String TAG = "NetworkChecker";
	private static final boolean D = false;
	public static boolean isInternetConnected = false;
	private Context context;
	
	public NetworkChecker() {
	}
	
	/**
	 * Constructor 
	 * @param context
	 */
	
	public NetworkChecker(Context context) {
		this.context = context;
	}
	
	/**
	 * Chech is internet connected 
	 * @return isInternetConnected
	 */
	
	public boolean isInternetConnected(){
		ConnectivityManager connectivityManager = (ConnectivityManager)
			    context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi =
			    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    NetworkInfo mobile =
	    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    if (wifi != null){
		    if( wifi.isAvailable() && wifi.getDetailedState() == DetailedState.CONNECTED){
		    	isInternetConnected = true;
		    	return true;
		    }
	    }
	    else if (mobile != null) {
	    	if( mobile.isAvailable() && mobile.getDetailedState() == DetailedState.CONNECTED ){
	    		isInternetConnected = true;
	    		return true;
	    	}  
	    }
	    
	    isInternetConnected = false;
	    return false;
			    
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (D) Log.d("app","Network connectivity change");
		ConnectivityManager connectivityManager = (ConnectivityManager)
			    context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi =
			    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    NetworkInfo mobile =
	    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    if( wifi.isAvailable() && wifi.isConnectedOrConnecting()){
	    	isInternetConnected = true;
	    	Log.d(TAG, "Device has Internet connection (Wi-Fi)");
	    }  else if (mobile != null) {
	    	if( mobile.isAvailable() && mobile.isConnectedOrConnecting() ){
	    		isInternetConnected = true;
	    		Log.d(TAG, "Device has Internet connection (3G)");
	    	} 
	    } else {
	    	Log.d(TAG, "Device has No Internet connection....");
	    	isInternetConnected = false;
	    }

	}
	

}

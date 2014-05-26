/*
 * version 1.0 - MUSES prototype software
 * Copyright MUSES project (European Commission FP7) - 2013 
 * 
 */


package eu.musesproject.client.connectionmanager;

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
	    	if (D) Log.d(TAG, "Device has Internet connection (Wi-Fi)");
	    }  else if( mobile.isAvailable() && mobile.isConnectedOrConnecting() ){
	    	isInternetConnected = true;
	    	if (D) Log.d(TAG, "Device has Internet connection (3G)");
	    } else {
	    	if (D) Log.d(TAG, "Device has No Internet connection....");
	    	isInternetConnected = false;
	    }

	}
	

}

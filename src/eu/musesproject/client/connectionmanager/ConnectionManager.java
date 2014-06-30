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
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Connection Manager class implements the iConnection interface
 * 
 * @author Yasir Ali 
 * @version Jan 27, 2014
 */

public class ConnectionManager extends HttpConnectionsHelper implements IConnectionManager{

	public static String URL;
	public static IConnectionCallbacks callBacks;
	public static final boolean D = false;
	public static final boolean FAKE_MODE_ON = true;
	public static final String CONNECT = "connect";
	public static final String POLL = "poll";
	public static final String DATA = "data";
	public static final String DISCONNECT = "disconnect";
	public static final String ACK = "ack";
	private static final String TAG = ConnectionManager.class.getSimpleName();
	
	private AlarmReceiver alarmReceiver;
	private Context context;
	
	/**
	 * Connect to the server with specified parameter
	 * @param url
	 * @param pollInterval
	 * @param sleepPollInterval
	 * @param callbacks
	 * @param context
	 * @return void
	 */
	
	@Override
	public void connect(String url, int pollInterval, int sleepPollInterval, IConnectionCallbacks callbacks, Context context) {
		URL = url;
		AlarmReceiver.POLL_INTERVAL = pollInterval;
		AlarmReceiver.SLEEP_POLL_INTERVAL = sleepPollInterval;
		AlarmReceiver.DEFAULT_POLL_INTERVAL = pollInterval;
		AlarmReceiver.DEFAULT_SLEEP_POLL_INTERVAL = sleepPollInterval;
		AlarmReceiver.LAST_SENT_POLL_INTERVAL = pollInterval;
		callBacks = callbacks;
		this.context = context;
		NetworkChecker networkChecker = new NetworkChecker(context);
		PhoneModeReceiver phoneModeReceiver = new PhoneModeReceiver(context);
		phoneModeReceiver.register();
		if (D) Log.v(TAG, "Connecting ..");
		if (networkChecker.isInternetConnected()) {
			HttpClientAsyncThread httpClientAsyncThread = new HttpClientAsyncThread();
			httpClientAsyncThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, CONNECT, 
					URL, Integer.toString(AlarmReceiver.DEFAULT_POLL_INTERVAL),"");
		} else callBacks.statusCb(Statuses.CONNECTION_FAILED, DetailedStatuses.NO_INTERNET_CONNECTION);
		
		alarmReceiver = new AlarmReceiver();
		alarmReceiver.setAlarm(context);
		
	}

	/**
	 * Send data to the server 
	 * @param data
	 * @return void 
	 */
	
	@Override
	public void sendData(String data) {
		
		if (NetworkChecker.isInternetConnected) {
			HttpClientAsyncThread httpClientAsyncThread = new HttpClientAsyncThread();
			httpClientAsyncThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, DATA, URL, 
					Integer.toString(AlarmReceiver.DEFAULT_POLL_INTERVAL), data);
		} else callBacks.statusCb(Statuses.DATA_SEND_FAILED, DetailedStatuses.NO_INTERNET_CONNECTION);
	}
	
	/**
	 * Disconnects session from the server 
	 * @return void 
	 */
	
	@Override
	public void disconnect() { // FIXME What if the server is not online, How should we stop polling from here
		// As we are disconnecting we need to stop the polling 
		if (D) Log.v(TAG, "Disconnecting ..");
		if (NetworkChecker.isInternetConnected) {
			HttpClientAsyncThread httpClientAsyncThread = new HttpClientAsyncThread();
			httpClientAsyncThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, DISCONNECT, URL, 
					Integer.toString(AlarmReceiver.DEFAULT_POLL_INTERVAL), "");
			HttpConnectionsHelper.cookie = null;
			alarmReceiver.cancelAlarm(context);
			callBacks.statusCb(Statuses.DISCONNECTED, Statuses.DISCONNECTED);
		} else callBacks.statusCb(Statuses.DISCONNECTED, Statuses.DISCONNECTED);

	
	}

	/**
	 * Sets poll timeouts
	 * @param pollInterval
	 * @param sleepPollInterval
	 * @return void 
	 */
	
	
	@Override
	public void setPollTimeOuts(int pollInterval, int sleepPollInterval) {
		AlarmReceiver.POLL_INTERVAL = pollInterval;
		AlarmReceiver.SLEEP_POLL_INTERVAL = sleepPollInterval;
	}

	/**
	 * Starts to poll with the server either in sleep/active mode
	 * @return void
	 */
	
	public void poll() {
		if (D) Log.v(TAG, "Polling !!");
		if (NetworkChecker.isInternetConnected) {
			if (PhoneModeReceiver.SLEEP_MODE_ACTIVE) { // FIXME 
				HttpClientAsyncThread httpClientAsyncThread = new HttpClientAsyncThread();
				httpClientAsyncThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, POLL, URL, 
						Integer.toString(AlarmReceiver.DEFAULT_SLEEP_POLL_INTERVAL), "");
			} else {
				HttpClientAsyncThread httpClientAsyncThread = new HttpClientAsyncThread();
				httpClientAsyncThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, POLL, URL, 
						Integer.toString(AlarmReceiver.DEFAULT_POLL_INTERVAL), "");
			}
		}
	}

	/**
	 * Sends ack to the server to tell that data has been received on the client
	 * @return void
	 */
	
	public void ack() {
		if (D) Log.v(TAG, "Sending ack..");
		if (NetworkChecker.isInternetConnected) {
			if (PhoneModeReceiver.SLEEP_MODE_ACTIVE) { // FIXME 
				HttpClientAsyncThread httpClientAsyncThread = new HttpClientAsyncThread();
				httpClientAsyncThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ACK, URL, 
						Integer.toString(AlarmReceiver.DEFAULT_SLEEP_POLL_INTERVAL), "");
			} else {
				HttpClientAsyncThread httpClientAsyncThread = new HttpClientAsyncThread();
				httpClientAsyncThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ACK, URL, 
						Integer.toString(AlarmReceiver.DEFAULT_POLL_INTERVAL), "");
			}
		}
	}
	
	/**
	 * Handler Http/Https (Networking in background or separate thread)
	 * @author Yasir Ali
	 * @version Jan 27, 2014
	 */
	
    private class HttpClientAsyncThread extends AsyncTask<String, Void, String> {

    	@Override
    	protected String doInBackground(String... params) {
    		HttpResponse response = null;
			Request request = new Request(params[0], 
					params[1], params[2], params[3]);
			
			try {
				response = doSecurePost(request);
//				response = doPost(params[0], 
//					params[1], params[3]);
				HttpResponseHandler httpResponseHandler = new HttpResponseHandler(response, request.getType());
				httpResponseHandler.checkHttpResponse();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;

    	}

    }
	
}
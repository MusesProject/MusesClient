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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

import eu.musesproject.client.ui.DebugFileLog;
import eu.musesproject.client.usercontexteventhandler.JSONManager;
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

	private static String URL = "";
	private static String certificate;
	public static IConnectionCallbacks callBacks;
	public static final String CONNECT = "connect";
	public static final String POLL = "poll";
	public static final String DATA = "data";
	public static final String DISCONNECT = "disconnect";
	public static final String ACK = "ack";
	private static final String TAG = ConnectionManager.class.getSimpleName();
	private static final String APP_TAG = "APP_TAG";
	private static Boolean isServerConnectionSet = false; 
	static private int lastSentStatus = Statuses.OFFLINE;
	private AtomicInteger mCommandOngoing = new AtomicInteger(0);
	private AlarmReceiver alarmReceiver;
	public static Context context;
	public static boolean isNewSession = true;
		
	public ConnectionManager(){
		alarmReceiver = new AlarmReceiver();
		AlarmReceiver.setManager(this);
	}
	
	/**
	 * Connect to the server with specified parameter
	 * @param url
	 * @param pollInterval
	 * @param sleepPollInterval
	 * @param callbacks
	 * @param cntext
	 * @return void
	 */
	
	@Override
	public void connect(String url, String cert, int pollInterval, int sleepPollInterval, IConnectionCallbacks callbacks, Context cntext) {
		/* FIXME, temporary fix for dual calls */
		synchronized (this){
			if (isServerConnectionSet){
				Log.d(TAG, "connect: More then one more connect call!");
				return;
			}
			isServerConnectionSet = true;
		}
		
		URL = url;
		callBacks = callbacks;
		context = cntext;
		certificate = cert;
		
		/* Check that cert is ok, spec length */
		/* FIXME which Length.. */
		if (cert.isEmpty() || cert.length() < 1000){
			callBacks.statusCb(Statuses.CONNECTION_FAILED, DetailedStatuses.INCORRECT_CERTIFICATE, 0);
			Log.d(TAG, "connect: Incorrect certificate!");
			DebugFileLog.write(TAG+ " connect: Incorrect certificate!");
			return;
		}
			
		NetworkChecker networkChecker = new NetworkChecker(cntext);
		PhoneModeReceiver phoneModeReceiver = new PhoneModeReceiver(cntext);
		phoneModeReceiver.register();
		Log.d(TAG, "Connecting to URL:"+url);
		DebugFileLog.write(TAG+ " Connecting to URL:"+url);
		
		if (networkChecker.isInternetConnected()) {
			Log.d(TAG, "InternetConnected");
		}
				
        setCommandOngoing();
        
        startHttpThread( CONNECT,
				URL, Integer.toString(pollInterval),"", "");
				
		alarmReceiver.setPollInterval(pollInterval, sleepPollInterval);
		alarmReceiver.setDefaultPollInterval(pollInterval, sleepPollInterval);
		alarmReceiver.setAlarm(cntext);
		
	}

	/**
	 * Send data to the server 
	 * @param data
	 * @return void 
	 */
	
	@Override
	public void sendData(String data, int dataId) {
		String dataIdStr = "";
		dataIdStr = Integer.toString(dataId);
		setCommandOngoing();
		Log.d(APP_TAG, "ConnManager=> send data to server with request type: "+JSONManager.getRequestType(data));
		DebugFileLog.write(APP_TAG+ " ConnManager=> send data to server with request type: "+JSONManager.getRequestType(data));
		startHttpThread(DATA, URL, 
				Integer.toString(AlarmReceiver.getCurrentPollInterval()), data, dataIdStr); 
	}
	
	/**
	 * Disconnects session from the server 
	 * @return void 
	 */
	
	@Override
	public void disconnect() { // FIXME What if the server is not online, How should we stop polling from here
		// As we are disconnecting we need to stop the polling 
		Log.d(TAG, "Disconnecting ..");
		synchronized (this){
			isServerConnectionSet = false;
		}
		
		Log.d(APP_TAG, "ConnManager=> disconnecting session to server");
		DebugFileLog.write(APP_TAG+ " ConnManager=> disconnecting session to server");
		startHttpThread(DISCONNECT, URL, 
				Integer.toString(AlarmReceiver.getCurrentPollInterval()), "", "");
			
			//callBacks.statusCb(Statuses.DISCONNECTED, Statuses.DISCONNECTED);  // FIXME
		alarmReceiver.cancelAlarm(context);
	}

	/**
	 * Sets poll timeouts
	 * @param pollInterval
	 * @param sleepPollInterval
	 * @return void 
	 */
	
	
	@Override
	public void setPollTimeOuts(int pollInterval, int sleepPollInterval) {
		alarmReceiver.setPollInterval(pollInterval, sleepPollInterval);
		alarmReceiver.setDefaultPollInterval(pollInterval, sleepPollInterval);
	}

	/**
	 * Set timeout
	 * @param timeout
	 * @return void
	 */

	@Override
	public void setTimeout(int timeout) {
		CONNECTION_TIMEOUT = timeout;
	}

	/**
	 * Enable polling
	 * @param polling
	 * @return void 
	 */
	
	@Override
	public void setPolling(int polling) {
		POLLING_ENABLED = polling;
	}


	/**
	 * Starts to poll with the server either in sleep/active mode
	 * @return void
	 */
	
	public void periodicPoll() {
		//Log.d(APP_TAG, "Polling !!");
		
		// If ongoing command, don't poll
		if (mCommandOngoing.get()==0){
			poll();
		}
		
	}
	
	/**
	 * Starts to poll with the server either in sleep/active mode
	 * @return void
	 */
	
	public void poll() {
		//Log.d(APP_TAG, "Polling !!");
		setCommandOngoing();
		startHttpThread(POLL, URL, 
				Integer.toString(AlarmReceiver.getCurrentPollInterval()), "", "");
			
	}

	/**
	 * Sends ack to the server to tell that data has been received on the client
	 * @return void
	 */
	
	public void ack() {
		Log.d(TAG, "Sending ack..");
		DebugFileLog.write(TAG+ "Sending ack..");
		startHttpThread(ACK, URL, 
				Integer.toString(AlarmReceiver.getCurrentPollInterval()), "", "");

	}
	
	private void startHttpThread(String cmd, String url, String pollInterval, String data, String dataId) {
		HttpClientAsyncThread httpClientAsyncThread = new HttpClientAsyncThread();
		httpClientAsyncThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cmd, url, 
				pollInterval, data, certificate, dataId);

		/* If too many threads, use serial executor */
//		httpClientAsyncThread.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, cmd, url, 
//				pollInterval, data, certificate);

	}

	/**
	 * Handler Http/Https (Networking in background or separate thread)
	 * @author Yasir Ali
	 * @version Jan 27, 2014
	 */
	
    private class HttpClientAsyncThread extends AsyncTask<String, Void, String> {

    	/* FIXME use onPost Execute to send callbacks */
    	@Override
    	protected String doInBackground(String... params) {
    		HttpResponse response = null;
			Request request = new Request(params[0], 
					params[1], params[2], params[3], params[4], params[5]);
			if (!NetworkChecker.isInternetConnected) {
				if (request.getType().contentEquals(CONNECT)){
					callBacks.statusCb(Statuses.CONNECTION_FAILED, DetailedStatuses.NO_INTERNET_CONNECTION, request.getDataId());
				}
				else if (request.getType().contentEquals(DATA)){
					Log.d(APP_TAG, "ConnManager=> can't send data with no internet connection, calling statusCB");
					sendServerStatus(Statuses.OFFLINE, DetailedStatuses.NO_INTERNET_CONNECTION, request.getDataId());
					callBacks.statusCb(Statuses.DATA_SEND_FAILED, DetailedStatuses.NO_INTERNET_CONNECTION, request.getDataId());
					
				} else if (request.getType().contentEquals(DISCONNECT)){
					callBacks.statusCb(Statuses.DISCONNECTED, DetailedStatuses.NO_INTERNET_CONNECTION, request.getDataId());
					//HttpConnectionsHelper.current_cookie = null;
				}
			}
			else
			{
				Log.d(APP_TAG,"doInBackground: starts with parameters: "+params[0]+", "+params[1]+", "+params[2]+", "+params[3]);
				DebugFileLog.write(APP_TAG+" doInBackground: starts with parameters: "+params[0]+", "+params[1]+", "+params[2]+", "+params[3]);
				try {
					HttpResponseHandler httpResponseHandler = doSecurePost(request, params[4]);
					if (httpResponseHandler != null){
						httpResponseHandler.checkHttpResponse();
					}
					
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					Log.d(APP_TAG, Log.getStackTraceString(e));
				} catch (IOException e) {
					e.printStackTrace();
					Log.d(APP_TAG, Log.getStackTraceString(e));
				} catch (Exception e) {
					e.printStackTrace();
					Log.d(APP_TAG, Log.getStackTraceString(e));
				}
				
				if (request.getType().contentEquals(DISCONNECT)){
					//HttpConnectionsHelper.current_cookie = null;
				}
			}
			
			setCommandNotOngoing();
			Log.d(APP_TAG,"doInBackground: finished.");
			DebugFileLog.write(APP_TAG +" doInBackground: finished.");
			return null;

    	}

    }
	
	private void setCommandOngoing(){
		mCommandOngoing.set(1);	
	}
	
	private void setCommandNotOngoing(){	
		mCommandOngoing.set(0);	
	}
	
	public static void sendServerStatus(int status, int detailedStatus, int dataId) {
		if (status == Statuses.OFFLINE || status == Statuses.ONLINE) {
			if (lastSentStatus != status ){
				ConnectionManager.callBacks.statusCb(status, detailedStatus, dataId);
				lastSentStatus = status;
			}
		}
		else {
			//DBG SweFileLog.write("Weird status: "+Integer.toString(status)+", , ");
		}


	}
	
}
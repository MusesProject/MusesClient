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

import android.util.Log;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;


/**
 * Handles http response and updates the other module using callbacks
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */
public class HttpResponseHandler {

	private static final String TAG = HttpResponseHandler.class.getSimpleName();
	private static final String APP_TAG = "APP_TAG";
	private static final int MINIMUM_POLL_AFTER_REQUEST = 10000;
	private String receivedHttpResponseData = null;
	private HttpResponse httpResponse = null;
	private boolean isNewSession = false;
	private String requestType;
	private int dataId;
	private int sessionUpdateReason = 0;
	/**
	 * Constructor initialise with httpResponse and request type (connect,data,poll etc)
	 * @param httpResponse
	 * @param requestType
	 * @return void
	 */
	public HttpResponseHandler(HttpResponse httpResponse, String requestType, int dataId) {
		this.httpResponse = httpResponse;
		this.requestType = requestType;
		this.dataId = dataId;
	}

	public HttpResponseHandler(String requestType, int dataId) {
		// TODO Auto-generated constructor stub
		this.requestType = requestType;
		this.dataId = dataId;
	}

	/**
	 * Check response and call appropriate callback methods
	 * @return void
	 */
	public synchronized void checkHttpResponse(){
		if (httpResponse != null) {
			switch(getStatusCodeResponse(httpResponse)){
			case DetailedStatuses.SUCCESS:
				// Only send if Success in connection
				int detailedOnlineStatus = DetailedStatuses.SUCCESS;
				if (isNewSession )
				{
					isNewSession = false;
					if (Statuses.CURRENT_STATUS == Statuses.ONLINE)
					{
						// For testing
						//DBG SweFileLog.write("New sessionId, ,");
						setServerStatusAndCallBack(Statuses.NEW_SESSION_CREATED, sessionUpdateReason, dataId);
					}
				}

				Statuses.CURRENT_STATUS = Statuses.ONLINE;
				
				if (isPollRequest(requestType)) {
					setServerStatusAndCallBack(Statuses.ONLINE, detailedOnlineStatus, dataId);
					if (isPayloadInData(httpResponse)) {
						Log.d(APP_TAG, "ConnManager=> Server responded with JSON: " + receivedHttpResponseData);
						sendDataToFunctionalLayer();
						sendAcktoServer();
					}
					if (isMorePackets(httpResponse)){
						doPollForAnExtraPacket();
					}
				} else if (isSendDataRequest(requestType)){
					setServerStatusAndCallBack(Statuses.ONLINE, detailedOnlineStatus, dataId);
					setServerStatusAndCallBack(Statuses.DATA_SEND_OK, DetailedStatuses.SUCCESS, dataId);
					if (isPayloadInData(httpResponse)) {
						Log.d(APP_TAG, "ConnManager=> Server responded with JSON: " + receivedHttpResponseData);
						sendDataToFunctionalLayer();
					}

					if (isMorePackets(httpResponse) || AlarmReceiver.getCurrentPollInterval()>MINIMUM_POLL_AFTER_REQUEST){
						doPollForAnExtraPacket();
					}
				} else if (isAckRequest(requestType)) {
					setServerStatusAndCallBack(Statuses.ONLINE, detailedOnlineStatus, dataId);
					Log.d(APP_TAG, "ConnManager=> Server responded with JSON: " + receivedHttpResponseData);
					Log.d(TAG, "Ack by the server");
				} else if (isConnectRequest(requestType)){
					setServerStatusAndCallBack(Statuses.ONLINE, detailedOnlineStatus, dataId);
					setServerStatusAndCallBack(Statuses.CONNECTION_OK, DetailedStatuses.SUCCESS, dataId);
					if (isPayloadInData(httpResponse)) {
						Log.d(APP_TAG, "ConnManager=> Server responded with JSON: " + receivedHttpResponseData);

						//sendDataToFunctionalLayer();
					} 
				} else if (isDisonnectRequest(requestType)){
					setServerStatusAndCallBack(Statuses.DISCONNECTED, DetailedStatuses.SUCCESS, dataId);
					if (isPayloadInData(httpResponse)) {
						Log.d(APP_TAG, "ConnManager=> Server responded with JSON: " + receivedHttpResponseData);

						//sendDataToFunctionalLayer();
					} 
				}

				AlarmReceiver.resetExponentialPollTime();
				break;
			case DetailedStatuses.INCORRECT_URL:
				Statuses.CURRENT_STATUS = Statuses.OFFLINE;
				Log.d(APP_TAG, "Server is OFFLINE .. Incorrect URL");

				if (isSendDataRequest(requestType)){
					setServerStatusAndCallBack(Statuses.DATA_SEND_FAILED, DetailedStatuses.INCORRECT_URL, dataId);
				}
				
				setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.INCORRECT_URL, dataId);
				
				AlarmReceiver.increasePollTime();
				break;
			case DetailedStatuses.NOT_ALLOWED_FROM_SERVER_UNAUTHORIZED:
				Statuses.CURRENT_STATUS = Statuses.OFFLINE;
				Log.d(APP_TAG, "Server is OFFLINE .. Request not allowed from Server..");
				
				if (isSendDataRequest(requestType)){
					setServerStatusAndCallBack(Statuses.DATA_SEND_FAILED, DetailedStatuses.NOT_ALLOWED_FROM_SERVER_UNAUTHORIZED, dataId);
				}
				
				setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.NOT_ALLOWED_FROM_SERVER_UNAUTHORIZED, dataId);
				AlarmReceiver.increasePollTime();
				break;
			case DetailedStatuses.NOT_FOUND:
				Statuses.CURRENT_STATUS = Statuses.OFFLINE;
				Log.d(APP_TAG, "Server is OFFLINE .. Error: NOt found");
				
				if (isSendDataRequest(requestType)){
					setServerStatusAndCallBack(Statuses.DATA_SEND_FAILED, DetailedStatuses.NOT_FOUND, dataId);
				}
				
				setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.NOT_FOUND, dataId);
				AlarmReceiver.increasePollTime();
				break;
			case DetailedStatuses.INTERNAL_SERVER_ERROR:
				Statuses.CURRENT_STATUS = Statuses.OFFLINE;
				Log.d(APP_TAG, "Server is OFFLINE .. Internal Server Error");
				
				if (isSendDataRequest(requestType)){
					setServerStatusAndCallBack(Statuses.DATA_SEND_FAILED, DetailedStatuses.INTERNAL_SERVER_ERROR, dataId);
				}
				
				setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.INTERNAL_SERVER_ERROR, dataId);
				AlarmReceiver.increasePollTime();
				break;
			case DetailedStatuses.SERVER_NOT_AVAIABLE:
				Statuses.CURRENT_STATUS = Statuses.OFFLINE;
				Log.d(APP_TAG, "Server is OFFLINE .. Server not available..");
				if (isSendDataRequest(requestType)){
					setServerStatusAndCallBack(Statuses.DATA_SEND_FAILED, DetailedStatuses.SERVER_NOT_AVAIABLE, dataId);
				}
				
				setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.SERVER_NOT_AVAIABLE, dataId);
				AlarmReceiver.increasePollTime();
				break;
			default:
				Statuses.CURRENT_STATUS = Statuses.OFFLINE;
				Log.d(APP_TAG, "Server is OFFLINE .. Unknown Error:"+getStatusCodeResponse(httpResponse));
				if (isSendDataRequest(requestType)){
					setServerStatusAndCallBack(Statuses.DATA_SEND_FAILED, DetailedStatuses.UNKNOWN_ERROR, dataId);
				}
				setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.UNKNOWN_ERROR, dataId);
				AlarmReceiver.increasePollTime();
				break;
			}


		} else {
			Statuses.CURRENT_STATUS = Statuses.OFFLINE;
			setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.UNKNOWN_ERROR, dataId);
			if (isSendDataRequest(requestType)){
				setServerStatusAndCallBack(Statuses.DATA_SEND_FAILED, DetailedStatuses.UNKNOWN_ERROR, dataId);
			} 
			if (isConnectRequest(requestType)){
				setServerStatusAndCallBack(Statuses.CONNECTION_FAILED, DetailedStatuses.UNKNOWN_ERROR, dataId);
				/* Depending on error Polling shall be stopped, but difficult to know if error is recoverable */
			}
			Log.d(APP_TAG, "Server is OFFLINE, HttpResponse is null, check network connectivity or address of server!");
		}
	}
	
	private boolean isMorePackets(HttpResponse httpResponse) {
		Header [] dataReceived = httpResponse.getAllHeaders();
		for (Header responseHedar : dataReceived){
			if (responseHedar.getName().equals("more-packets")){
				if (responseHedar.getValue().equalsIgnoreCase("YES")){
					return true;
				}
			}
			
		}
		return false;
	}

	private void doPollForAnExtraPacket(){
		if (NetworkChecker.isInternetConnected){
			ConnectionManager connectionManager = new ConnectionManager();
			connectionManager.poll();
		}
	}
	
	/**
	 * Send data to functional module 
	 * @return void
	 */
	
	private void sendDataToFunctionalLayer(){
		Log.d(APP_TAG, "Info SS, Sending JSON to MusACS");
		ConnectionManager.callBacks.receiveCb(receivedHttpResponseData);
	}
	
	/**
	 * Calls the connection manager ack method to send the ack to the server
	 */
	private void sendAcktoServer() {
		ConnectionManager connectionManager = new ConnectionManager();
		connectionManager.ack();
	}

	/**
	 * Check if the http request is for polling
	 * @param requestType
	 * @return
	 */
	private boolean isPollRequest(String requestType) {
		if (requestType.equalsIgnoreCase(ConnectionManager.POLL)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check if the http request is for data
	 * @param requestType
	 * @return
	 */
	
	private boolean isSendDataRequest(String requestType) {
		if (requestType.equalsIgnoreCase(ConnectionManager.DATA)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check if the http request is for acknowledgment for the last data sent
	 * @param requestType
	 * @return
	 */
	
	private boolean isAckRequest(String requestType) {
		if (requestType.equalsIgnoreCase(ConnectionManager.ACK)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check if the http request is was a connect request
	 * @param requestType
	 * @return
	 */
	private boolean isConnectRequest(String requestType) {
		if (requestType.equalsIgnoreCase(ConnectionManager.CONNECT)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check if the http request is was a disconnect request
	 * @param requestType
	 * @return
	 */
	private boolean isDisonnectRequest(String requestType) {
		if (requestType.equalsIgnoreCase(ConnectionManager.DISCONNECT)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check if the http response has any payload of data
	 * @param HttpResponse
	 * @return
	 */
	
	private boolean isPayloadInData(HttpResponse httpResponse){
		String data = reteiveDataFromHttpResponseHeader(httpResponse);
		if ( data == null || data.equalsIgnoreCase("")){
			return false;
		}
		else return true;
	}

	/**
	 * Sets curent status of the Server in terms of ONLINE/OFFLINE using callbacks
	 * @param status
	 * @return void
	 */
	private void setServerStatusAndCallBack(int status, int detailedStatus, int dataId) {
		
		if (status == Statuses.OFFLINE || status == Statuses.ONLINE)
		{
			ConnectionManager.sendServerStatus(status, detailedStatus, dataId);
			
			
		}
		else
		{	
			ConnectionManager.callBacks.statusCb(status, detailedStatus, dataId);
		}
		
	}
	
	
	/**
	 * Get response code from status code
	 * @param httpResponse
	 * @return void
	 */
	
	private int getStatusCodeResponse(HttpResponse httpResponse){
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		switch(statusCode){
		case 200 :
			return DetailedStatuses.SUCCESS;
		case 400 :
			return DetailedStatuses.INCORRECT_URL;
		case 401 :
			return DetailedStatuses.NOT_ALLOWED_FROM_SERVER_UNAUTHORIZED;
		case 404 :
			return DetailedStatuses.NOT_FOUND;
		case 500 :
			return DetailedStatuses.INTERNAL_SERVER_ERROR;
		case 503 :
			return DetailedStatuses.SERVER_NOT_AVAIABLE;
		default :
			return DetailedStatuses.UNKNOWN_ERROR;
		}
	}
	
	/**
	 * Retrieve Data from http response from the Server
	 * @param response
	 * @return receivedHttpResponseData
	 * @throws IOException 
	 * @throws IllegalStateException 
	 * @throws UnsupportedEncodingException 
	 */
	
	private String reteiveDataFromHttpResponseHeader(HttpResponse response) {
		
		BufferedReader reader;
		String json = "";
		try {
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			json = reader.readLine();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		receivedHttpResponseData = json;
		return receivedHttpResponseData;
	}

	public void setNewSession(boolean isNewSession, int reason) {
		this.isNewSession = isNewSession;
		sessionUpdateReason = reason;
	}

	public void setResponse(HttpResponse httpResponse) {
		// TODO Auto-generated method stub
		this.httpResponse = httpResponse;
	}

	public String getDataLength() {
		String dataLength = "0";
		if (httpResponse != null)
		{
			Header header = httpResponse.getFirstHeader("data");
			if (header!=null)
			{
				if (header.getValue().contains("sensor-config"))
					Log.d(APP_TAG, "Sensor-Config");
				dataLength = Integer.toString(header.getValue().length());
			}
		}
		// new implementation as data header is not used anymore
		dataLength = Integer.toString(receivedHttpResponseData.length() );
		return dataLength;
	}

	
}


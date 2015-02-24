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
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import android.util.Log;


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
	private HttpResponse httpResponse;
	private boolean isNewSession = false;
	private int sessionUpdateReason = 0;
	private String requestType;

	/**
	 * Constructor initialise with httpResponse and request type (connect,data,poll etc)
	 * @param httpResponse
	 * @param requestType
	 * @return void
	 */
	public HttpResponseHandler(HttpResponse httpResponse, String requestType) {
		this.httpResponse = httpResponse;
		this.requestType = requestType;
	}
	
	/**
	 * Check response and call appropriate callback methods
	 * @return void
	 */
	public void checkHttpResponse(){
		if (httpResponse != null) {
			switch(getStatusCodeResponse(httpResponse)){
			case DetailedStatuses.SUCCESS:
				Statuses.CURRENT_STATUS = Statuses.ONLINE;
				
				if (isPollRequest(requestType)) {
					setServerStatusAndCallBack(Statuses.ONLINE, DetailedStatuses.SUCCESS);
					if (isPayloadInData(httpResponse)) {
						Log.d(APP_TAG, "ConnManager=> Server responded with JSON: " + receivedHttpResponseData);
						sendDataToFunctionalLayer();
						sendAcktoServer();
					}
					if (isMorePackets(httpResponse)){
						doPollForAnExtraPacket();
					}
				} else if (isSendDataRequest(requestType)){
					setServerStatusAndCallBack(Statuses.DATA_SEND_OK, DetailedStatuses.SUCCESS);
					if (isPayloadInData(httpResponse)) {
						Log.d(APP_TAG, "ConnManager=> Server responded with JSON: " + receivedHttpResponseData);
						sendDataToFunctionalLayer();
					}

					if (isMorePackets(httpResponse) || AlarmReceiver.getCurrentPollInterval()>MINIMUM_POLL_AFTER_REQUEST){
						doPollForAnExtraPacket();
					}
				} else if (isAckRequest(requestType)) {
					setServerStatusAndCallBack(Statuses.ONLINE, DetailedStatuses.SUCCESS);
					Log.d(APP_TAG, "ConnManager=> Server responded with JSON: " + receivedHttpResponseData);
					Log.d(TAG, "Ack by the server");
				} else if (isConnectRequest(requestType)){
					setServerStatusAndCallBack(Statuses.ONLINE, DetailedStatuses.SUCCESS);
					setServerStatusAndCallBack(Statuses.CONNECTION_OK, DetailedStatuses.SUCCESS);
					if (isPayloadInData(httpResponse)) {
						Log.d(APP_TAG, "ConnManager=> Server responded with JSON: " + receivedHttpResponseData);
						
						//sendDataToFunctionalLayer();
					} 
				} else if (isDisonnectRequest(requestType)){
					setServerStatusAndCallBack(Statuses.DISCONNECTED, DetailedStatuses.SUCCESS);
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
					setServerStatusAndCallBack(Statuses.DATA_SEND_FAILED, DetailedStatuses.INCORRECT_URL);
				}
				
				setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.INCORRECT_URL);
				
				AlarmReceiver.increasePollTime();
				break;
			case DetailedStatuses.NOT_ALLOWED_FROM_SERVER:
				Statuses.CURRENT_STATUS = Statuses.OFFLINE;
				Log.d(APP_TAG, "Server is OFFLINE .. Request not allowed from Server..");
				
				if (isSendDataRequest(requestType)){
					setServerStatusAndCallBack(Statuses.DATA_SEND_FAILED, DetailedStatuses.NOT_ALLOWED_FROM_SERVER);
				}
				
				setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.NOT_ALLOWED_FROM_SERVER);
				AlarmReceiver.increasePollTime();
				break;
			case DetailedStatuses.SERVER_NOT_AVAIABLE:
				Statuses.CURRENT_STATUS = Statuses.OFFLINE;
				Log.d(APP_TAG, "Server is OFFLINE .. Server not available..");
				
				if (isSendDataRequest(requestType)){
					setServerStatusAndCallBack(Statuses.DATA_SEND_FAILED, DetailedStatuses.SERVER_NOT_AVAIABLE);
				}
				
				setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.SERVER_NOT_AVAIABLE);
				AlarmReceiver.increasePollTime();
				break;
			default:
				Statuses.CURRENT_STATUS = Statuses.OFFLINE;
				Log.d(APP_TAG, "Server is OFFLINE .. Unknown Error:"+getStatusCodeResponse(httpResponse));
				if (isSendDataRequest(requestType)){
					setServerStatusAndCallBack(Statuses.DATA_SEND_FAILED, DetailedStatuses.UNKNOWN_ERROR);
				}
				setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.UNKNOWN_ERROR);
				AlarmReceiver.increasePollTime();
				break;
			}

			if (isNewSession)
			{
				setServerStatusAndCallBack(Statuses.NEW_SESSION_CREATED, sessionUpdateReason);
			}

		} else {
			Statuses.CURRENT_STATUS = Statuses.OFFLINE;
			setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.UNKNOWN_ERROR);
			if (isSendDataRequest(requestType)){
				setServerStatusAndCallBack(Statuses.DATA_SEND_FAILED, DetailedStatuses.UNKNOWN_ERROR);
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
	private void setServerStatusAndCallBack(int status, int detailedStatus) {
		ConnectionManager.callBacks.statusCb(status, detailedStatus);
		
		
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
			return DetailedStatuses.NOT_ALLOWED_FROM_SERVER;
		case 404 :
			return DetailedStatuses.SERVER_NOT_AVAIABLE;
		default :
			return DetailedStatuses.UNKNOWN_ERROR;
		}
	}
	
	/**
	 * Retrieve Data from http response from the Server
	 * @param response
	 * @return receivedHttpResponseData
	 */
	
	private String reteiveDataFromHttpResponseHeader(HttpResponse response) {
		Header [] dataReceived = response.getAllHeaders();
		for (Header responseHedar : dataReceived){
			if (responseHedar.getName().equals("data")){
				receivedHttpResponseData = responseHedar.getValue();
				break;
			}
			
		}
		return receivedHttpResponseData;
	}

	public void setNewSession(int reason) {
		// TODO Auto-generated method stub
		isNewSession = true;
		sessionUpdateReason = reason;
	}

	
}


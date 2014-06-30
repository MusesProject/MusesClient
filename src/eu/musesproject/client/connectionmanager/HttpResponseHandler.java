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
	
	private static final boolean D = false;
	private static final String TAG = HttpResponseHandler.class.getSimpleName();
	private String receivedHttpResponseData = null;
	private HttpResponse httpResponse;
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
				setServerStatusAndCallBack(Statuses.ONLINE, DetailedStatuses.SUCCESS);
				if (isPollRequest(requestType)) {
					if (isPayloadInData(httpResponse)) {
						sendDataToFunctionalLayer();
						sendAcktoServer();
					}
					if (isMorePackets(httpResponse)){
						doPollForAnExtraPacket();
					}
				}
				if (isSendDataRequest(requestType)){
					if (isPayloadInData(httpResponse)) {
						sendDataToFunctionalLayer();
					} 
				}
				if (isAckRequest(requestType)) {
					if (D) Log.e(TAG, "Ack by the server"); // FIXME this can do more stuff
				}
				AlarmReceiver.resetExponentialPollTime();
				break;
			case DetailedStatuses.INCORRECT_URL:
				setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.INCORRECT_URL);
				AlarmReceiver.increasePollTime();
				break;
			case DetailedStatuses.NOT_ALLOWED_FROM_SERVER:
				setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.NOT_ALLOWED_FROM_SERVER);
				AlarmReceiver.increasePollTime();
				break;
			case DetailedStatuses.SERVER_NOT_AVAIABLE:
				setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.SERVER_NOT_AVAIABLE);
				AlarmReceiver.increasePollTime();
				break;
			default:
				setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.UNKNOWN_ERROR);
				AlarmReceiver.increasePollTime();
				break;
			}
		} else {
			if (D) Log.v(TAG, "Exception occured during communication with the server .. check the stacktrace to fix it!");
			setServerStatusAndCallBack(Statuses.OFFLINE, DetailedStatuses.UNKNOWN_ERROR);
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
	 * Check if the http response has any payload of data
	 * @param requestType
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
		if (status != Statuses.CURRENT_STATUS){ 
			// FIXME below if condition can be merged in this condition
			Statuses.CURRENT_STATUS = status;
			Statuses.STATUS_CHANGED = true;
			if (D) Log.v(TAG, "Server ONLINE");
		}
		
		if (Statuses.STATUS_CHANGED){
			//CommunicationManager.callBacks.receiveCb(receivedHttpResponseData); // FIXME  (this callback should be only triggered when their is data in the response)
			ConnectionManager.callBacks.statusCb(status, detailedStatus);
			Statuses.STATUS_CHANGED = false;
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
				Log.v(TAG, "Server responded with JSON " + receivedHttpResponseData);
				break;
			}
			
		}
		return receivedHttpResponseData;
	}

	
}


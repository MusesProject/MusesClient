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
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;

import android.util.Log;
import eu.musesproject.client.db.handler.DBManager;

/**
 * Helper class for connection Manager handles POST and GET request with the server
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */


public abstract class HttpConnectionsHelper {
	public static Cookie current_cookie = null;
	private static Date cookieExpiryDate = new Date();
	public static final String CONNECT = "connect";
	public static final String POLL = "poll";
	public static final String DISCONNECT = "disconnect";
	public static int CONNECTION_TIMEOUT = 5500;
	private static final int SOCKET_TIMEOUT = 5500; 
	private static final int MCC_TIMEOUT = 5500;
	public static int POLLING_ENABLED = 1;
	private static final String TAG = HttpConnectionsHelper.class.getSimpleName();
	private static String APP_TAG = "APP_TAG"; 
	private DBManager dbManager;
	
	private BasicCookieStore cookieStore;
	private BasicHttpContext localContext;
	
	/**
	 * 
	 * @param type
	 * @param url
	 * @param data
	 * @return httpResponse
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public synchronized HttpResponse doPost(String type ,String url, String data) throws ClientProtocolException, IOException {
		HttpResponse httpResponse = null;
		HttpPost httpPost = new HttpPost(url);
		HttpParams httpParameters = httpPost.getParams();  
	    httpParameters.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 
	            CONNECTION_TIMEOUT);
	    httpParameters.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, SOCKET_TIMEOUT);
	    httpParameters.setLongParameter(ConnManagerPNames.TIMEOUT, MCC_TIMEOUT);
	    
	    DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
        StringEntity s = new StringEntity(data.toString());
        s.setContentEncoding("UTF-8");
        s.setContentType("application/xml");
        httpPost.addHeader("connection-type", type);
        httpPost.setEntity(s);
        httpPost.addHeader("accept", "application/xml");
        
        if (current_cookie == null || current_cookie.isExpired(new Date())) {
        	try {
        		httpResponse = httpclient.execute(httpPost);
				List<Cookie> cookies = httpclient.getCookieStore().getCookies();
		 	    if (cookies.isEmpty()) {
		 	    	Log.d(TAG,"None");
		 	    } else {
		 	    	current_cookie = cookies.get(0);
		 	        cookieExpiryDate = current_cookie.getExpiryDate();
		 	        Log.d(TAG,"Curent cookie expiry : " + cookieExpiryDate);
		 	    }
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				Log.d(TAG,e.toString());
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG,e.toString());
			}
		    
        }else {
	    	httpPost.addHeader("accept", "application/xml");
	        httpclient.getCookieStore().addCookie(current_cookie);
	        try {
	        	httpResponse = httpclient.execute(httpPost);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				Log.d(TAG,e.toString());
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG,e.toString());
			}
	    }
        return httpResponse;
    }
	

	/**
	 * POST (HTTPS)
	 * @param url
	 * @param data
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	
	public HttpResponseHandler doSecurePost(Request request, String cert) throws ClientProtocolException, IOException {

		HttpResponse httpResponse = null;
		HttpResponseHandler serverResponse = new HttpResponseHandler(request.getType(), request.getDataId());
		HttpPost httpPost = null;
		TLSManager tlsManager = new TLSManager(cert);
		DefaultHttpClient httpclient = tlsManager.getTLSHttpClient();

		cookieStore = new BasicCookieStore();
		localContext = new BasicHttpContext();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		if (httpclient !=null) {
			httpPost = new HttpPost(request.getUrl());
			
			HttpParams httpParameters = httpPost.getParams();  
		    httpParameters.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
		    httpParameters.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, SOCKET_TIMEOUT);
		    httpParameters.setLongParameter(ConnManagerPNames.TIMEOUT, MCC_TIMEOUT);

		    StringEntity s = new StringEntity(request.getData().toString());
	        s.setContentEncoding("UTF-8");
	        s.setContentType("application/xml");
	        httpPost.setEntity(s);
	        httpPost.addHeader("accept", "application/xml");
	        httpPost.addHeader("connection-type", request.getType());
	        httpPost.setHeader("poll-interval", getInStringSeconds(request.getPollInterval()));
		}

	    try {
			Cookie retreivedCookie = getCookieFromDB();
			boolean isCookieStoreEmpty = cookieStore.getCookies().size() == 0 ? true : false; 
			httpResponse = httpclient.execute(httpPost,localContext);
	        serverResponse.setResponse(httpResponse);
		    boolean cookieFound = false;
		    if (!isCookieStoreEmpty){
		    	for (Cookie c : cookieStore.getCookies()){
		    		if (retreivedCookie != null){
		    			if (c.getValue().equals(retreivedCookie.getValue()) ){
		    				cookieFound = true;
		    				serverResponse.setNewSession(false,DetailedStatuses.SESSION_UPDATED);
		    				Log.d(TAG+"_COOKIE","After doSecurePost, cookie updated from server "+retreivedCookie.toString());
		    			    Log.d(TAG+"_COOKIE","After doSecurePost, Retreived cookie : "+ retreivedCookie.getValue());
		    			}
		    		}
		    	}
		    	if (!cookieFound) {
		    		serverResponse.setNewSession(true,DetailedStatuses.SUCCESS_NEW_SESSION);
		    		retreivedCookie = cookieStore.getCookies().get(0);
		    		Log.d(TAG+"_COOKIE"," After doSecurePost, New cookie used: "+ retreivedCookie.getValue());
		    	}
		    } else {
	    		serverResponse.setNewSession(true,DetailedStatuses.SUCCESS_NEW_SESSION);
	    		retreivedCookie = cookieStore.getCookies().get(0);
	    		Log.d(TAG+"_COOKIE"," After doSecurePost, New cookie used: "+ retreivedCookie.getValue());
		    }
			saveCookiesToDB();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Log.e(APP_TAG,"doSecurePost"+ e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(APP_TAG, "doSecurePost"+e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(APP_TAG, "doSecurePost"+e.toString());
		}
		

        return serverResponse;
		
    }

	public void saveCookiesToDB() {
		List<Cookie> cookies = cookieStore.getCookies();
		if (cookies.isEmpty()) {
			Log.d(TAG, "No cookies");
		} else {
			dbManager = new DBManager(ConnectionManager.context);
			dbManager.openDB();
			try {
				for (Cookie c : cookies) {
					dbManager.insertCookie(c);
				}
				dbManager.closeDB();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (dbManager != null) dbManager.closeDB();
			}
		}

	}
	
	public Cookie getCookieFromDB() {
		cookieStore.clear();
		dbManager = new DBManager(ConnectionManager.context);
		dbManager.openDB();

		try {
			if (dbManager.getCookie(cookieStore)!=null){
				Cookie cookie = dbManager.getCookie(cookieStore);
				dbManager.closeDB();
				return cookie;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if (dbManager != null) dbManager.closeDB();
		}
		return null;
	}
	
	private static String getInStringSeconds(String pollInterval) {
		int pollIntervalInSeconds = (Integer.parseInt(pollInterval) / 1000) % 60 ;
		return Integer.toString(pollIntervalInSeconds);
	}

}

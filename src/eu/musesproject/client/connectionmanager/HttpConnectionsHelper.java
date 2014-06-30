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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

/**
 * Helper class for connection Manager handles POST and GET request with the server
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */


public abstract class HttpConnectionsHelper {
	public static final boolean D = false;
	public static Cookie cookie;
	static HttpContext localContext = new BasicHttpContext();
	static Date cookieExpiryDate = new Date();
	public static final String CONNECT = "connect";
	public static final String POLL = "poll";
	public static final String DISCONNECT = "disconnect";
	private static Date lastDate = new Date();
	private HttpResponse httpResponse = null;
	private static final int TIMEOUT = 5000; 
	
	/**
	 * Http get implementation               // Not Used for Muses
	 * @param url
	 * @param data
	 * @return httpResponse
	 */
	public synchronized HttpResponse doGet(String url, String data) {
	    DefaultHttpClient httpclient = new DefaultHttpClient();
	    HttpResponse httpResponse = null;
	    String dataAppendedURL;
	    // Prepare a request object
		String formatedData = data.replace(" ","%20");
		formatedData = formatedData.replace("\"","%22");
		formatedData = formatedData.replace("}", "%7D");
		formatedData = formatedData.replace("{", "%7B");
		formatedData = formatedData.replace("]", "%5D");
		formatedData = formatedData.replace("[", "%5B");
	    
		if (data.equalsIgnoreCase("disconnect"))
	    	dataAppendedURL = url+"?disconnect=";
	    else dataAppendedURL = url+"?data="+formatedData;
	    HttpGet httpget = new HttpGet(dataAppendedURL);
	    httpget.setHeader("Content-Type", "text/plain; charset=utf-8"); // Change it JSON
	    httpget.setHeader("Expect", "100-continue");
	    // Execute the request
	    if (cookie == null || cookie.isExpired(new Date())){
	    	try {
				httpResponse = httpclient.execute(httpget);
				List<Cookie> cookies = httpclient.getCookieStore().getCookies();
		 	    if (cookies.isEmpty()) {
		 	    	if (D) System.out.println("None");
		 	    } else {
		 	    	for (int i = 0; i < cookies.size(); i++) {
		 	              System.out.println("- " + cookies.get(i).toString());
		 	        }
		 	        cookie = cookies.get(0);
		 	        cookieExpiryDate = cookie.getExpiryDate();
		 	        if (D) System.out.println("Curent cookie expiry : " + cookieExpiryDate);
		 	    }
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
	    } else {
	    	httpget.addHeader("accept", "application/xml");
	        httpclient.getCookieStore().addCookie(cookie);
	        try {
	        	httpResponse = httpclient.execute(httpget);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
		return httpResponse;

	}
	

	/**
	 * Http post implementation 
	 * @param url
	 * @param data
	 * @return httpResponse
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	
	public synchronized HttpResponse doPost(String type ,String url, String data) throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost(url);
		HttpParams httpParameters = new BasicHttpParams();  
	    HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT);
	    DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
        StringEntity s = new StringEntity(data.toString());
        s.setContentEncoding("UTF-8");
        s.setContentType("application/xml");
        httpPost.addHeader("connection-type", type);
        httpPost.setEntity(s);
        httpPost.addHeader("accept", "application/xml");
        if (cookie == null || cookie.isExpired(new Date())) {
        	try {
        		httpResponse = httpclient.execute(httpPost);
				List<Cookie> cookies = httpclient.getCookieStore().getCookies();
		 	    if (cookies.isEmpty()) {
		 	        if (D) System.out.println("None");
		 	    } else {
		 	    	for (int i = 0; i < cookies.size(); i++) {
		 	              System.out.println("- " + cookies.get(i).toString());
		 	        }
		 	        cookie = cookies.get(0);
		 	        cookieExpiryDate = cookie.getExpiryDate();
		 	        if (D) System.out.println("Curent cookie expiry : " + cookieExpiryDate);
		 	    }
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		    
        }else {
	    	httpPost.addHeader("accept", "application/xml");
	        httpclient.getCookieStore().addCookie(cookie);
	        try {
	        	httpResponse = httpclient.execute(httpPost);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
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
	
	public HttpResponse doSecurePost(Request request) throws ClientProtocolException, IOException {
	
		HttpResponse httpResponse = null;
		HttpPost httpPost = null;
		TLSManager tlsManager = new TLSManager();
		DefaultHttpClient httpclient = tlsManager.getTLSHttpClient();

		if (httpclient !=null) {
			HttpParams httpParameters = new BasicHttpParams();  
		    HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT);
			httpPost = new HttpPost(request.getUrl());
	        StringEntity s = new StringEntity(request.getData().toString());
	        s.setContentEncoding("UTF-8");
	        s.setContentType("application/xml");
	        httpPost.setEntity(s);
	        httpPost.addHeader("accept", "application/xml");
	        httpPost.addHeader("connection-type", request.getType());
	        httpPost.setHeader("poll-interval", getInStringSeconds(request.getPollInterval()));
		}

		if (cookie != null && !isSessionExpired(new Date(), AlarmReceiver.LAST_SENT_POLL_INTERVAL)) {
			httpclient.getCookieStore().addCookie(cookie);
		    try {
		        httpResponse = httpclient.execute(httpPost);
			} catch (ClientProtocolException e) {
			if (D) e.printStackTrace();
			} catch (IOException e) {
			if (D) e.printStackTrace();
			}
		} else {
			try {
        		httpResponse = httpclient.execute(httpPost);
				List<Cookie> cookies = httpclient.getCookieStore().getCookies();
		 	    if (!cookies.isEmpty()) {
		 	    	cookie = cookies.get(0);
		 	    } 
		 	    
			} catch (ClientProtocolException e) {
				if (D) e.printStackTrace();
			} catch (IOException e) {
				if (D) e.printStackTrace();
			}
		}
		AlarmReceiver.LAST_SENT_POLL_INTERVAL = Integer.parseInt(request.getPollInterval());
        return httpResponse;
		
    }
	
	private boolean isSessionExpired(Date newDate, int pollInterval){
		long diff = newDate.getTime() - lastDate.getTime();
		long diffSeconds = (diff / 1000) % 60;
		if (D) System.out.println("Diffrence secondds: " + diffSeconds);
		if (D) System.out.println("NewDate: " + newDate);
		if (D) System.out.println("LastDate: " + lastDate);
		lastDate = newDate;
		if (diffSeconds > getInSeconds(AlarmReceiver.LAST_SENT_POLL_INTERVAL)*2){
			return true;
		}
		return false;
	}


	private int getInSeconds(int pollInterval) {
		int pollIntervalInSeconds = (pollInterval / 1000) % 60 ;
		return pollIntervalInSeconds;
	}
	
	private String getInStringSeconds(String pollInterval) {
		int pollIntervalInSeconds = (Integer.parseInt(pollInterval) / 1000) % 60 ;
		return Integer.toString(pollIntervalInSeconds);
	}

	public HttpResponse getHttpResponse(){
		return httpResponse;
	}
}

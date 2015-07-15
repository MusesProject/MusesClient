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
import eu.musesproject.client.ui.DebugFileLog;

/**
 * Helper class for connection Manager handles POST and GET request with the
 * server
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */

public abstract class HttpConnectionsHelper {
	private static final String TAG = HttpConnectionsHelper.class.getSimpleName();
	private static String APP_TAG = "APP_TAG";
	public static final String CONNECT = "connect";
	public static final String POLL = "poll";
	public static final String DISCONNECT = "disconnect";
	public static int CONNECTION_TIMEOUT = 10000;
	private static final int SOCKET_TIMEOUT = 10000;
	private static final int MCC_TIMEOUT = 10000;
	
	public static final int MAX_LOGIN_ATTEMPTS = 5;
	public static int POLLING_ENABLED = 1;
	public static final String SERVER_CONTEXT_PATH = "/server";
	public static final String SERVER_SERVLET_PATH = "/commain";
	public static Cookie retreivedCookie = null;
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
	public synchronized HttpResponseHandler doPost(Request request)
			throws ClientProtocolException, IOException {

		HttpResponse httpResponse = null;
		HttpResponseHandler serverResponse = new HttpResponseHandler(
				request.getType(), request.getDataId());
		HttpPost httpPost = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();

		cookieStore = new BasicCookieStore();
		localContext = new BasicHttpContext();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		if (httpclient != null) {
			httpPost = new HttpPost(request.getUrl());

			HttpParams httpParameters = httpPost.getParams();
			httpParameters
					.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
							CONNECTION_TIMEOUT);
			httpParameters.setIntParameter(CoreConnectionPNames.SO_TIMEOUT,
					SOCKET_TIMEOUT);
			httpParameters.setLongParameter(ConnManagerPNames.TIMEOUT,
					MCC_TIMEOUT);

			StringEntity s = new StringEntity(request.getData().toString());
			s.setContentEncoding("UTF-8");
			s.setContentType("application/xml");
			httpPost.setEntity(s);
			httpPost.addHeader("accept", "application/xml");
			httpPost.addHeader("connection-type", request.getType());
			httpPost.setHeader("poll-interval",
					getInStringSeconds(request.getPollInterval()));
		}

		try {
			if (retreivedCookie == null) {
				// Updating cookie if present in DB
				retreivedCookie = getCookieFromDB();
			}
			boolean isCookieStoreEmpty = cookieStore.getCookies().size() == 0 ? true
					: false;
			httpResponse = httpclient.execute(httpPost, localContext);
			serverResponse.setResponse(httpResponse);
			boolean cookieFound = false;
			if (!isCookieStoreEmpty) {
				for (Cookie c : cookieStore.getCookies()) {
					if (retreivedCookie != null) {
						if (c.getValue().equals(retreivedCookie.getValue())) {
							cookieFound = true;
							serverResponse.setNewSession(false,
									DetailedStatuses.SESSION_UPDATED);
							Log.d(TAG,	"After doSecurePost=> requestType: "+request.getType()+", poll-interval: "+request.getPollIntervalInSeconds()+ ", cookie updated from server "
											+ retreivedCookie.toString());
							Log.d(TAG, "After doSecurePost=> requestType: " +request.getType()+", poll-interval: "+request.getPollIntervalInSeconds()+ ", Retreived cookie: "
									+ retreivedCookie.getValue() + " expires: "
									+ retreivedCookie.getExpiryDate());
						}
					}
				}
				if (!cookieFound) {
					serverResponse.setNewSession(true,
							DetailedStatuses.SUCCESS_NEW_SESSION);
					if (cookieStore.getCookies().size() > 0) {
						retreivedCookie = cookieStore.getCookies().get(0);
						saveCookiesToDB();
					}
					Log.d(TAG,"After doSecurePost=> requestType: "+request.getType()+", poll-interval: "+request.getPollIntervalInSeconds()+ ", New cookie used: "
									+ retreivedCookie.getValue());
				}
			} else {
				serverResponse.setNewSession(true,
						DetailedStatuses.SUCCESS_NEW_SESSION);
				if (cookieStore.getCookies().size() > 0) {
					retreivedCookie = cookieStore.getCookies().get(0);
					saveCookiesToDB();
				}
				Log.d(TAG, " After doSecurePost=> requestType: "+request.getType()+", poll-interval: "+request.getPollIntervalInSeconds()+ ", New cookie used: "
						+ retreivedCookie.getValue());
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Log.e(APP_TAG, "doSecurePost" + e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(APP_TAG, "doSecurePost" + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(APP_TAG, "doSecurePost" + e.toString());
		}

		return serverResponse;

	}

	/**
	 * POST (HTTPS)
	 * 
	 * @param url
	 * @param data
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */

	public synchronized HttpResponseHandler doSecurePost(Request request, String cert)
			throws ClientProtocolException, IOException {

		HttpResponse httpResponse = null;
		HttpResponseHandler serverResponse = new HttpResponseHandler(
				request.getType(), request.getDataId());
		HttpPost httpPost = null;
		TLSManager tlsManager = new TLSManager(cert);
		DefaultHttpClient httpclient = tlsManager.getTLSHttpClient();

		cookieStore = new BasicCookieStore();
		localContext = new BasicHttpContext();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		if (httpclient != null) {
			httpPost = new HttpPost(request.getUrl());

			HttpParams httpParameters = httpPost.getParams();
			httpParameters
					.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
							CONNECTION_TIMEOUT);
			httpParameters.setIntParameter(CoreConnectionPNames.SO_TIMEOUT,
					SOCKET_TIMEOUT);
			httpParameters.setLongParameter(ConnManagerPNames.TIMEOUT,
					MCC_TIMEOUT);

			StringEntity s = new StringEntity(request.getData().toString());
			s.setContentEncoding("UTF-8");
			s.setContentType("application/xml");
			httpPost.setEntity(s);
			httpPost.addHeader("accept", "application/xml");
			httpPost.addHeader("connection-type", request.getType());
			httpPost.setHeader("poll-interval",
					getInStringSeconds(request.getPollInterval()));
		}

		try {
			if (retreivedCookie == null) {
				// Updating cookie if present in DB
				retreivedCookie = getCookieFromDB();
			} else {
				cookieStore.addCookie(retreivedCookie);
			}
			boolean isCookieStoreEmpty = cookieStore.getCookies().size() == 0 ? true
					: false;
			httpResponse = httpclient.execute(httpPost, localContext);
			serverResponse.setResponse(httpResponse);
			boolean cookieFound = false;
			if (!isCookieStoreEmpty) {
				for (Cookie c : cookieStore.getCookies()) {
					if (retreivedCookie != null) {
						if (c.getValue().equals(retreivedCookie.getValue())) {
							cookieFound = true;
							serverResponse.setNewSession(false,
									DetailedStatuses.SESSION_UPDATED);
							Log.d(TAG, "After doSecurePost=> requestType: " +request.getType()+", poll-interval: "+request.getPollIntervalInSeconds()+ ", Retreived cookie: "
									+ retreivedCookie.getValue() + " expires: "
									+ retreivedCookie.getExpiryDate());
							DebugFileLog.write(TAG+ " After doSecurePost=> requestType: " +request.getType()+", poll-interval: "+request.getPollIntervalInSeconds()+ ", Retreived cookie: "
									+ retreivedCookie.getValue() + " expires: "
									+ retreivedCookie.getExpiryDate());

						}
					}
				}
				if (!cookieFound) {
					serverResponse.setNewSession(true,
							DetailedStatuses.SUCCESS_NEW_SESSION);
					if (cookieStore.getCookies().size() > 0) {
						retreivedCookie = cookieStore.getCookies().get(0);
						saveCookiesToDB();
					}
					Log.d(TAG,"After doSecurePost=> requestType: "+request.getType()+", poll-interval: "+request.getPollIntervalInSeconds()+ ", New cookie used: "
							+ retreivedCookie.getValue());
					DebugFileLog.write(TAG+" After doSecurePost=> requestType: "+request.getType()+", poll-interval: "+request.getPollIntervalInSeconds()+ ", New cookie used: "
							+ retreivedCookie.getValue());
				}
			} else {
				serverResponse.setNewSession(true,
						DetailedStatuses.SUCCESS_NEW_SESSION);
				if (cookieStore.getCookies().size() > 0) {
					retreivedCookie = cookieStore.getCookies().get(0);
					saveCookiesToDB();
				}
				Log.d(TAG, " After doSecurePost=> requestType: "+request.getType()+", poll-interval: "+request.getPollIntervalInSeconds()+ ", New cookie used: "
							+ retreivedCookie.getValue());
				DebugFileLog.write(TAG+ " After doSecurePost=> requestType: "+request.getType()+", poll-interval: "+request.getPollIntervalInSeconds()+ ", New cookie used: "
						+ retreivedCookie.getValue());

			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Log.e(APP_TAG, "doSecurePost" + e.toString());
			DebugFileLog.write(APP_TAG+ " doSecurePost" + e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(APP_TAG, "doSecurePost" + e.toString());
			DebugFileLog.write(APP_TAG+ " doSecurePost" + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(APP_TAG, "doSecurePost" + e.toString());
			DebugFileLog.write(APP_TAG+ " doSecurePost" + e.toString());
		}

		return serverResponse;

	}

	public void saveCookiesToDB() {
		List<Cookie> cookies = cookieStore.getCookies();
		if (cookies.isEmpty()) {
			Log.d(TAG, "No cookies");
			DebugFileLog.write(TAG+" No cookies");
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
				if (dbManager != null)
					dbManager.closeDB();
			}
		}

	}

	public Cookie getCookieFromDB() {
		dbManager = new DBManager(ConnectionManager.context);
		dbManager.openDB();

		try {
			if (dbManager.getCookie(cookieStore) != null) {
				Cookie cookie = dbManager.getCookie(cookieStore);
				dbManager.closeDB();
				return cookie;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dbManager != null)
				dbManager.closeDB();
		}
		return null;
	}

	private static String getInStringSeconds(String pollInterval) {
		int pollIntervalInSeconds = (Integer.parseInt(pollInterval) / 1000) % 60;
		return Integer.toString(pollIntervalInSeconds);
	}

}

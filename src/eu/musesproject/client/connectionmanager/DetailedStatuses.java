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
/**
 * Stores detailed server status which are sent to other module using status callback
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */

public class DetailedStatuses {
	public static final int SUCCESS = 101;     									// ---> 200 OK
	public static final int INCORRECT_URL = 102;								// ---> 400 Bad Request
	public static final int NOT_ALLOWED_FROM_SERVER_UNAUTHORIZED = 103;		// ---> 401 Unauthorized
	public static final int NOT_FOUND = 104;										// ---> 404 Not Found
	public static final int INTERNAL_SERVER_ERROR = 105; 						// ---> 500 Internal server error
	public static final int UNKNOWN_ERROR = 106;
	public static final int SERVER_NOT_AVAIABLE = 107;  						// ---> 503 Server Unavailable
	public static final int NO_INTERNET_CONNECTION = 108;
	public static final int INCORRECT_CERTIFICATE = 109;
	public static final int SUCCESS_NEW_SESSION = 110;
	public static final int SESSION_UPDATED = 111;
	
	public static String getDetailedStatusString(int statusCode){
		switch (statusCode)	 {
		case SUCCESS:
			return "SUCCESS";
		case INCORRECT_URL:
			return "INCORRECT_URL";
		case NOT_ALLOWED_FROM_SERVER_UNAUTHORIZED:
			return "NOT_ALLOWED_FROM_SERVER_UNAUTHORIZED";
		case NOT_FOUND:
			return "NOT_FOUND";
		case INTERNAL_SERVER_ERROR:
			return "INTERNAL_SERVER_ERROR";
		case UNKNOWN_ERROR:
			return "UNKNOWN_ERROR";
		case SERVER_NOT_AVAIABLE:
			return "SERVER_NOT_AVAIABLE";
		case NO_INTERNET_CONNECTION:
			return "NO_INTERNET_CONNECTION";
		case INCORRECT_CERTIFICATE:
			return "INCORRECT_CERTIFICATE";
		case SUCCESS_NEW_SESSION:
			return "SUCCESS_NEW_SESSION";
		case SESSION_UPDATED:
			return "SESSION_UPDATED";
		}
		return "UNKNOWN_DETAILED_STATUS";
	}
	
}
/*
 * version 1.0 - MUSES prototype software
 * Copyright MUSES project (European Commission FP7) - 2013 
 * 
 */

package eu.musesproject.client.connectionmanager;

/**
 * Stores detailed server status which are sent to other module using status callback
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */

public class DetailedStatuses {


	// Detailed Server Statuses
	public static final int SUCCESS = 101;     				// ---> 200 OK
	public static final int INCORRECT_URL = 102;				// ---> 400 Bad Request
	public static final int NOT_ALLOWED_FROM_SERVER = 103;	// ---> 401 Unauthorized
	public static final int SERVER_NOT_AVAIABLE = 104;		// ---> 404 Not Found
	public static final int UNKNOWN_ERROR = 105;
	public static final int NO_INTERNET_CONNECTION = 106;
	// Should define more responses if needed in future

}
/*
 * version 1.0 - MUSES prototype software
 * Copyright MUSES project (European Commission FP7) - 2013 
 * 
 */

package eu.musesproject.client.connectionmanager;

/**
 * Stores Server statuses which are sent to other module using status callback
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */

public class Statuses {
	
	public static final int OFFLINE = 0;
	public static final int ONLINE = 1;
	public static int CURRENT_STATUS = 0; 
	public static boolean STATUS_CHANGED = false;
	public static final int DATA_SEND_FAILED = 3;
	public static final int DATA_SEND_OK = 4;
	public static final int CONNECTION_FAILED = 5;
	public static final int CONNECTION_OK = 6;
	public static final int DISCONNECTED = 7;

}

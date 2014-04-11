/*
 * version 1.0 - MUSES prototype software
 * Copyright MUSES project (European Commission FP7) - 2013 
 * 
 */

package eu.musesproject.client.connectionmanager;

import android.content.Context;

/**
 * Callback interface implemented by other modules
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */

public interface IConnectionManager {

	void connect (String url, int pollInterval, int sleepPollInterval, IConnectionCallbacks callbacks, Context context);
	void setPollTimeOuts (int pollInterval, int sleepPollInterval);
	void sendData (String data);
	void disconnect ();
}
/*
 * version 1.0 - MUSES prototype software
 * Copyright MUSES project (European Commission FP7) - 2013 
 * 
 */
package eu.musesproject.client.connectionmanager;


/**
 * The Interface IConnectionCallbacks
 * @author yasir (SWE)
 * @version Sep 25, 2013
 */

public interface IConnectionCallbacks {
    int receiveCb(String receiveData);
    int statusCb(int status, int detailedStatus); 
}

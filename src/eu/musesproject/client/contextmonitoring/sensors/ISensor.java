package eu.musesproject.client.contextmonitoring.sensors;

import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.contextmodel.ContextEvent;


public interface ISensor {
	/** This method enables the sensor. The sensor starts to collect data */
	void enable();
	
	/** This method disables the sensor. The sensor stops to collect data and unregisters all broadcast receivers if there is one */
	void disable();
	
	/**
	 * register a listener to notify other components whenever a context event is fired
	 * @param listener of type {@link ContextListener}
	 */
	void addContextListener(ContextListener listener);
	
	ContextEvent getLastFiredContextEvent();
}
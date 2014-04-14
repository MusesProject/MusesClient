package eu.musesproject.client.contextmonitoring.sensors;

import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.contextmodel.ContextEvent;

public interface ISensor {
    /**
     * max size of the array for the context event history
     */
    static final int CONTEXT_EVENT_HISTORY_SIZE = 2;


    /**
     * Must be called before a sensor is enabled
     * register a listener to notify other components whenever a context event is fired
     * @param listener of type {@link ContextListener}
     */
    void addContextListener(ContextListener listener);

    /**
     * Must be called before a sensor is disabled.
     * unregister a listener
     * @param listener of type {@link ContextListener}
     */
    void removeContextListener(ContextListener listener);

    /** This method enables the sensor. The sensor starts to collect data */
    void enable();

    /** This method disables the sensor. The sensor stops to collect data and unregisters all broadcast receivers if there is one */
    void disable();

    /**
     * @return the last fired context event
     */
    ContextEvent getLastFiredContextEvent();
}
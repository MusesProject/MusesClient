package eu.musesproject.client.contextmonitoring.sensors;

/*
 * #%L
 * musesclient
 * %%
 * Copyright (C) 2013 - 2014 HITEC
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

import java.util.List;

import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.client.db.entity.SensorConfiguration;
import eu.musesproject.contextmodel.ContextEvent;

public interface ISensor {
	public static String CONFIG_KEY_ENABLED = "enabled"; 
	
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
    
    /**
     * Method to setup the configuration of a sensor
     * @param config {@link SensorConfiguration}
     */
    void configure(List<SensorConfiguration> config);
}
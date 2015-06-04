package eu.musesproject.client.contextmonitoring.test;

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

import android.content.Context;
import android.test.AndroidTestCase;
import eu.musesproject.client.contextmonitoring.SensorController;
import eu.musesproject.client.contextmonitoring.sensors.ISensor;
import eu.musesproject.contextmodel.ContextEvent;

import java.util.List;
import java.util.Map;

public class SensorControllerTest extends AndroidTestCase {
    private Context context;
    private Map<String, ISensor> activeSensors;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        context = getContext();
    }

    public void testSensorControllerInitialization() {
        SensorController sensorController = SensorController.getInstance(context);
        // test if an instance of the sensor controller is created
        assertNotNull(sensorController);
    }

    public void testGetLastFiredEvents() {
        List<ContextEvent> contextEvents =  SensorController.getInstance(context).getLastFiredEvents();
        // test if a List of context events is received
        assertNotNull(contextEvents);
        // test if the size is 0, because no context events are fired at this time
        assertEquals("contextEvents size:", 0 , contextEvents.size());
    }

    public void testStopAllSensors() {
        SensorController.getInstance(context).stopAllSensors();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
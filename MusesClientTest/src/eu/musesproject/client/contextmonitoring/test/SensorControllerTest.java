package eu.musesproject.client.contextmonitoring.test;

import android.content.Context;
import android.test.AndroidTestCase;
import eu.musesproject.client.contextmonitoring.SensorController;
import eu.musesproject.client.contextmonitoring.sensors.*;
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

    public void testStartAllSensors() {
        SensorController.getInstance(context).startAllSensors();
        // test if the map that holds the sensors exist
        //assertNotNull(activeSensors);
        // test if the map of the active sensors is as large as expected
        //assertEquals("activeSensor size:", 5, activeSensors.size());
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
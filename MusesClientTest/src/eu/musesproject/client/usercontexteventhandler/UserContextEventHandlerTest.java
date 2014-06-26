package eu.musesproject.client.usercontexteventhandler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.test.AndroidTestCase;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.sensors.AppSensor;
import eu.musesproject.client.contextmonitoring.sensors.ConnectivitySensor;
import eu.musesproject.client.db.entity.Property;
import eu.musesproject.client.db.handler.DBManager;
import eu.musesproject.contextmodel.ContextEvent;

public class UserContextEventHandlerTest extends AndroidTestCase {
    private DBManager dbManager;
    private Context context;
    private UserContextEventHandler handler;
    private List<ContextEvent> contextEvents;
    private final long timestamp = System.currentTimeMillis();

    public void setUp(){
        context = getContext();
        dbManager = new DBManager(context);
        handler = UserContextEventHandler.getInstance();
        handler.setContext(getContext());

        // set up test data
        contextEvents = new ArrayList<ContextEvent>();
        ContextEvent event = new ContextEvent();
        event.setTimestamp(timestamp);
        event.setType(ConnectivitySensor.TYPE);
        event.addProperty("wifiEnabled", "true");
        event.addProperty("wifiConnected", "true");
        event.addProperty("airplaneMode", "false");
        contextEvents.add(event);

        event = new ContextEvent();
        event.setTimestamp(timestamp);
        event.setType(AppSensor.TYPE);
        event.addProperty("appName", "gmail");
        contextEvents.add(event);
    }

    public void testStoreContextEvent() {
        handler.storeContextEvent(null, null, contextEvents);
        dbManager.openDB();

        // number of stored events
        assertEquals("no of stored context events", 2, dbManager.getNoOfContextEventsStored());
        List<eu.musesproject.client.db.entity.ContextEvent> storedContextEvents = dbManager.getAllStoredContextEvents();

        assertEquals("no of stored context events", 2, storedContextEvents.size());
        

        // connectivity context event
        eu.musesproject.client.db.entity.ContextEvent connectivityContextEvent = storedContextEvents.get(0);
        assertEquals("Connectivity context event", String.valueOf(timestamp), connectivityContextEvent.getTimestamp());
        assertEquals("Connectivity context event", ConnectivitySensor.TYPE, connectivityContextEvent.getType());
        List<Property> connectivityProperties = dbManager.getPropertiesOfContextEvent(storedContextEvents.get(0).getId());
        assertEquals("number of connectivity properties", 3, connectivityProperties.size());
        assertEquals("connectivity property: wifiConnected", "wifiConnected", connectivityProperties.get(0).getKey());
        assertEquals("connectivity property: wifiConnected", "true", connectivityProperties.get(0).getValue());
        assertEquals("connectivity property: wifiEnabled", "wifiEnabled", connectivityProperties.get(1).getKey());
        assertEquals("connectivity property: wifiEnabled", "true", connectivityProperties.get(1).getValue());
        assertEquals("connectivity property: airplaneMode", "airplaneMode", connectivityProperties.get(2).getKey());
        assertEquals("connectivity property: airplaneMode", "false", connectivityProperties.get(2).getValue());


        // app context event
        eu.musesproject.client.db.entity.ContextEvent appContextEvent = storedContextEvents.get(1);
        assertEquals("app context event", String.valueOf(timestamp), appContextEvent.getTimestamp());
        assertEquals("app context event", AppSensor.TYPE, appContextEvent.getType());
        List<Property> appProperties = dbManager.getPropertiesOfContextEvent(storedContextEvents.get(1).getId());
        assertEquals("number of app properties", 1, appProperties.size());
        assertEquals("connectivity property: appName", "appName", appProperties.get(0).getKey());
        assertEquals("connectivity property: appName", "gmail", appProperties.get(0).getValue());

        dbManager.closeDB();
    }

    public void testSendOfflineStoredContextEventsToServer() {
        //handler.sendOfflineStoredContextEventsToServer();
    }

    public void testSetContext() {
        handler.setContext(context);
        Field contextField = null;
        try {
            contextField = handler.getClass().getDeclaredField("context");
            contextField.setAccessible(true);

            assertNotNull(contextField);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void tearDown() throws Exception{
        super.tearDown();
    }
}

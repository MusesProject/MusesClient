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

import java.util.ArrayList;
import java.util.List;

import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.contextmodel.ContextEvent;

/**
 * @author christophstanik
 *
 * Class to collect information about accesses and modifications
 * to a specific directory or file in a given directory on the device
 */
public class FileSensor implements ISensor {
    private static final String TAG = FileSensor.class.getName();

    // sensor identifier
    public static final String TYPE = "CONTEXT_SENSOR_FILEOBSERVER";

    // context property keys
    public static final String PROPERTY_KEY_ID 			= "id";
    public static final String PROPERTY_KEY_FILE_EVENT 	= "fileevent";
    public static final String PROPERTY_KEY_PATH 		= "path";

    // possible events
    public static final String OPEN  	 	 = "open";
    public static final String ATTRIB 	 	 = "metadata";
    public static final String ACCESS 		 = "access";
    public static final String CREATE 		 = "create";
    public static final String DELETE 		 = "delete";
    public static final String MODIFY 		 = "modify";
    public static final String MOVED_FROM 	 = "moved_from";
    public static final String MOVED_TO 	 = "moved_to";
    public static final String MOVE_SELF 	 = "move_self";
    public static final String CLOSE_WRITE   = "close_write";
    public static final String CLOSE_NOWRITE = "close_no_write";

    private FileObserver fileObserver;
    private String fullPath;
    private String pathToObserve;

    private ContextListener listener;

    // history of fired context events
    List<ContextEvent> contextEventHistory;

    // file observation already started
    private boolean running;

    public FileSensor() {
        init();
    }

    // initializes all necessary default values
    private void init() {
        running = false;

        // example directory.
        pathToObserve = "/Pictures/Screenshots/"; // TODO add sensor config to set a directory
        fullPath =  Environment.getExternalStorageDirectory().getAbsolutePath() + pathToObserve;
        Log.d(TAG, "path:" + fullPath);

        contextEventHistory = new ArrayList<ContextEvent>(CONTEXT_EVENT_HISTORY_SIZE);
    }

    /**
     * This method enables the sensor and initiates the context event collection.
     * If the implemented File Observer fires an event it will be created in
     * {@link FileSensor#createContextEvent(String, String)}
     */
    @Override
    public void enable() {
        if(!running){
            Log.d(TAG, "start file observation");

            fileObserver = new FileObserver(fullPath) {
                // these variables are needed to prevent the context event creation multiple times a second.
                // this is necessary because the FileObserver fires the same event multiple times
                int oldEvent = - 1;
                long lastEventTimestamp = System.currentTimeMillis();
                long threshold = 1000;

                @Override
                public void onEvent(int event, String path) {
                    long eventTimeStamp = System.currentTimeMillis();

                    // add ALL_EVENTS to erase high bit values
                    event &= ALL_EVENTS;
                    String eventText = null;

                    if((oldEvent != event) || ((eventTimeStamp - lastEventTimestamp) >= threshold)) {
                        oldEvent = event;
                        lastEventTimestamp = eventTimeStamp;

                        switch(event){
                            case FileObserver.OPEN			: eventText = FileSensor.OPEN; 		   break;
                            case FileObserver.ATTRIB		: eventText = FileSensor.ATTRIB;  	   break;
                            case FileObserver.ACCESS		: eventText = FileSensor.ACCESS; 	   break;
                            case FileObserver.CREATE		: eventText = FileSensor.CREATE; 	   break;
                            case FileObserver.DELETE		: eventText = FileSensor.DELETE;	   break;
                            case FileObserver.MODIFY		: eventText = FileSensor.MODIFY;	   break;
                            case FileObserver.MOVED_FROM	: eventText = FileSensor.MOVED_FROM;   break;
                            case FileObserver.MOVED_TO		: eventText = FileSensor.MOVED_TO; 	   break;
                            case FileObserver.MOVE_SELF		: eventText = FileSensor.MOVE_SELF;    break;
                            case FileObserver.CLOSE_WRITE	: eventText = FileSensor.CLOSE_WRITE;  break;
                            case FileObserver.CLOSE_NOWRITE	: eventText = FileSensor.CLOSE_NOWRITE;break;
                            default: break;
                        }
                        if((eventText != null) && (path != null)) {
                            createContextEvent(eventText, path);
                        }
                    }
                }
            };
            fileObserver.startWatching();
            running = true;
        }
    }

    /**
     * create the context event for this sensor
     * @param eventText private static field of {@link FileSensor} that describes the event.
     * @param path related to the file that fires the event
     */
    private void createContextEvent(String eventText, String path) {
        String fullPath = this.fullPath + path;
        String id = String.valueOf(contextEventHistory != null ? (contextEventHistory.size() + 1) : - 1);

        ContextEvent contextEvent = new ContextEvent();
        contextEvent.setType(TYPE);
        contextEvent.setTimestamp(System.currentTimeMillis());
        contextEvent.addProperty(PROPERTY_KEY_ID, id);
        contextEvent.addProperty(PROPERTY_KEY_FILE_EVENT, eventText);
        contextEvent.addProperty(PROPERTY_KEY_PATH, fullPath);
        Log.d(TAG, "event received: " + eventText + " path: " +fullPath);

        // add context event to the context event history
        contextEventHistory.add(contextEvent);
        if(contextEventHistory.size() > CONTEXT_EVENT_HISTORY_SIZE) {
            contextEventHistory.remove(0);
        }
        if(listener != null) {
            listener.onEvent(contextEvent);
        }
    }


    @Override
    public void disable() {
        Log.d(TAG, "stop file observation...waiting for confirmation");
        if(running) {
            try {
                fileObserver.stopWatching();
                running = false;
                Log.d(TAG, "confirmation: file observation stopped");
            } catch (Exception e) {}
        }
    }


    @Override
    public void addContextListener(ContextListener listener) {
        this.listener = listener;
    }

    @Override
    public void removeContextListener(ContextListener listener) {
        this.listener = listener;
    }

    @Override
    public ContextEvent getLastFiredContextEvent() {
        if(contextEventHistory.size() > 0) {
            return contextEventHistory.get(contextEventHistory.size() - 1);
        }
        else {
            return null;
        }
    }

    public String getPathToObserve() {
        return pathToObserve;
    }

    public void setPathToObserve(String pathToObserve) {
        this.pathToObserve = pathToObserve;
    }
}
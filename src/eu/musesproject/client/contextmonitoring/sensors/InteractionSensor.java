package eu.musesproject.client.contextmonitoring.sensors;

import java.util.ArrayList;
import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.client.model.contextmonitoring.InteractionObservedApps;
import eu.musesproject.client.model.contextmonitoring.MailContent;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.contextmodel.ContextEvent;

public class InteractionSensor extends AccessibilityService implements ISensor {
   // sensor identifier
    public static final String TYPE = "CONTEXT_SENSOR_INTERACTION";

    // context property keys
    public static final String PROPERTY_KEY_ID 					= "id";
    public static final String PROPERTY_KEY_APP_NAME 			= "appname";
    public static final String PROPERTY_KEY_PACKAGE_NAME		= "packagename";
    public static final String PROPERTY_KEY_BACKGROUND_PROCESS 	= "backgroundprocess";

    private Context context;
    private ContextListener listener;

    // stores all fired context events of this sensor
    private List<ContextEvent> contextEventHistory;

    // hold this value, because just specific apps shall be observed
    private String appName;

	// holds a value that indicates if the sensor is enabled or disabled
    private boolean sensorEnabled;
    
    public InteractionSensor() {
    	init();
    }
    
    public InteractionSensor(String appName) {
    	this.appName = appName;
    	init();
    }
   
    
    // initializes all necessary default values
    private void init() {
	    sensorEnabled = false;
	    contextEventHistory = new ArrayList<ContextEvent>(CONTEXT_EVENT_HISTORY_SIZE);
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
	public void enable() {
        if (!sensorEnabled) {
            sensorEnabled = true;
        }
	}

	@Override
	public void disable() {
	  if(sensorEnabled) {
            sensorEnabled = false;
        }
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

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if(sensorEnabled) { // sensor must be enabled
			if(getAppName().equals(InteractionObservedApps.OBSERVED_GMAIL)) { // observe just defined apps
				observeGmail(event);
			}
		}
	}

	private void observeGmail(AccessibilityEvent event) {
		
	}
	
	private void createUserAction(Action action, MailContent content) {
		
	}

	@Override
	public void onInterrupt() {
		// ignore
	}
	
	public String getAppName() {
		return this.appName;
	}

    public void setAppName(String appName) {
		this.appName = appName;
	}
}
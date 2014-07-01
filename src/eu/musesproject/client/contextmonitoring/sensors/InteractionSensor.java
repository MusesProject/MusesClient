package eu.musesproject.client.contextmonitoring.sensors;

import java.util.ArrayList;
import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.client.model.contextmonitoring.InteractionDictionary;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.contextmodel.ContextEvent;

/**
 * 
 * @author danielgleim, christophstanik
 * 
 */
public class InteractionSensor extends AccessibilityService implements ISensor {
	private final static String TAG = InteractionSensor.class.getSimpleName();

	// sensor identifier
	public static final String TYPE = "CONTEXT_SENSOR_INTERACTION";

	// context property keys
	public static final String PROPERTY_KEY_ID = "id";
	public static final String PROPERTY_FOREGROUND_APP_PACKAGENAME = "apppackage";
	public static final String PROPERTY_FOREGROUND_APP_NAME = "appname";
	public static final String PROPERTY_CONTAINS_PASSWORDFIELDS = "passwordfields";

	private ContextListener listener;

	// history of fired context events
	List<ContextEvent> contextEventHistory;

	// holds a value that indicates if the sensor is enabled or disabled
	private boolean sensorEnabled;

	public InteractionSensor() {
		contextEventHistory = new ArrayList<ContextEvent>(CONTEXT_EVENT_HISTORY_SIZE);

		init();
	}

	private void init() {
		sensorEnabled = false;
	}

	@Override
	public void enable() {
		if (!sensorEnabled) {
			sensorEnabled = true;
		}
	}

	private void createContextEvent(String actionType, String containsPasswordField, String foregroundAppPackageName, String appName) {
		// create context event
		ContextEvent contextEvent = new ContextEvent();
		contextEvent.setType(TYPE);
		contextEvent.setTimestamp(System.currentTimeMillis());
		contextEvent.addProperty(PROPERTY_KEY_ID, String.valueOf(contextEventHistory != null ? (contextEventHistory.size() + 1) : -1));
		contextEvent.addProperty(PROPERTY_FOREGROUND_APP_PACKAGENAME, foregroundAppPackageName);
		contextEvent.addProperty(PROPERTY_FOREGROUND_APP_NAME, appName);
		contextEvent.addProperty(PROPERTY_CONTAINS_PASSWORDFIELDS, containsPasswordField);
		
		if (listener != null) {
			listener.onEvent(contextEvent);
		}
	}

	/**
	 * Method that returns whether an app is listed as an app
	 * that should be observed. Like e.g. Gmail to detect interactions
	 * with the mail application
	 * 
	 * @param appName current active app
	 * @return true if app should be observed
	 */
	private boolean isAppUnderObservation(String appName) {
		return appName.equalsIgnoreCase("Gmail"); // just for testing purpose hard coded.
	}

	@Override
	public void disable() {
		if (sensorEnabled) {
			sensorEnabled = false;
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
		if (contextEventHistory.size() > 0) {
			return contextEventHistory.get(contextEventHistory.size() - 1);
		} else {
			return null;
		}
	}

	@Override
	protected void onServiceConnected() {
		Log.d(TAG, "DetectPasswordFieldsAccessibilityService:onServiceConnected");
		super.onServiceConnected();
	}

	public void onDestroy() {
		Log.d(TAG, "DetectPasswordFieldsAccessibilityService:onDestroy");
		super.onDestroy();
	}

	/**
	 * This method iterates over all children of a given view (the
	 * AccessibilityNodeInfo to be precise). It checks if there are
	 * editTextFields declared as type=password and returns a respective
	 * boolean. This iteration can be used and extended for all kinds of checks.
	 * 
	 * @param source
	 *            The triggering source of the received AccessibilityEvent. Can
	 *            be a click, a user performed, or an updated View or similar.
	 * @param generation
	 *            Currently used for debugging mainly, to see a readable
	 *            representation of the Hierarchical View structure.
	 *            Additionally the criteria to stop recursive method calls.
	 * @return TRUE or FALSE, depending on whether a password field is displayed
	 *         in the current view or not.
	 */
	private boolean isOneChildPassword(AccessibilityNodeInfo source,
			String generation) {
		// Abort if the 25th generation is reached.
		// TODO This is used as criteria to abort the recursive method calls.
		// This needs to be changed and a better way to determine should be
		// found.
		if (generation.length() > 50) {
			return false;
		}
		if (source != null) {
			if (source.isPassword()) {
				return true;
			}
		}	

		boolean thisRun = false;
		boolean nextRun = false;

		try {
			for (int i = 0; i < source.getChildCount(); i++) {
				AccessibilityNodeInfo child = source.getChild(i);

				if (child.isPassword()) {
					thisRun = true;
				}

				// recursively call this method again, to check children of this child
				if (child.getChildCount() > 0) {
					nextRun = isOneChildPassword(child, generation.concat("> "));
				}
			}
		} catch (NullPointerException e) {
			Log.e(TAG, "This Nullpointer Exception should not have occured.");
		}
		return (thisRun || nextRun);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.accessibilityservice.AccessibilityService#onAccessibilityEvent
	 * (android.view.accessibility.AccessibilityEvent) Called whenever one
	 * AccessibilityEvent is received. The service needs to register for those
	 * Events. This is done in the xml file
	 * res/xml/accessibility_service_config.xml
	 */
	@Override
	public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		new InteractionObserverAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, accessibilityEvent);
	}

	@Override
	public void onInterrupt() {
		Log.d(TAG, "DetectPasswordFieldsAccessibilityService:onInterrupt");
	}
	
	private class InteractionObserverAsync extends AsyncTask<AccessibilityEvent, Void, Void> {

		@Override
		protected Void doInBackground(AccessibilityEvent... params) {
			if(params.length == 1) {
				AccessibilityEvent accessibilityEvent = params[0];
				if (sensorEnabled) {
					String foregroundAppPackageName = "unknown";
					try {
						foregroundAppPackageName = (String) accessibilityEvent.getSource().getPackageName();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
					
					// retrieve the current app name based on the package name
					String appName = "unknown";
					final PackageManager pm = getApplicationContext().getPackageManager();
					ApplicationInfo ai;
					try {
						ai = pm.getApplicationInfo(InteractionSensor.this.getPackageName(), 0);
					} catch (final NameNotFoundException e) {
						ai = null;
					}
					appName = (String) (ai != null ? pm.getApplicationLabel(ai) : "unknown");
					
					// just react on specified apps / app types
					if(isAppUnderObservation(appName)) {
						switch (accessibilityEvent.getEventType()) {
							// just react on view clicked events
							case AccessibilityEvent.TYPE_VIEW_CLICKED:
								String containsPasswordField = String.valueOf(isOneChildPassword(accessibilityEvent.getSource(), ""));
								String actionType = observeMailApplication(accessibilityEvent);
								if(actionType != null) {
									createContextEvent(actionType, containsPasswordField, foregroundAppPackageName, appName);
								}
							break;
						}
					}
				}
			}
			return null;
		}
		
		// returns the text of clicked view
		private String getEventText(AccessibilityEvent event) {
			StringBuilder sb = new StringBuilder();
	        for (CharSequence s : event.getText()) {
	            sb.append(s);
	        }
	        return sb.toString();
	    }
		
		private String observeMailApplication(AccessibilityEvent event) {
			String eventText = getEventText(event);
			
			// check (in different languages) if file is attached
			for(String dictionaryEntry : InteractionDictionary.ATTACH_FILE) { 
				if(eventText.equalsIgnoreCase(dictionaryEntry)) {
					return ActionType.FILE_ATTACHED;
				}
			}
			// check (in different languages) if mail is send
			for(String dictionaryEntry : InteractionDictionary.SEND) { 
				if(eventText.equalsIgnoreCase(dictionaryEntry)) {
					return ActionType.SEND_MAIL;
				}
			}
			
			return null;
		}
	}
}
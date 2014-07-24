package eu.musesproject.client.contextmonitoring.sensors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.client.model.contextmonitoring.InteractionDictionary;
import eu.musesproject.client.model.contextmonitoring.InteractionObservedApps;
import eu.musesproject.client.model.contextmonitoring.MailAttachment;
import eu.musesproject.client.model.contextmonitoring.MailContent;
import eu.musesproject.client.model.contextmonitoring.MailProperties;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.contextmodel.ContextEvent;

public class InteractionSensor extends AccessibilityService implements ISensor {
	private static final String TAG = InteractionSensor.class.getSimpleName();
	
	// sensor identifier
	public static final String TYPE = "CONTEXT_SENSOR_INTERACTION";

	// context property keys
	public static final String PROPERTY_KEY_ID = "id";
	public static final String PROPERTY_KEY_APP_NAME = "appname";
	public static final String PROPERTY_KEY_PACKAGE_NAME = "packagename";
	public static final String PROPERTY_KEY_BACKGROUND_PROCESS = "backgroundprocess";

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
		contextEventHistory = new ArrayList<ContextEvent>(
				CONTEXT_EVENT_HISTORY_SIZE);
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
		if (sensorEnabled) {
			sensorEnabled = false;
		}
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
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (sensorEnabled) { // sensor must be enabled
			// observe just definied apps
			if (getAppName().equals(InteractionObservedApps.OBSERVED_GMAIL)) { 
				new GmailObserver(event);
			}
		}
	}

	private void createUserAction(Action action, Map<String, String> actionProperties) {
		if(action.getActionType() == ActionType.FILE_ATTACHED) {
			
		}
		else if(action.getActionType() == ActionType.SEND_MAIL) {
			Log.d(TAG, "action.getActionType(): " + action.getActionType());
			for(Map.Entry<String, String> entry : actionProperties.entrySet()) {
				Log.d(TAG, entry.getKey() + ":" + entry.getValue());
			}
		}
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
		Log.d(TAG, "set name called");
	}
	
	// returns the text of clicked view
    private String getEventText(AccessibilityEvent event) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : event.getText()) {
            sb.append(s);
        }
        return sb.toString();
    }

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private class GmailObserver {
		private static final String INNER_SEP = ",";
		private static final String OUTER_SEP = ";";

		private MailContent content;

		public GmailObserver(AccessibilityEvent event) {
			String eventText = getEventText(event);
			
			if(eventText.equals(InteractionDictionary.ATTACH_FILE_EN) || event.equals(InteractionDictionary.ATTACH_FILE_DE)) {
				Action action = new Action();
				action.setActionType(ActionType.FILE_ATTACHED);
				action.setTimestamp(System.currentTimeMillis());
				
				createUserAction(action, null);
			}

			if(eventText.equals(InteractionDictionary.SEND_EN) || event.equals(InteractionDictionary.SEND_DE)) {
				Log.e(TAG, "SEND");

				// create action
				Action action = new Action();
				action.setActionType(ActionType.SEND_MAIL);
				action.setTimestamp(System.currentTimeMillis());
				
				// create properties
				readMailContent(getRootInActiveWindow(), "");
				readAttachmentsAndSubject(getRootInActiveWindow());
				
				Map<String, String> actionProperties = new HashMap<String, String>();
				actionProperties.put(MailProperties.PROPERTY_KEY_FROM, content.getFrom());
				actionProperties.put(MailProperties.PROPERTY_KEY_TO, content.getTo());
				actionProperties.put(MailProperties.PROPERTY_KEY_CC, content.getCc());
				actionProperties.put(MailProperties.PROPERTY_KEY_BCC, content.getBcc());
				actionProperties.put(MailProperties.PROPERTY_KEY_SUBJECT, content.getSubject());
				actionProperties.put(MailProperties.PROPERTY_KEY_ATTACHMENT_COUNT, String.valueOf(content.getAttachments().size()));
				actionProperties.put(MailProperties.PROPERTY_KEY_ATTACHMENT_INFO, generateMailAttachmentInfo(content.getAttachments()));
				
				createUserAction(action, actionProperties);
				
				content = null;
				content = new MailContent();
			}
		}
		
		private String generateMailAttachmentInfo(List<MailAttachment> attachments) {
			if(content.getAttachments().size() > 0) {
				String attachmentInfos = "";
				for (MailAttachment item : content.getAttachments()) {
					attachmentInfos += item.getFileName() + INNER_SEP + item.getFileType() + INNER_SEP + item.getFileSize() + OUTER_SEP;
				}
				
				// remove last separator and return value
				return attachmentInfos.substring(0, attachmentInfos.length() - 1);
			}
			else {
				return "";
			}
		}
		
		private void readMailContent(AccessibilityNodeInfo source, String generation) {
	        if(source != null) {
	            for(int i = 0; i < source.getChildCount(); i++) {
	                AccessibilityNodeInfo child = source.getChild(i);
	                if(child != null) {
	                    String line = generation + " " + child.getClassName();
	                    if(child.getText() != null) {
	                        line += " = " + child.getText() + "|";
	                    }
	                    if(child.getLabelFor() != null) {
	                        line += " label for= " + child.getLabelFor() + "|";
	                    }
	                    if(child.getContentDescription() != null) {
	                        line += " content description= " + child.getContentDescription() + "|";
	                        if (child.getContentDescription().equals("An") || child.getContentDescription().equals("To") && child.getText() != null) {
	                            content.setTo(child.getText().toString());
	                        }
	                        else if(child.getContentDescription().equals("Cc") && child.getText() != null) {
	                            content.setCc(child.getText().toString());
	                        }
	                        else if(child.getContentDescription().equals("Bcc") && child.getText() != null) {
	                            content.setBcc(child.getText().toString());
	                        }
	                    }
	                    if(child.getClassName().equals("android.widget.Spinner")) {
	                        if(child.getChild(0) != null && child.getChild(0).getClassName().equals("android.widget.TextView")) {
	                            content.setFrom(child.getChild(0).getText().toString());
	                        }
	                    }

	                    Log.d(TAG, line);
	
	                    if(child.getChildCount() > 0) {
	                    	readMailContent(child, generation.concat("\t"));
	                    }
	                }
	            }
	        }
	    }
		
		private void readAttachmentsAndSubject(AccessibilityNodeInfo source) {
	        List<AccessibilityNodeInfo> lLayouts = new ArrayList<AccessibilityNodeInfo>();
	        List<AccessibilityNodeInfo> rLayouts = new ArrayList<AccessibilityNodeInfo>();
	        if(source != null) {
	            for (int i = 0; i < source.getChildCount(); i++) {
	                AccessibilityNodeInfo child = source.getChild(i);
	                if (child != null) {
	                    if (child.getClassName().equals("android.widget.ScrollView")) {
	                        for(int j = 0; j < child.getChildCount(); j++) {
	                            AccessibilityNodeInfo subChild = child.getChild(j);
	                            if(subChild != null && subChild.getClassName().equals("android.widget.LinearLayout")) {
	                                lLayouts.add(subChild);
	                            }if(subChild != null && subChild.getClassName().equals("android.widget.RelativeLayout")) {
	                                rLayouts.add(subChild);
	                            }
	                        }
	                    }
	                }
	            }

	            // Attachment
	            if(lLayouts.size() > 0) {
	                AccessibilityNodeInfo attachmentChild = lLayouts.get(lLayouts.size() - 1);
	                for(int i = 0; i < attachmentChild.getChildCount(); i++) {
	                    AccessibilityNodeInfo subChild;
	                    if((subChild = attachmentChild.getChild(i)) != null) {
	                        try {
	                        	MailAttachment attachment = new MailAttachment();
	                        	String[] splitAttachmentName = subChild.getChild(0).getText().toString().split(".");
	                        	attachment.setFileName(splitAttachmentName[0]);
	                        	attachment.setFileType(splitAttachmentName[1]);
	                        	attachment.setFileSize(subChild.getChild(1).getText().toString());
	                        	
	                        	content.addMailAttachmentItem(attachment);
	                        } catch (NullPointerException e) {}
	                    }
	                }
	            }

	            // Subject
	            if(rLayouts.size() > 0) {
	                AccessibilityNodeInfo subjectChild = rLayouts.get(rLayouts.size() - 2);
	                if (subjectChild.getChild(0) != null) {
	                    try {
	                        Log.d(TAG, "subject: " + subjectChild.getChild(0).getText());
	                        content.setSubject(subjectChild.getChild(0).getText().toString());
	                    } catch (NullPointerException e) {}
	                }
	            }
	        }
		}
	}
}
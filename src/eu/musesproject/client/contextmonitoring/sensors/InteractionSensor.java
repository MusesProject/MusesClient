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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;
import eu.musesproject.client.db.entity.SensorConfiguration;
import eu.musesproject.client.model.contextmonitoring.InteractionDictionary;
import eu.musesproject.client.model.contextmonitoring.MailAttachment;
import eu.musesproject.client.model.contextmonitoring.MailContent;
import eu.musesproject.client.model.contextmonitoring.MailProperties;
import eu.musesproject.client.model.contextmonitoring.UISource;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.contextmodel.ContextEvent;

public class InteractionSensor extends AccessibilityService implements ISensor {
	private static final String TAG = InteractionSensor.class.getSimpleName();
	
	// sensor identifier
	public static final String TYPE = "CONTEXT_SENSOR_INTERACTION";

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
	
	@Override
	public void onCreate() {
		super.onCreate();
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
		String pckName = (String) event.getPackageName();
		if(pckName != null && pckName.equals("com.google.android.gm")) {
			new GmailObserver(getRootInActiveWindow(), event);
		}
	}

	private void createUserAction(Action action, Map<String, String> actionProperties) {
		Log.d(TAG, "FINISH createUserAction: " + action.getActionType());
		if(action.getActionType() == ActionType.FILE_ATTACHED) {
			UserContextMonitoringController.getInstance(this).sendUserAction(UISource.INTERNAL, action, null);
		}
		else if(action.getActionType() == ActionType.SEND_MAIL) {
			UserContextMonitoringController.getInstance(this).sendUserAction(UISource.INTERNAL, action, actionProperties);
			for(Map.Entry<String, String> entry : actionProperties.entrySet()) {
				Log.d(TAG, "mail test : " + entry.getKey() + ":" + entry.getValue());
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
	}
	
	// returns the text of clicked view
    private String getEventText(AccessibilityEvent event) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : event.getText()) {
            sb.append(s);
        }
        return sb.toString();
    }

	private class GmailObserver {
		private static final String INNER_SEP = ",";
		private static final String OUTER_SEP = ";";

		private MailContent content;

		public GmailObserver(AccessibilityNodeInfo accessibilityNodeInfo, AccessibilityEvent event) {
			String eventText = getEventText(event);
			
			if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
				if(eventText.contains(InteractionDictionary.ATTACH_FILE_EN) || eventText.contains(InteractionDictionary.ATTACH_FILE_DE)) {
					Log.d(TAG, "mail test : attach file");
					Action action = new Action();
					action.setActionType(ActionType.FILE_ATTACHED);
					action.setTimestamp(System.currentTimeMillis());
					
					createUserAction(action, null);
				}
	
				if(eventText.contains(InteractionDictionary.SEND_EN) || eventText.contains(InteractionDictionary.SEND_DE)) {
					Log.d(TAG, "mail test : send clicked");
					content = null;
					content = new MailContent();
					
					// create action
					Action action = new Action();
					action.setActionType(ActionType.SEND_MAIL);
					action.setTimestamp(System.currentTimeMillis());
					
					// create properties (for phones)
//                	Log.d(TAG, "readMailContent(accessibilityNodeInfo)");
//					readMailContent(accessibilityNodeInfo, "");
//					Log.d(TAG, "readAttachmentsAndSubject(accessibilityNodeInfo)");
//					readAttachmentsAndSubject(accessibilityNodeInfo);
					
					// for 10" tablet
					readMailContentOfTablet(accessibilityNodeInfo);
					
					// debugging
//					testView(accessibilityNodeInfo, "");
					
					Map<String, String> actionProperties = new HashMap<String, String>();
					actionProperties.put(MailProperties.PROPERTY_KEY_FROM, content.getFrom());
					actionProperties.put(MailProperties.PROPERTY_KEY_TO, content.getTo());
					actionProperties.put(MailProperties.PROPERTY_KEY_CC, content.getCc());
					actionProperties.put(MailProperties.PROPERTY_KEY_BCC, content.getBcc());
					actionProperties.put(MailProperties.PROPERTY_KEY_SUBJECT, content.getSubject());
					actionProperties.put(MailProperties.PROPERTY_KEY_ATTACHMENT_COUNT, String.valueOf(content.getAttachments().size()));
					actionProperties.put(MailProperties.PROPERTY_KEY_ATTACHMENT_INFO, generateMailAttachmentInfo(content.getAttachments()));
					
					createUserAction(action, actionProperties);
				}
			}
		}
		
		private void readMailContentOfTablet(AccessibilityNodeInfo accessibilityNodeInfo) {
			boolean subjectHierarchyLevelReached = false;
	        Log.d(TAG, "getTabletContent");
	        // here we can find: From, to, cc, bcc
	        AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(1);
	        if(!child.getClassName().equals("android.widget.ScrollView")) {
	        	return; // cancel because the current root view does not contain the necessary information
	        }
	        if(child != null && child.getClassName().equals("android.widget.ScrollView")) {
	            Log.d(TAG, "ScrollView");
	            int childCount = child.getChildCount();
	            // from field
	            AccessibilityNodeInfo fromChild = child.getChild(0);
	            content.setFrom(fromChild.getChild(0).getText().toString());
	            // to field
	            AccessibilityNodeInfo toChild = child.getChild(1);
	            content.setTo(toChild.getChild(1).getText().toString());
	
	            if(childCount > 2) { // mcc and bcc
	                // cc, bcc
	                for (int i = 2; i < childCount; i++) {
	                    AccessibilityNodeInfo subChild = child.getChild(i);
	                    if(subChild != null) {
	                        if(subChild.getClassName().equals("android.widget.LinearLayout")) {
	                            AccessibilityNodeInfo subSubChild = subChild.getChild(1);
	                            try {
	                                String contentDescription  = subSubChild.getContentDescription().toString();
	                                String text = subSubChild.getText().toString();
	                                if(contentDescription != null && text != null && contentDescription.equalsIgnoreCase("Cc")) {
	                                    content.setCc(text);
	                                }
	                                else if(contentDescription != null && text != null && contentDescription.equalsIgnoreCase("Bcc")) {
	                                    content.setBcc(text);
	                                }
	                            } catch (NullPointerException e) {}
	                        }
	                        else if(subChild.getClassName().equals("android.widget.RelativeLayout")) { // subject and attachments
	                            AccessibilityNodeInfo subSubChild = subChild.getChild(0);
	                            if(subSubChild != null) {
	                            	if(subSubChild.getClassName() != null && subSubChild.getClassName().equals("android.widget.EditText")) {
	                            		if(!subjectHierarchyLevelReached) { // subject
	                            			try {
	                            				String subject = subSubChild.getText().toString();
	                            				content.setSubject(subject);
	                            				subjectHierarchyLevelReached = true;
	                            			} catch (NullPointerException e) {}
	                            		}
	                            	}
	                            	else if(subSubChild.getClassName() != null && subSubChild.getClassName().equals("android.widget.TextView")) { // attachments
	                            		MailAttachment attachment = new MailAttachment();
	                            		attachment.setFileName(subChild.getChild(0).getText().toString());
	                            		attachment.setFileType(subChild.getChild(0).getText().toString().split("\\.")[1]);
	                            		attachment.setFileSize(subChild.getChild(1).getText().toString());
	                            		
	                            		content.addMailAttachmentItem(attachment);
	                            	}
	                            }
	                        }
	                    }
	                }
	            }
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
		

	    private void testView(AccessibilityNodeInfo source, String generation) {
	        if(source != null) {
	            for(int i = 0; i < source.getChildCount(); i++) {
	                AccessibilityNodeInfo child = source.getChild(i);
	                if(child != null) {
	                    if (child.getClassName().equals("android.widget.TextView") || child.getClassName().equals("android.widget.EditText")) {
	                        Log.d(TAG, generation +  child.getClassName() + " = " + child.getText() + " | label for: " + child.getLabelFor() +" | content description= " + child.getContentDescription());
	                    }
	                    String line = generation + " " + child.getClassName();
	                    if(child.getText() != null) {
	                        line += " = " + child.getText() + "|";
	                    }
	                    if(child.getLabelFor() != null) {
	                        line += " label for= " + child.getLabelFor() + "|";
	                    }
	                    if(child.getContentDescription() != null) {
	                        line += " content description= " + child.getContentDescription() + "|";
	                        if (child.getContentDescription().equals("An") && child.getText() != null) {
	                        }
	                        else if(child.getContentDescription().equals("Cc") && child.getText() != null) {
	                        }
	                        else if(child.getContentDescription().equals("Bcc") && child.getText() != null) {
	                        }
	                    }
	                    if(child.getClassName().equals("android.widget.Spinner")) {
	                        if(child.getChild(0) != null && child.getChild(0).getClassName().equals("android.widget.TextView")) {
	                        }
	                    }

	                    Log.d(TAG, line);

	                    if(child.getChildCount() > 0) {
	                        testView(child, generation.concat("\t->"));
	                    }
	                }
	            }
	        }
	    }
		
		private void readMailContent(AccessibilityNodeInfo source, String generation) {
	        if(source != null) {
	            for(int i = 0; i < source.getChildCount(); i++) {
	                AccessibilityNodeInfo child = source.getChild(i);
	                if(child != null) {
	                    if(child.getContentDescription() != null) {
	                    	Log.d(TAG, "contentDescription : " + child.getContentDescription());
	                        if (child.getContentDescription().equals("An") || child.getContentDescription().equals("To") && child.getText() != null) {
		                    	Log.d(TAG, "to");
	                            content.setTo(child.getText().toString());
	                        }
	                        else if(child.getContentDescription().equals("Cc") && child.getText() != null) {
		                    	Log.d(TAG, "cc");
	                            content.setCc(child.getText().toString());
	                        }
	                        else if(child.getContentDescription().equals("Bcc") && child.getText() != null) {
		                    	Log.d(TAG, "bcc");
	                            content.setBcc(child.getText().toString());
	                        }
	                    }
	                    if(child.getClassName().equals("android.widget.Spinner")) {
	                    	Log.d(TAG, "spinner for from");
	                        if(child.getChild(0) != null && child.getChild(0).getClassName().equals("android.widget.TextView")) {
		                    	Log.d(TAG, "from");
	                            content.setFrom(child.getChild(0).getText().toString());
	                        }
	                    }

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
	            	Log.d(TAG, "possible attachment");
	                AccessibilityNodeInfo attachmentChild = lLayouts.get(lLayouts.size() - 1);
	                for(int i = 0; i < attachmentChild.getChildCount(); i++) {
	                    AccessibilityNodeInfo subChild;
	                    if((subChild = attachmentChild.getChild(i)) != null) {
	                        try {
	                        	MailAttachment attachment = new MailAttachment();
	                        	String[] splitAttachmentName = subChild.getChild(0).getText().toString().split(".");
	                        	if(splitAttachmentName.length >= 2) {
	                        		attachment.setFileName(splitAttachmentName[0]);
	                        		attachment.setFileType(splitAttachmentName[1]);
	                        		attachment.setFileSize(subChild.getChild(1).getText().toString());
	                        		
	                        		content.addMailAttachmentItem(attachment);
	            	            	Log.d(TAG, "attachment");
	                        	}
	                        } catch (NullPointerException e) {}
	                    }
	                }
	            }

	            // Subject
	            if(rLayouts.size() > 0) {
	            	Log.d(TAG, "possible subject");
	                AccessibilityNodeInfo subjectChild = rLayouts.get(rLayouts.size() - 2);
	                if (subjectChild.getChild(0) != null) {
	                    try {
	                        content.setSubject(subjectChild.getChild(0).getText().toString());
	    	            	Log.d(TAG, "subject");
	                    } catch (NullPointerException e) {}
	                }
	            }
	        }
		}
	}

	@Override
	public void configure(List<SensorConfiguration> config) {
		// TODO Auto-generated method stub
		
	}
}
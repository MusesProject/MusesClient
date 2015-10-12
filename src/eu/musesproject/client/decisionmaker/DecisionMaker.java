/*
 * MUSES High-Level Object Oriented Model
 * Copyright MUSES project (European Commission FP7) - 2013 
 */
package eu.musesproject.client.decisionmaker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.util.Log;
import eu.musesproject.client.contextmonitoring.sensors.ConnectivitySensor;
import eu.musesproject.client.contextmonitoring.sensors.PackageSensor;
import eu.musesproject.client.db.entity.Action;
import eu.musesproject.client.db.entity.DecisionTable;
import eu.musesproject.client.db.entity.Resource;
import eu.musesproject.client.db.entity.RiskCommunication;
import eu.musesproject.client.db.entity.RiskTreatment;
import eu.musesproject.client.db.handler.DBManager;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.client.model.decisiontable.Decision;
import eu.musesproject.client.model.decisiontable.Request;
import eu.musesproject.client.ui.DebugFileLog;
import eu.musesproject.client.usercontexteventhandler.UserContextEventHandler;
import eu.musesproject.contextmodel.ContextEvent;


/**
 * The Class LocalPolicySelector.
 * 
 * @author Sergio Zamarripa (S2)
 * @version 26 sep 2013
 */
public class DecisionMaker {

    private static final String TAG = DecisionMaker.class.getSimpleName();
    private static final String APP_TAG = "APP_TAG";
    
    /**
	 * Info DC
	 * 
	 *  Method to notify the decision maker about an incoming request
	 * 
	 * @param request
	 * 
	 * 
	 * @return 
	 */
	
	public void notifyActionRequest(Request request){

	
	}	
	private String getConditionType(String condition) {
		if ((condition.contains("installedApps"))||(condition.contains("resource"))){
			return "event";
		}else{
			return "property";
		}
	}
	public Decision manageDecision(Request request, List<ContextEvent> eventList, Map<String, String> properties){
		Log.d(TAG, "called: manageDecision(Request request, List<ContextEvent> eventList)");
		DebugFileLog.write("DecisionMaker- called: manageDecision(Request request, List<ContextEvent> eventList)");
		Decision resultDecision = null;
		Map<String,String> conditions = new HashMap<String,String>();
		Map<String,String> eventProperties = new HashMap<String,String>();
		String condition = null;
		eu.musesproject.client.db.entity.Decision entityDecision = null;
		eu.musesproject.client.db.entity.DecisionTable dt = null;
		eu.musesproject.client.db.entity.RiskCommunication comm = null;
		eu.musesproject.client.db.entity.RiskTreatment treatment = null;
		boolean match = false;
		
		DBManager dbManager = new DBManager(UserContextEventHandler.getInstance().getContext());
        dbManager.openDB();
        
        //List<Resource> list = dbManager.getAllResourcesWithCondition();
        List<eu.musesproject.client.db.entity.Decision> list = dbManager.getAllDecisionsWithCondition();
        Log.d(TAG+"SZL","conditions:"+list.size());
        DebugFileLog.write("DecisionMaker - conditions:"+list.size());
        //get all conditions for all current device policy decisions (only for elements that are meant to appear in the eventList, not properties)
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
        	eu.musesproject.client.db.entity.Decision decision = (eu.musesproject.client.db.entity.Decision) iterator.next();
        	condition = decision.getCondition();
        	Log.d(TAG+"SZL","	condition:"+condition);
        	DebugFileLog.write("DecisionMaker- 	condition:"+condition);
        	if  (getConditionType(condition).equals("event")){
        		//conditions.put(decision.getCondition(), getConditionType(condition));
        		conditions.put(decision.getCondition(), String.valueOf(decision.getId()));
        	}			
		}
        
        // Now, check if any ContextEvent in the eventList satisfies such conditions
        
        for (Map.Entry<String, String> entry : conditions.entrySet())
        {
            Log.d(TAG+"SZL","1. Decision condition to be checked: "+entry.getKey() + "/" + entry.getValue());
            DebugFileLog.write("DecisionMaker-1. Decision condition to be checked: "+entry.getKey() + "/" + entry.getValue());
            Log.d(TAG+"SZL","Event List size:"+eventList.size());
            DebugFileLog.write("DecisionMaker-Event List size:"+eventList.size());
           	//Iterate over eventList
            for (Iterator iterator = eventList.iterator(); iterator.hasNext();) {
				ContextEvent contextEvent = (ContextEvent) iterator.next();
				//Get properties of such contextEvent
				eventProperties= contextEvent.getProperties();
				//Iterate over the event properties to check if the condition is in place
				for (Map.Entry<String, String> propEntry : eventProperties.entrySet()){
					String propKey = propEntry.getKey();
					 Log.d(TAG+"SZL","2. Property event to be checked: "+propEntry.getKey() + "/" + propEntry.getValue());
				 		DebugFileLog.write("DecisionMaker-2. Property event to be checked: "+propEntry.getKey() + "/" + propEntry.getValue());
					 if (entry.getKey().toLowerCase().contains(propKey.toLowerCase())){
						 String value = entry.getKey()
									.substring(
											entry.getKey()
													.indexOf(":") + 2,
													entry.getKey()
													.length() - 2);
						 Log.d(TAG+"SZL","2.1 Value: "+value);
						 DebugFileLog.write("DecisionMaker-2.1 Value: "+value);
						 if ((propKey.contains("installedapps"))&&(!propEntry.getValue().contains(value))){
							Log.d(TAG+"SZL","3.installedapps  Match!");
							DebugFileLog.write("DecisionMaker-3.installedapps  Match!");
							match = true;
						 }else if ((propKey.contains("resourceName"))&&(propEntry.getValue().contains(value))){
								Log.d(TAG+"SZL","3. resourcename Match!");
								DebugFileLog.write("DecisionMaker-3.resourcename Match!");
								match = true;
						 }else if (propEntry.getValue().contains(value)){
							 Log.d(TAG+"SZL","3.Match!");
							 DebugFileLog.write("DecisionMaker-3.Match!");
							 match = false;
						 }
					 }
				}
			}
            if (match){
            	entityDecision = dbManager.getDecisionFromID(entry.getValue());
				dt = dbManager.getDecisionTableFromDecisionId(String.valueOf(entityDecision.getId()));
				comm= dbManager.getRiskCommunicationFromID(String.valueOf(dt.getRiskcommunication_id()));
		        	if (comm != null){
		        		treatment = dbManager.getRiskTreatmentFromID(String.valueOf(comm.getRisktreatment_id()));
		        	}
		        	resultDecision = composeDecision(entityDecision, comm, treatment);
				 return resultDecision;
            }else{
            	return null;
            }
        }
 
		
		return resultDecision;
		
	}


	/**
	 * Info DC
	 * 
	 *  Method to process the decision regarding a request
	 * 
	 * @param request
	 * 
	 * 
	 * @return 
	 */
	
	public Decision makeDecision(Request request, List<ContextEvent> eventList, Map<String, String> properties){
		
		Decision resultDecision = new Decision();
		try {
		
		boolean match = false;
		String matchedCondition = "";
		Decision priorDecision = manageDecision(request, eventList, properties);
		if (priorDecision != null){
			Logger.getLogger(TAG).log(Level.WARNING, "Policy Device Decision: " + priorDecision.getName());
			DebugFileLog.write("DecisionMaker-Policy Device Decision: " + priorDecision.getName());
			return priorDecision;
		}
		
		Log.d(APP_TAG, "DecisionMaker=> Making decision with request and events");
        Log.d(TAG, "called: makeDecision(Request request, List<ContextEvent> eventList)");
        DebugFileLog.write("DecisionMaker: makeDecision(Request request, List<ContextEvent> eventList)");
        String resourceCondition = null;

        eu.musesproject.client.db.entity.Decision decision = new eu.musesproject.client.db.entity.Decision();
        eu.musesproject.client.db.entity.RiskCommunication comm = new eu.musesproject.client.db.entity.RiskCommunication();
        eu.musesproject.client.db.entity.RiskTreatment treatment = new eu.musesproject.client.db.entity.RiskTreatment();
        Resource resourceInPolicy = null;
        Action actionInPolicy = new Action();
        RiskCommunication riskCommInPolicy = new RiskCommunication();
        RiskTreatment riskTreatInPolicy = new RiskTreatment();
        
        DecisionTable decisionTable = null;
        
        Log.d(TAG, "Action type:"+request.getAction().getActionType());
        DebugFileLog.write("DecisionMaker-Action type:"+request.getAction().getActionType());
        Log.d(TAG, "Action description:"+request.getAction().getDescription());
        DebugFileLog.write("DecisionMaker-Action description:"+request.getAction().getDescription());
        Log.d(TAG, "Action id:"+request.getAction().getId());
        DebugFileLog.write("DecisionMaker-Action id:"+request.getAction().getId());
        Log.d(TAG, "Action timestamp:"+request.getAction().getTimestamp());
        DebugFileLog.write("DecisionMaker-Action timestamp:"+request.getAction().getTimestamp());
        Log.d(TAG, "Resource description:"+request.getResource().getDescription());
        DebugFileLog.write("DecisionMaker-Resource description:"+request.getResource().getDescription());
        
        //TODO Remove this tweak when the action and resources are not null:        
        /*if (request.getAction().getActionType()==null){
        	//request.getAction().setActionType("open_asset");
        	request.getAction().setActionType("open_application");
        }

        if (request.getResource().getDescription()==null){
        	request.getResource().setPath("Gmail");
        }*/
        /*if (request.getResource().getPath()==null){
    	request.getResource().setPath("/sdcard/Swe/MUSES_partner_grades.txt");
    	}*/
        //End of Tweak
        
        for (Iterator iterator = eventList.iterator(); iterator.hasNext();) {
			ContextEvent contextEvent = (ContextEvent) iterator.next();
			Log.d(TAG, "Event list:"+contextEvent.getType());
			DebugFileLog.write("DecisionMaker-Event list:"+contextEvent.getType());
		}
        
        Log.d(TAG, "Resource:"+request.getResource());
        DebugFileLog.write("DecisionMaker-Resource:"+request.getResource().getName());
        Log.d(TAG, "Resource path:"+request.getResource().getPath());
        DebugFileLog.write("DecisionMaker-Resource path:"+request.getResource().getPath());
        
        DBManager dbManager = new DBManager(UserContextEventHandler.getInstance().getContext());
        dbManager.openDB();
        
        
        if ((request.getAction()!=null)&&(request.getResource()!=null)){
        	/*SZLif (request.getResource().getPath()!=null){
        		Log.d(TAG, "Looking for resource by path" );
        		resourceInPolicy = dbManager.getResourceFromPath(request.getResource().getPath());
        	}else{SZL*/
        		Log.d(TAG, "Find resource by condition properties..." );
        		DebugFileLog.write("DecisionMaker-Find resource by condition properties..." );
        		List<Resource> allConditionResources = dbManager.getAllResources();
        		Log.d(TAG, "Found..."+allConditionResources.size());
        		DebugFileLog.write("DecisionMaker-Found..."+allConditionResources.size());
        		
        		for (Iterator iterator = allConditionResources.iterator(); iterator
						.hasNext();) {
					Resource resource = (Resource) iterator.next();
					if (match){
						Log.d(TAG, "One condition matched, then break.");
						DebugFileLog.write("DecisionMaker-One condition matched, then break from conditions iterator.");
						break;
					}else{
						Log.d(TAG, "No match for previous condition");
						DebugFileLog.write("DecisionMaker-No match for previous condition");
					}
					Log.d(TAG, "Id:"+resource.getId());
					DebugFileLog.write("DecisionMaker-Id:"+resource.getId());
					if (resource.getCondition()!=null){
						Log.d(TAG, "Condition:"+resource.getCondition());
						DebugFileLog.write("DecisionMaker-Condition:"+resource.getCondition());
						Log.d(TAG, "Resource properties:");
						DebugFileLog.write("DecisionMaker-Resource properties:");
		        		for (Map.Entry<String, String> entry : properties.entrySet())
		                {        			
		        			String comparisonString = null;
		        			if (entry.getKey().contains("path")||entry.getKey().contains("resource")||entry.getKey().contains("packagename")||entry.getKey().contains("appname")){
		        				comparisonString = "{\""+entry.getKey()+"\":\""+entry.getValue()+"\"}";
		        			}else{
		        				comparisonString = "{\""+entry.getKey()+"\":"+entry.getValue()+"}";
		        			}
		        			
		                    Log.d(TAG, "	"+comparisonString);
		                    DebugFileLog.write("DecisionMaker-	"+comparisonString);
		                    
		                    if(resource.getCondition().contains("\\/")){
		                    	resourceCondition = resource.getCondition().replace("\\/","/");
		                    }else{
		                    	resourceCondition = resource.getCondition();
		                    }
		                    if (resourceCondition != null){
		                    	Log.d(TAG, "	1:"+resourceCondition.toLowerCase()+"-- 2:"+comparisonString.toLowerCase()+"--");
		                    	DebugFileLog.write("DecisionMaker-	1:"+resourceCondition.toLowerCase()+"-- 2:"+comparisonString.toLowerCase()+"--");
		                    }
		                    //if (resource.getCondition().toLowerCase().equals(comparisonString.toLowerCase())){
		                    if (resourceCondition.toLowerCase().equals(comparisonString.toLowerCase())){
		                    	 Log.d(TAG, "	Match!");
		                    	 DebugFileLog.write("DecisionMaker-	Match!");
		                    	resourceInPolicy = resource;//No break, since the last one should have priority over older ones
		                    	matchedCondition = resource.getCondition();
		                    	match=true;
		                    	break;
							} else {
								Log.d(TAG, "	No Match!" + comparisonString);
								DebugFileLog.write("DecisionMaker-	No Match!" + comparisonString);

								//
								try{
								//if (resource.getCondition().contains(":")) {
									if (resourceCondition.contains(":")) {
									String property = resourceCondition
											.substring(
													0,
													resourceCondition
															.indexOf(":") - 1);
									Log.d(TAG, "property:" + property);
									DebugFileLog.write("DecisionMaker-property:" + property);
									if (property.contains(entry.getKey())) {
										int intValue = -1;
										String value = resource
												.getCondition()
												.substring(
														resourceCondition
																.indexOf(":") + 1,
														resourceCondition
																.length() - 1);
										Log.d(TAG, "value:" + value);
										DebugFileLog.write("DecisionMaker-value:" + value);
										try {
											intValue = Integer.valueOf(value);
										} catch (NumberFormatException e) {
											Log.d(TAG, "value " + value
													+ " is not a number");
											DebugFileLog.write("DecisionMaker-value " + value
													+ " is not a number");
										}

										if (intValue != -1) {
											int currentValue = -1;
											Log.d(TAG,
													"Current value:"
															+ entry.getValue());
											DebugFileLog.write("DecisionMaker-Current value:"
															+ entry.getValue());
											try {
												currentValue = Integer
														.valueOf(entry
																.getValue());
											} catch (NumberFormatException e) {
												Log.d(TAG, "current value "
														+ entry.getValue()
														+ " is not a number");
												DebugFileLog.write("DecisionMaker-current value "
														+ entry.getValue()
														+ " is not a number");
											}
											if (currentValue != -1) {
												if (currentValue < intValue) {
													Log.d(TAG, "Current value "
															+ currentValue
															+ " is less than "
															+ intValue);
													DebugFileLog.write("DecisionMaker-Current value "
															+ currentValue
															+ " is less than "
															+ intValue);
													Log.d(TAG, "Allow");
													DebugFileLog.write("DecisionMaker-Allow");
													dbManager.closeDB();
													return getConditionNotSatisfiedDecision();
												} else {
													Log.d(TAG,
															"Current value "
																	+ currentValue
																	+ " is greater or equal than "
																	+ intValue);
													DebugFileLog.write("DecisionMaker-Current value "
																	+ currentValue
																	+ " is greater or equal than "
																	+ intValue);
												}
											}
										}

									}
								}
								}catch (Exception e){
									Log.d(TAG, e.getMessage());
								}	
							}
		                    
		                    //Connectivity condition
		                    
 		                    if ((resourceCondition.contains("wifi"))||(resourceCondition.contains("bluetooth"))){
		                    	DebugFileLog.write("DecisionMaker-contains is true ");
		                    	for (Iterator iterator1 = eventList.iterator(); iterator1.hasNext();) {
		                			ContextEvent contextEvent = (ContextEvent) iterator1.next();
		                			Log.d(TAG, "Event list:"+contextEvent.getType());
		                			DebugFileLog.write("DecisionMaker-Event list:"+contextEvent.getType());
		                			if (contextEvent.getType().equals(ConnectivitySensor.TYPE)){
		                				Log.d(TAG, "resourcecondition:"+resourceCondition);
		                				DebugFileLog.write("DecisionMaker-resourcecondition:"+resourceCondition);
		                				for (Map.Entry<String, String> connEntry : contextEvent.getProperties().entrySet())
		        		                { 
		                					String currentProperty = "{\""+connEntry.getKey()+"\":\""+connEntry.getValue()+"\"}";
		                					if (connEntry.getKey().contains("bluetooth")){
		                						currentProperty = "{\""+connEntry.getKey()+"\":"+connEntry.getValue()+"}";
		                					}
		                					Log.d(TAG,"WIFI     "+currentProperty);
		                					DebugFileLog.write("DecisionMaker-WIFI     "+currentProperty);
		                					
		                					if (resourceCondition.toLowerCase().equals(currentProperty.toLowerCase())){		                						
		                						Log.d(TAG, "	Environment Match!");
		                						DebugFileLog.write("DecisionMaker-Environment Match!");
		                						
		                						if (request.getResource().getPath()!=null){
		                			        		Log.d(TAG, "Request path:" + request.getResource().getPath() );
		                			        		DebugFileLog.write("DecisionMaker-Request path:" + request.getResource().getPath() );
		                			        		Log.d(TAG, "Resource:" + resource.getPath() );
		                			        		DebugFileLog.write("DecisionMaker-Resource:" + resource.getPath() );
		                			        		if (resource.getPath().equals(request.getResource().getPath())){
		                			        			Log.d(TAG, "	Path Match!");
		                			        			DebugFileLog.write("DecisionMaker-Path Match!");
		                			        			resourceInPolicy = resource;
		                			        			matchedCondition = resource.getCondition();
		                			        			match=true;
				                						break;
		                			        		}else{
		                			        			Log.d(TAG, "	No Path Match!");
		                			        			DebugFileLog.write("DecisionMaker-No Path Match!");
		                			        		}
		                			        		
		                						}else{
		                							Log.d(TAG, "	Path for resource is null!");
		                							DebugFileLog.write("DecisionMaker-Path for resource is null!");
		                						}
		                						
		                					} else {
		                						Log.d(TAG, "	No EnvironmentMatch!" + currentProperty);
		                						DebugFileLog.write("DecisionMaker-No EnvironmentMatch!" + currentProperty);
		                					}
		        		                }
		                				
		                				
		                				
		                			}
		                		}
		                    }else if (resourceCondition.contains("package")){
		                    	for (Iterator iterator1 = eventList.iterator(); iterator1.hasNext();) {
		                			ContextEvent contextEvent = (ContextEvent) iterator1.next();
		                			Log.d(TAG, "Event list:"+contextEvent.getType());
		                			if (contextEvent.getType().equals(PackageSensor.TYPE)){
		                				Log.d(TAG, "resourcecondition:"+resourceCondition);
		                				
		                				for (Map.Entry<String, String> connEntry : contextEvent.getProperties().entrySet())
		        		                { 
		                					String currentProperty = "{\""+connEntry.getKey()+"\":\""+connEntry.getValue()+"\"}";
		                					Log.d(TAG,"PACKAGE     "+currentProperty);
		                					
		                					if (resourceCondition.toLowerCase().equals(currentProperty.toLowerCase())){		                						
		                						Log.d(TAG, "	Package Match!");
		                						
		                						resourceInPolicy = resource;
		                						Log.d(TAG, " resourceInPolicy:"+ resourceInPolicy.getPath());
		                						matchedCondition = resource.getCondition();
		                						match=true;
		                						break;
		                						
		                					} else {
		                						Log.d(TAG, "	No EnvironmentMatch!" + currentProperty);
		                					}
		        		                }
		                				
		                				
		                				
		                			}
		                		}
		                    }
		                    
		                }
		        		
					}else{
						Log.d(TAG, "Condition null");
					}
				}
        		
        		
        	//SZL}
        	
        	if (resourceInPolicy == null){

        		Log.d(TAG, "Looking for resource by description" );
        		resourceInPolicy = dbManager.getResourceFromPath(request.getResource().getDescription());
        		if ((resourceInPolicy == null)||(resourceInPolicy.getPath()==null)||(resourceInPolicy.getId()==0)){
        			//return getDefaultDecision();
        			dbManager.closeDB();
        			return null;
        		}
        	}else{
        		Log.d(TAG, "resourceInPolicy not null" );
        	}
        	
        	actionInPolicy = dbManager.getActionFromType(request.getAction().getActionType());        	
        	Log.d(TAG, "Resource in table:" + resourceInPolicy.getPath() + " Id:" +  resourceInPolicy.getId());
        	DebugFileLog.write("DecisionMaker-Resource in table:" + resourceInPolicy.getPath() + " Id:" +  resourceInPolicy.getId());
        	Log.d(TAG, "Action in table:" + actionInPolicy.getDescription() + " Id:" +  actionInPolicy.getId());
        	DebugFileLog.write("DecisionMaker-Action in table:" + actionInPolicy.getDescription() + " Id:" +  actionInPolicy.getId());
        	decisionTable = dbManager.getDecisionTableFromResourceId(String.valueOf(resourceInPolicy.getId()),String.valueOf(actionInPolicy.getId()));
        	Log.d(TAG, "DT in table: Id:" +  decisionTable.getId());
        	DebugFileLog.write("DecisionMaker-DT in table: Id:" +  decisionTable.getId());
        	if (decisionTable.getId()==0){
        		// Find decision with such condition
        		Log.d(TAG, "Find all decisions with conditions:");
            	DebugFileLog.write("DecisionMaker-Find all decisions with conditions:");
            	eu.musesproject.client.db.entity.Decision decisionInDB = null;
        		List<eu.musesproject.client.db.entity.Decision> decisionsWithCondition = dbManager.getAllDecisions();
        		for (Iterator iterator = decisionsWithCondition.iterator(); iterator
						.hasNext();) {
        			eu.musesproject.client.db.entity.Decision decision2 = (eu.musesproject.client.db.entity.Decision) iterator.next();
						if (decision2.getCondition() != null) {
							Log.d(TAG,
									"matchedCondition:" + matchedCondition
											+ "- current decision cond:"
											+ decision2.getCondition() + "-");
							DebugFileLog
									.write("matchedCondition:" + matchedCondition
											+ "- current decision cond:"
											+ decision2.getCondition() + "-");
							if (matchedCondition.equals(decision2.getCondition())) {
								Log.d(TAG,
										"setting decision with id:"+decision2.getId());
								DebugFileLog
										.write("DecisionMaker-setting decision id:"+decision2.getId());
								decisionInDB = decision2;
							}
						}else{
							Log.d(TAG, "null condition");
							DebugFileLog
									.write("DecisionMaker-null condition");
						}
					
				}
        		if (decisionInDB != null){
        			Log.d(TAG, "Found decision with id :" + decisionInDB.getId());
                	DebugFileLog.write("DecisionMaker-Found decision with id :" + decisionInDB.getId());
            		decisionTable = dbManager.getDecisionTableFromDecisionId(String.valueOf(decisionInDB.getId()));
        		}else{   		
        			dbManager.closeDB();
        			return null;
        		}	
        	}
        	Log.d(TAG, "Retrieving riskCommunication associated to id:" +  String.valueOf(decisionTable.getRiskcommunication_id()));
        	DebugFileLog.write("DecisionMaker-Retrieving riskCommunication associated to id:" +  String.valueOf(decisionTable.getRiskcommunication_id()));
        	riskCommInPolicy = dbManager.getRiskCommunicationFromID(String.valueOf(decisionTable.getRiskcommunication_id()));
        	Log.d(TAG, "RiskComm in table: Id:" +  riskCommInPolicy.getId());
        	DebugFileLog.write("DecisionMaker-RiskComm in table: Id:" +  riskCommInPolicy.getId());
        	if (riskCommInPolicy != null){
        		Log.d(TAG, "Retrieving riskTreatment associated to id:" +  String.valueOf(riskCommInPolicy.getRisktreatment_id()));
        		DebugFileLog.write("DecisionMaker-Retrieving riskTreatment associated to id:" +  String.valueOf(riskCommInPolicy.getRisktreatment_id()));
        		riskTreatInPolicy = dbManager.getRiskTreatmentFromID(String.valueOf(riskCommInPolicy.getRisktreatment_id()));
        		Log.d(TAG, "RiskTreat in table:" + riskTreatInPolicy.getTextualdescription() + " Id:" +  riskTreatInPolicy.getId());
        		DebugFileLog.write("DecisionMaker-RiskTreat in table:" + riskTreatInPolicy.getTextualdescription() + " Id:" +  riskTreatInPolicy.getId());
        		
        	}
        }
        
        if (decisionTable != null){
        	decision = dbManager.getDecisionFromID(String.valueOf(decisionTable.getDecision_id()));
        	
        	if (decision!=null){
        	String condition = decision.getCondition();
    		//if ((decision.getName()!=null)&&(decision.getName().equals("deny"))){
        	if ((decision.getName()!=null)&&(decision.getName().equals("maybe"))){
    			if ((condition!=null)&&(!condition.equals("any"))){
        			if (condition.contains("wifiencryption")){// TODO This should be managed by a ConditionHelper, to be implemented
        				for (Iterator iterator = eventList.iterator(); iterator.hasNext();) {
							ContextEvent contextEvent = (ContextEvent) iterator.next();
							if (contextEvent.getType().equals(ConnectivitySensor.TYPE)){
								if (contextEvent.getProperties()!=null){
									Map<String,String> map = contextEvent.getProperties();
									for(Map.Entry<String, String> entry : map.entrySet()){
										if (entry.getKey().contains("wifiencryption")){
											Logger.getLogger(TAG).log(Level.WARNING, "Condition with wifiencryption");
											condition = condition.substring(("wifiencryption").length());
											Logger.getLogger(TAG).log(Level.WARNING, "" + condition);
											if (condition.startsWith("!=")){
												String comparisonValue = condition.substring(2);
												Log.d(TAG, "comparisonValue:"+comparisonValue);
												if (!entry.getValue().contains(comparisonValue)){
													//Deny
													Logger.getLogger(TAG).log(Level.WARNING, "Condition satisfied: MUSES should say maybe, explaining the risk treatment");
													resultDecision.setName(Decision.MAYBE_ACCESS_WITH_RISKTREATMENTS);
													//resultDecision.setName(Decision.STRONG_DENY_ACCESS);
													eu.musesproject.server.risktrust.RiskTreatment [] riskTreatments = new eu.musesproject.server.risktrust.RiskTreatment[1];												
													eu.musesproject.server.risktrust.RiskTreatment riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(riskTreatInPolicy.getTextualdescription());
													eu.musesproject.server.risktrust.RiskCommunication riskCommunication = new eu.musesproject.server.risktrust.RiskCommunication();
													riskTreatments[0] = riskTreatment;	
													riskCommunication.setRiskTreatment(riskTreatments);
													resultDecision.setRiskCommunication(riskCommunication); 
													dbManager.closeDB();
													return resultDecision;
												}else{
													//Allow
													Logger.getLogger(TAG).log(Level.WARNING, "Condition not satisfied: "+comparisonValue+".MUSES should allow");
													resultDecision.setName(Decision.GRANTED_ACCESS);
													dbManager.closeDB();
													return resultDecision;
												}
											}
										}
									}
									
								}
							}
						}
        			}
        		}
        	}else if ((decision.getName()!=null)&&(decision.getName().equals("allow"))){
        		if ((condition!=null)&&(!condition.equals("any"))){
        			Log.d(TAG, "Allow decision with a concrete condition");
        		}else{
        			Log.d(TAG, "Allow decision with any condition");
					resultDecision.setName(Decision.GRANTED_ACCESS);
        			

        			eu.musesproject.server.risktrust.RiskTreatment [] riskTreatments = new eu.musesproject.server.risktrust.RiskTreatment[1];				
					eu.musesproject.server.risktrust.RiskTreatment riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(riskTreatInPolicy.getTextualdescription());
					eu.musesproject.server.risktrust.RiskCommunication riskCommunication = new eu.musesproject.server.risktrust.RiskCommunication();
					riskTreatments[0] = riskTreatment;
					Log.d(TAG, "RiskTreatment inserted for feedback:"+ riskTreatment.getTextualDescription());
					riskCommunication.setRiskTreatment(riskTreatments);
					resultDecision.setRiskCommunication(riskCommunication); 

        		
					
					eu.musesproject.server.risktrust.RiskTreatment[] r = resultDecision.getRiskCommunication().getRiskTreatment();// TODO Remove: Simple log
					Log.d(TAG, "RiskTreat for feedback:"+ resultDecision.getRiskCommunication().getRiskTreatment());
					if (r[0].getTextualDescription() != null) {
						String textualDecp = r[0].getTextualDescription();
						Log.d(TAG, "RiskTreatment:"+textualDecp);
					}else{
						Log.d(TAG, "RiskTreatment textualDescription null. Array length:"+r.length);
					}
					
					dbManager.closeDB();
					return resultDecision;
        		}
    		}else if ((decision.getName()!=null)&&(decision.getName().equals("deny"))){
        		if ((condition!=null)&&(!condition.equals("any"))){
        			Log.d(TAG, "Deny with condition");
        		}else{
        			Log.d(TAG, "Deny");
					
        			
        			//resultDecision.setName(Decision.MAYBE_ACCESS_WITH_RISKTREATMENTS);
        			resultDecision.setName(Decision.STRONG_DENY_ACCESS);
        			eu.musesproject.server.risktrust.RiskTreatment [] riskTreatments = new eu.musesproject.server.risktrust.RiskTreatment[1];				
					eu.musesproject.server.risktrust.RiskTreatment riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(riskTreatInPolicy.getTextualdescription());
					eu.musesproject.server.risktrust.RiskCommunication riskCommunication = new eu.musesproject.server.risktrust.RiskCommunication();
					riskTreatments[0] = riskTreatment;
					Log.d(TAG, "RiskTreatment inserted for feedback:"+ riskTreatment.getTextualDescription());
					riskCommunication.setRiskTreatment(riskTreatments);
					resultDecision.setRiskCommunication(riskCommunication); 

        		
					
					eu.musesproject.server.risktrust.RiskTreatment[] r = resultDecision.getRiskCommunication().getRiskTreatment();// TODO Remove: Simple log
					Log.d(TAG, "RiskTreat for feedback:"+ resultDecision.getRiskCommunication().getRiskTreatment());
					if (r[0].getTextualDescription() != null) {
						String textualDecp = r[0].getTextualDescription();
						Log.d(TAG, "RiskTreatment:"+textualDecp);
					}else{
						Log.d(TAG, "RiskTreatment textualDescription null. Array length:"+r.length);
					}
					
					dbManager.closeDB();
					return resultDecision;
        		}
    		}else if ((decision.getName()!=null)&&(decision.getName().equals("up-to-you"))){
        		if ((condition!=null)&&(!condition.equals("any"))){
        			Log.d(TAG, "Up to user with condition");
        		}else{
        			Log.d(TAG, "Up to user");
					
        			
        			//resultDecision.setName(Decision.MAYBE_ACCESS_WITH_RISKTREATMENTS);
        			resultDecision.setName(Decision.UPTOYOU_ACCESS_WITH_RISKCOMMUNICATION);
        			eu.musesproject.server.risktrust.RiskTreatment [] riskTreatments = new eu.musesproject.server.risktrust.RiskTreatment[1];				
					eu.musesproject.server.risktrust.RiskTreatment riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(riskTreatInPolicy.getTextualdescription());
					eu.musesproject.server.risktrust.RiskCommunication riskCommunication = new eu.musesproject.server.risktrust.RiskCommunication();
					riskTreatments[0] = riskTreatment;
					Log.d(TAG, "RiskTreatment inserted for feedback:"+ riskTreatment.getTextualDescription());
					riskCommunication.setRiskTreatment(riskTreatments);
					resultDecision.setRiskCommunication(riskCommunication); 

        		
					
					eu.musesproject.server.risktrust.RiskTreatment[] r = resultDecision.getRiskCommunication().getRiskTreatment();// TODO Remove: Simple log
					Log.d(TAG, "RiskTreat for feedback:"+ resultDecision.getRiskCommunication().getRiskTreatment());
					if (r[0].getTextualDescription() != null) {
						String textualDecp = r[0].getTextualDescription();
						Log.d(TAG, "RiskTreatment:"+textualDecp);
					}else{
						Log.d(TAG, "RiskTreatment textualDescription null. Array length:"+r.length);
					}
					
					dbManager.closeDB();
					return resultDecision;
        		}
    		}
        }

        	comm= dbManager.getRiskCommunicationFromID(String.valueOf(decisionTable.getRiskcommunication_id()));
        	if (comm != null){
        		treatment = dbManager.getRiskTreatmentFromID(String.valueOf(comm.getRisktreatment_id()));
        	}
        	resultDecision = composeDecision(decision, comm, treatment);
        }else{
        	Log.d(TAG,"Decision table is null");
        	dbManager.closeDB();
        	return null;
        	//return getDefaultDecision();
        }
        

        
        dbManager.closeDB();
		}catch (Throwable t){
			DebugFileLog.write(t.getMessage());
			t.printStackTrace();
		}
        
		return resultDecision;

	}	
	
	private Decision composeDecision(
			eu.musesproject.client.db.entity.Decision decision,
			eu.musesproject.client.db.entity.RiskCommunication comm,
			eu.musesproject.client.db.entity.RiskTreatment treatment) {
		
		Decision resultDecision = new Decision();
		eu.musesproject.server.risktrust.RiskCommunication riskCommunication = new eu.musesproject.server.risktrust.RiskCommunication();
		eu.musesproject.server.risktrust.RiskTreatment riskTreatment = null;
		eu.musesproject.server.risktrust.RiskTreatment[] arrayTreatment = null;
		
		if (decision != null){
			if (decision.getDecision_id()!=null){
				resultDecision.setDecision_id(decision.getDecision_id());
				Logger.getLogger(TAG).log(Level.INFO, "Server decision id set to:"+decision.getDecision_id());
			}
			resultDecision.setSolving_risktreatment(decision.getSolving_risktreatment());
			Logger.getLogger(TAG).log(Level.INFO, "Server solving risk treatment set to:"+decision.getSolving_risktreatment());
			if (decision.getName() != null){
				if (decision.getName().equals("deny")){
					resultDecision.setName(Decision.STRONG_DENY_ACCESS);
				}else if (decision.getName().equals("maybe")){
					resultDecision.setName(Decision.MAYBE_ACCESS_WITH_RISKTREATMENTS);
				}else if (decision.getName().equals("allow")){
					resultDecision.setName(Decision.GRANTED_ACCESS);
				}else if (decision.getName().equals("up-to-you")){
					resultDecision.setName(Decision.UPTOYOU_ACCESS_WITH_RISKCOMMUNICATION);
				}
				
				Logger.getLogger(TAG).log(Level.WARNING, "Policy Device Decision: " + decision.getName());
			}else{
				Logger.getLogger(TAG).log(Level.WARNING, "No decision is found. Hence, MUSES sets default decision");
				resultDecision.setName(Decision.STRONG_DENY_ACCESS);//Default decision is deny
			}
		}else {
			Logger.getLogger(TAG).log(Level.WARNING, "No decision is found. Hence, MUSES sets default decision");
			resultDecision.setName(Decision.STRONG_DENY_ACCESS);//Default decision is deny
		}
		riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(treatment.getTextualdescription());
		arrayTreatment = new eu.musesproject.server.risktrust.RiskTreatment[]{riskTreatment};
		riskCommunication.setRiskTreatment(arrayTreatment);
		resultDecision.setRiskCommunication(riskCommunication);
		Logger.getLogger(TAG).log(Level.WARNING, "Result decision: " + resultDecision.getName());
		Logger.getLogger(TAG).log(Level.WARNING, "Risk treatment: " + treatment.getTextualdescription());
		
		return resultDecision;
	}

	/**
	 * Info DC
	 * 
	 *  Method to push the decision associated to a request, including RiskTreatment and RiskCommunication
	 * 
	 * @param request
	 * 
	 * 
	 * @return Decision
	 */
	
	public Decision pushDecisionToEventHandler(Request request){
		
		return null;

	}
	
	public Decision makeDummyDecision(Request request, List<ContextEvent> eventList){
		
		Decision decision = new Decision();

		eu.musesproject.server.risktrust.RiskCommunication riskCommunication = new eu.musesproject.server.risktrust.RiskCommunication();
		eu.musesproject.server.risktrust.RiskTreatment riskTreatment = null;
		eu.musesproject.server.risktrust.RiskTreatment[] arrayTreatment = new eu.musesproject.server.risktrust.RiskTreatment[]{riskTreatment};
		
	
		if(request.getAction() != null) {
		    if (request.getAction().getActionType().equals(ActionType.ACCESS)){
		        decision.setName(Decision.GRANTED_ACCESS);
		        riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment("No additional treatment is needed");
		    }else if (request.getAction().getActionType().equals(ActionType.OPEN)){
		        decision.setName(Decision.MAYBE_ACCESS_WITH_RISKTREATMENTS);
		        riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment("Requested action will be allowed with the user connects to an encrypted connection");
		    }else if (request.getAction().getActionType().equals(ActionType.RUN)){
		        decision.setName(Decision.STRONG_DENY_ACCESS);
		        riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment("Requested action is not allowed, no matter the settings");//TODO: Us
		    }else if (request.getAction().getActionType().equals(ActionType.INSTALL)){
		        decision.setName(Decision.UPTOYOU_ACCESS_WITH_RISKCOMMUNICATION);
		        riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment("This action is potentially unsecure.You might continue with the action under your own risk");
		    } else {
		    	decision.setName(Decision.STRONG_DENY_ACCESS);
		    	riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment("Requested action is not allowed, no matter the settings");
		    }
		}
		
		riskCommunication.setRiskTreatment(arrayTreatment);
		decision.setRiskCommunication(riskCommunication);
		return decision;
	}
	
	public Decision getDefaultDecision() {

		Log.d(TAG,"Returning default decision...");
		Decision defaultDecision = new Decision();
		defaultDecision.setName(Decision.DEFAULT_DENY_ACCESS);
		eu.musesproject.server.risktrust.RiskCommunication riskCommunication = new eu.musesproject.server.risktrust.RiskCommunication();
		eu.musesproject.server.risktrust.RiskTreatment riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(
				"Decision denied by default (since no concrete local device policies apply)");
		eu.musesproject.server.risktrust.RiskTreatment[] arrayTreatment = new eu.musesproject.server.risktrust.RiskTreatment[] { riskTreatment };
		riskCommunication.setRiskTreatment(arrayTreatment);
		defaultDecision.setRiskCommunication(riskCommunication);
		return defaultDecision;
	}
	
	public Decision getDefaultDecision(eu.musesproject.client.model.decisiontable.Action action) {

		Log.d(TAG,"Returning default decision based on action ...");
		Decision defaultDecision = new Decision();
		eu.musesproject.server.risktrust.RiskTreatment riskTreatment = null;
		eu.musesproject.server.risktrust.RiskCommunication riskCommunication = new eu.musesproject.server.risktrust.RiskCommunication();
		//TODO These default decisions should be pre-loaded when the client is connected to the server, and will be accessed from the local database
		if (action.getActionType().equals(ActionType.ACCESS)){
			defaultDecision.setName(Decision.DEFAULT_DENY_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"Decision denied by default, according to action: " + action.getActionType());
		}else if (action.getActionType().equals(ActionType.CANCEL)){
			defaultDecision.setName(Decision.GRANTED_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"You are allowed to go on, under your own responsibility" + action.getActionType());
		}else if (action.getActionType().equals(ActionType.DELETE)){
			defaultDecision.setName(Decision.DEFAULT_DENY_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"Decision denied by default, according to action: " + action.getActionType());
		}else if (action.getActionType().equals(ActionType.ENCRYPT_EVENT)){
			defaultDecision.setName(Decision.GRANTED_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"You are allowed to go on, under your own responsibility" + action.getActionType());
		}else if (action.getActionType().equals(ActionType.FILE_ATTACHED)){
			defaultDecision.setName(Decision.GRANTED_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"You are allowed to go on, under your own responsibility" + action.getActionType());
		}else if (action.getActionType().equals(ActionType.INSTALL)){
			defaultDecision.setName(Decision.DEFAULT_DENY_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"Decision denied by default, according to action: " + action.getActionType());
		}else if (action.getActionType().equals(ActionType.OK)){
			defaultDecision.setName(Decision.GRANTED_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"You are allowed to go on, under your own responsibility" + action.getActionType());
		}else if (action.getActionType().equals(ActionType.OPEN)){
			defaultDecision.setName(Decision.GRANTED_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"You are allowed to go on, under your own responsibility" + action.getActionType());
		}else if (action.getActionType().equals(ActionType.OPEN_APPLICATION)){
			defaultDecision.setName(Decision.GRANTED_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"You are allowed to go on, under your own responsibility" + action.getActionType());
		}else if (action.getActionType().equals(ActionType.OPEN_ASSET)){
			defaultDecision.setName(Decision.GRANTED_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"You are allowed to go on, under your own responsibility" + action.getActionType());
		}else if (action.getActionType().equals(ActionType.SAVE_ASSET)){
			defaultDecision.setName(Decision.GRANTED_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"You are allowed to go on, under your own responsibility" + action.getActionType());
		}else if (action.getActionType().equals(ActionType.SECURITY_PROPERTY_CHANGED)){
			defaultDecision.setName(Decision.DEFAULT_DENY_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"Decision denied by default, according to action: " + action.getActionType());
		}else if (action.getActionType().equals(ActionType.SEND)){
			defaultDecision.setName(Decision.DEFAULT_DENY_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"Decision denied by default, according to action: " + action.getActionType());
		}else if (action.getActionType().equals(ActionType.SEND_MAIL)){
			defaultDecision.setName(Decision.GRANTED_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"You are allowed to go on, under your own responsibility" + action.getActionType());
		}else if (action.getActionType().equals(ActionType.UNINSTALL)){
			defaultDecision.setName(Decision.DEFAULT_DENY_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"Decision denied by default, according to action: " + action.getActionType());
		}else if (action.getActionType().equals(ActionType.UPDATE)){
			defaultDecision.setName(Decision.DEFAULT_DENY_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"Decision denied by default, according to action: " + action.getActionType());
		}else if (action.getActionType().equals(ActionType.VIRUS_FOUND)){
			defaultDecision.setName(Decision.DEFAULT_DENY_ACCESS);
			riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(	"Decision denied by default, according to action: " + action.getActionType());
		}
		
		eu.musesproject.server.risktrust.RiskTreatment[] arrayTreatment = new eu.musesproject.server.risktrust.RiskTreatment[] { riskTreatment };
		riskCommunication.setRiskTreatment(arrayTreatment);
		defaultDecision.setRiskCommunication(riskCommunication);
		return defaultDecision;

	}
	
	public Decision getConditionNotSatisfiedDecision() {

		Log.d(TAG,"Returning allow decision due to condition not satisfied...");
		Decision defaultDecision = new Decision();
		defaultDecision.setName(Decision.GRANTED_ACCESS);
		eu.musesproject.server.risktrust.RiskCommunication riskCommunication = new eu.musesproject.server.risktrust.RiskCommunication();
		eu.musesproject.server.risktrust.RiskTreatment riskTreatment = new eu.musesproject.server.risktrust.RiskTreatment(
				"Decision allowed");
		eu.musesproject.server.risktrust.RiskTreatment[] arrayTreatment = new eu.musesproject.server.risktrust.RiskTreatment[] { riskTreatment };
		riskCommunication.setRiskTreatment(arrayTreatment);
		defaultDecision.setRiskCommunication(riskCommunication);
		return defaultDecision;
	}
	
	public Decision getDefaultDecision(
			eu.musesproject.client.model.decisiontable.Action action,
			Map<String, String> actionProperties,
			List<ContextEvent> contextEvents) {

		return getDefaultDecision(action);
	}
}
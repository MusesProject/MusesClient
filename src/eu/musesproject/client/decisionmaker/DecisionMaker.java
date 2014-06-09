/*
 * MUSES High-Level Object Oriented Model
 * Copyright MUSES project (European Commission FP7) - 2013 
 */
package eu.musesproject.client.decisionmaker;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.util.Log;
import eu.musesproject.client.contextmonitoring.sensors.ConnectivitySensor;
import eu.musesproject.client.db.entity.Action;
import eu.musesproject.client.db.entity.DecisionTable;
import eu.musesproject.client.db.entity.Resource;
import eu.musesproject.client.db.entity.RiskCommunication;
import eu.musesproject.client.db.entity.RiskTreatment;
import eu.musesproject.client.db.handler.DBManager;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.client.model.decisiontable.Decision;
import eu.musesproject.client.model.decisiontable.Request;
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
	
	public Decision makeDecision(Request request, List<ContextEvent> eventList){
        Log.d(TAG, "called: makeDecision(Request request, List<ContextEvent> eventList)");

        eu.musesproject.client.db.entity.Decision decision = new eu.musesproject.client.db.entity.Decision();
        eu.musesproject.client.db.entity.RiskCommunication comm = new eu.musesproject.client.db.entity.RiskCommunication();
        eu.musesproject.client.db.entity.RiskTreatment treatment = new eu.musesproject.client.db.entity.RiskTreatment();
        Resource resourceInPolicy = new Resource();
        Action actionInPolicy = new Action();
        RiskCommunication riskCommInPolicy = new RiskCommunication();
        RiskTreatment riskTreatInPolicy = new RiskTreatment();
        Decision resultDecision = new Decision();
        DecisionTable decisionTable = null;
        
        Log.d(TAG, "Action type:"+request.getAction().getActionType());
        Log.d(TAG, "Action description:"+request.getAction().getDescription());
        Log.d(TAG, "Action id:"+request.getAction().getId());
        Log.d(TAG, "Action timestamp:"+request.getAction().getTimestamp());
        
        Log.d(TAG, "Resource description:"+request.getResource().getDescription());
        
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
		}
        
        Log.d(TAG, "Resource:"+request.getResource());
        Log.d(TAG, "Resource path:"+request.getResource().getPath());
        
        DBManager dbManager = new DBManager(UserContextEventHandler.getInstance().getContext());
        dbManager.openDB();
        
        
        if ((request.getAction()!=null)&&(request.getResource()!=null)){
        	if (request.getResource().getPath()!=null){
        		resourceInPolicy = dbManager.getResourceFromPath(request.getResource().getPath());
        	}else{
        		resourceInPolicy = dbManager.getResourceFromPath(request.getResource().getDescription());
        	}
        	
        	actionInPolicy = dbManager.getActionFromType(request.getAction().getActionType());        	
        	Log.d(TAG, "Resource in table:" + resourceInPolicy.getPath() + " Id:" +  resourceInPolicy.getId());
        	Log.d(TAG, "Action in table:" + actionInPolicy.getDescription() + " Id:" +  actionInPolicy.getId());
        	decisionTable = dbManager.getDecisionTableFromResourceId(String.valueOf(resourceInPolicy.getId()),String.valueOf(actionInPolicy.getId()));
        	Log.d(TAG, "DT in table: Id:" +  decisionTable.getId());
        	if (decisionTable.getId()==0){
        		return null;
        	}
        	Log.d(TAG, "Retrieving riskCommunication associated to id:" +  String.valueOf(decisionTable.getRiskcommunication_id()));
        	riskCommInPolicy = dbManager.getRiskCommunicationFromID(String.valueOf(decisionTable.getRiskcommunication_id()));
        	Log.d(TAG, "RiskComm in table: Id:" +  riskCommInPolicy.getId());
        	if (riskCommInPolicy != null){
        		Log.d(TAG, "Retrieving riskTreatment associated to id:" +  String.valueOf(riskCommInPolicy.getRisktreatment_id()));
        		riskTreatInPolicy = dbManager.getRiskTreatmentFromID(String.valueOf(riskCommInPolicy.getRisktreatment_id()));
        		Log.d(TAG, "RiskTreat in table:" + riskTreatInPolicy.getTextualdescription() + " Id:" +  riskTreatInPolicy.getId());
        		
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
													return resultDecision;
												}else{
													//Allow
													Logger.getLogger(TAG).log(Level.WARNING, "Condition not satisfied: "+comparisonValue+".MUSES should allow");
													resultDecision.setName(Decision.GRANTED_ACCESS);
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
					
					
					return resultDecision;
        		}
    		}
        }

        	comm= dbManager.getRiskCommunicationFromID(String.valueOf(decisionTable.getRiskcommunication_id()));
        	if (comm != null){
        		treatment = dbManager.getRiskTreatmentFromID(String.valueOf(comm.getRisktreatment_id()));
        	}
        	resultDecision = composeDecision(decision, comm, treatment);
        }
        
        dbManager.closeDB();
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
			if (decision.getName() != null){
				resultDecision.setName(decision.getName());
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

}

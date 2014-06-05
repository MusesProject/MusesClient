/*
 * MUSES High-Level Object Oriented Model
 * Copyright MUSES project (European Commission FP7) - 2013 
 */
package eu.musesproject.client.decisionmaker;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.util.Log;
import eu.musesproject.client.db.entity.Action;
import eu.musesproject.client.db.entity.DecisionTable;
import eu.musesproject.client.db.entity.Resource;
import eu.musesproject.client.db.handler.DBManager;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.client.model.decisiontable.Decision;
import eu.musesproject.client.model.decisiontable.Request;
import eu.musesproject.client.usercontexteventhandler.UserContextEventHandler;
import eu.musesproject.contextmodel.ContextEvent;
import eu.musesproject.server.risktrust.RiskCommunication;
import eu.musesproject.server.risktrust.RiskTreatment;

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
        Decision resultDecision = new Decision();
        DecisionTable decisionTable = null;
        
        Log.d(TAG, "Action:"+request.getAction().getActionType());
        Log.d(TAG, "Action:"+request.getAction().getDescription());
        Log.d(TAG, "Action:"+request.getAction().getId());
        Log.d(TAG, "Action:"+request.getAction().getTimestamp());
        
        for (Iterator iterator = eventList.iterator(); iterator.hasNext();) {
			ContextEvent contextEvent = (ContextEvent) iterator.next();
			Log.d(TAG, "Event list:"+contextEvent.getType());
		}
        
        Log.d(TAG, "Resource:"+request.getResource());
        
        DBManager dbManager = new DBManager(UserContextEventHandler.getInstance().getContext());
        dbManager.openDB();
        
        
        //if ((request.getAction()!=null)&&(request.getResource()!=null)){
        if (request.getAction()!=null){
        	resourceInPolicy = dbManager.getResourceFromPath("null");
        	actionInPolicy = dbManager.getActionFromType(request.getAction().getActionType());
        	Log.d(TAG, "Resource in table:" + resourceInPolicy.getPath() + " Id:" +  resourceInPolicy.getId());
        	Log.d(TAG, "Action in table:" + actionInPolicy.getDescription() + " Id:" +  actionInPolicy.getId());
        	//decisionTable = dbManager.getDecisionTableFromActionAndResource(String.valueOf(request.getAction().getId()), String.valueOf(request.getResource().getId()));
        	decisionTable = dbManager.getDecisionTableFromActionAndResource(String.valueOf(actionInPolicy.getId()), String.valueOf(resourceInPolicy.getId()));
        	Log.d(TAG, "DT in table: Id:" +  decisionTable.getId());
        }
        
        if (decisionTable != null){
        	decision = dbManager.getDecisionFromID(String.valueOf(decisionTable.getDecision_id()));
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
		RiskCommunication riskCommunication = new RiskCommunication();
		RiskTreatment riskTreatment = null;
		RiskTreatment[] arrayTreatment = null;
		
		if (decision != null){
			if (decision.getName() != null){
				resultDecision.setName(decision.getName());
			}else{
				Logger.getLogger(TAG).log(Level.WARNING, "No decision is found. Hence, MUSES sets default decision");
				resultDecision.setName(Decision.STRONG_DENY_ACCESS);//Default decision is deny
			}
		}else {
			Logger.getLogger(TAG).log(Level.WARNING, "No decision is found. Hence, MUSES sets default decision");
			resultDecision.setName(Decision.STRONG_DENY_ACCESS);//Default decision is deny
		}
		riskTreatment = new RiskTreatment(treatment.getTextualdescription());
		arrayTreatment = new RiskTreatment[]{riskTreatment};
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

		RiskCommunication riskCommunication = new RiskCommunication();
		RiskTreatment riskTreatment = null;
		RiskTreatment[] arrayTreatment = new RiskTreatment[]{riskTreatment};
		
	
		if(request.getAction() != null) {
		    if (request.getAction().getActionType().equals(ActionType.ACCESS)){
		        decision.setName(Decision.GRANTED_ACCESS);
		        riskTreatment = new RiskTreatment("No additional treatment is needed");
		    }else if (request.getAction().getActionType().equals(ActionType.OPEN)){
		        decision.setName(Decision.MAYBE_ACCESS_WITH_RISKTREATMENTS);
		        riskTreatment = new RiskTreatment("Requested action will be allowed with the user connects to an encrypted connection");
		    }else if (request.getAction().getActionType().equals(ActionType.RUN)){
		        decision.setName(Decision.STRONG_DENY_ACCESS);
		        riskTreatment = new RiskTreatment("Requested action is not allowed, no matter the settings");//TODO: Us
		    }else if (request.getAction().getActionType().equals(ActionType.INSTALL)){
		        decision.setName(Decision.UPTOYOU_ACCESS_WITH_RISKCOMMUNICATION);
		        riskTreatment = new RiskTreatment("This action is potentially unsecure.You might continue with the action under your own risk");
		    } else {
		    	decision.setName(Decision.STRONG_DENY_ACCESS);
		    	riskTreatment = new RiskTreatment("Requested action is not allowed, no matter the settings");
		    }
		}
		
		riskCommunication.setRiskTreatment(arrayTreatment);
		decision.setRiskCommunication(riskCommunication);
		return decision;
	}	

}

/*
 * MUSES High-Level Object Oriented Model
 * Copyright MUSES project (European Commission FP7) - 2013 
 */
package eu.musesproject.client.decisionmaker;

import java.util.List;

import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.Decision;
import eu.musesproject.client.model.decisiontable.Request;
import eu.musesproject.contextmodel.ContextEvent;

/**
 * The Class LocalPolicySelector.
 * 
 * @author Sergio Zamarripa (S2)
 * @version 26 sep 2013
 */
public class DecisionMaker {
	
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
		
		Decision decision = new Decision();
		
		if (request.getAction().getDescription().equals(Action.ACCESS)){
			decision.setName(Decision.GRANTED_ACCESS);			
		}else if (request.getAction().getDescription().equals(Action.OPEN)){
			decision.setName(Decision.MAYBE_ACCESS_WITH_RISKTREATMENTS);			
		}else if (request.getAction().getDescription().equals(Action.RUN)){
			decision.setName(Decision.STRONG_DENY_ACCESS);			
		}else if (request.getAction().getDescription().equals(Action.INSTALL)){
			decision.setName(Decision.UPTOYOU_ACCESS_WITH_RISKCOMMUNICATION);			
		}
				
		return decision;

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

}

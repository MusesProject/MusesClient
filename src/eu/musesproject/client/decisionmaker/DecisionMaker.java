/*
 * MUSES High-Level Object Oriented Model
 * Copyright MUSES project (European Commission FP7) - 2013 
 */
package eu.musesproject.client.decisionmaker;

import java.util.List;

import android.util.Log;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.client.model.decisiontable.Decision;
import eu.musesproject.client.model.decisiontable.Request;
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

        Decision decision = new Decision();

        RiskCommunication riskCommunication = new RiskCommunication();
        RiskTreatment riskTreatment = null;
        RiskTreatment[] arrayTreatment = new RiskTreatment[]{riskTreatment};
        
        if(request.getAction() != null) {
            if (request.getAction().getActionType().equals(ActionType.ACCESS)){
                decision.setName(Decision.GRANTED_ACCESS);
                riskTreatment = new RiskTreatment("No additional treatment is needed");//TODO: Use riskTreatments stored in the local database                                
            }else if (request.getAction().getActionType().equals(ActionType.OPEN)){
                decision.setName(Decision.MAYBE_ACCESS_WITH_RISKTREATMENTS);
                riskTreatment = new RiskTreatment("Requested action will be allowed with the user connects to an encrypted Wi-Fi");//TODO: Use riskTreatments stored in the local database                
            }else if (request.getAction().getActionType().equals(ActionType.RUN)){
                decision.setName(Decision.STRONG_DENY_ACCESS);
                riskTreatment = new RiskTreatment("Requested action is not allowed, no matter the settings");//TODO: Use riskTreatments stored in the local database
            }else if (request.getAction().getActionType().equals(ActionType.INSTALL)){
                decision.setName(Decision.UPTOYOU_ACCESS_WITH_RISKCOMMUNICATION);
                riskTreatment = new RiskTreatment("This action is potentially unsecure.You might continue with the action, under your own risk.");//TODO: Use riskTreatments stored in the local database
            } else {
            	decision.setName(Decision.STRONG_DENY_ACCESS);
                riskTreatment = new RiskTreatment("Requested action is not allowed, no matter the settings");//TODO: Use riskTreatments stored in the local database
            }
        }
        else {
        	decision.setName(Decision.STRONG_DENY_ACCESS);
            riskTreatment = new RiskTreatment("Requested action is not allowed, no matter the settings");//TODO: Use riskTreatments stored in the local database
        }
		
        riskCommunication.setRiskTreatment(arrayTreatment);
        decision.setRiskCommunication(riskCommunication);
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

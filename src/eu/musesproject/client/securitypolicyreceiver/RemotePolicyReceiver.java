/*
 * MUSES High-Level Object Oriented Model
 * Copyright MUSES project (European Commission FP7) - 2013 
 */
package eu.musesproject.client.securitypolicyreceiver;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import eu.musesproject.client.db.entity.DecisionTable;
import eu.musesproject.client.db.handler.DBManager;
import eu.musesproject.client.model.JSONIdentifiers;
import eu.musesproject.client.model.decisiontable.PolicyDT;

/**
 * The Class RemotePolicyReceiver.
 * 
 * @author Sergio Zamarripa (S2)
 * @version 2 avr. 2013
 */
public class RemotePolicyReceiver {
	
	private static RemotePolicyReceiver remotePolicyReceiver = null;	
	private static final String TAG = RemotePolicyReceiver.class.getSimpleName();
	public static final int SUCCESSFUL_RECEPTION = 0;
	public static final int FAILED_RECEPTION = -1;
	
	public static RemotePolicyReceiver getInstance() {
        if (remotePolicyReceiver == null) {
        	remotePolicyReceiver = new RemotePolicyReceiver();
        }
        return remotePolicyReceiver;
    }
	
	/**
	 * Info D
	 * 
	 *  This method allows the reception of policy DT (coming from the server side)
	 * 
	 * @param policyDT
	 * 
	 * 
	 * @return int as status of the reception
	 */
	
	public int receivePolicyDT( PolicyDT policy){
		
		return 0;

	}
	
	public int updateJSONPolicy(String jsonPolicy, Context context){
		Log.d(TAG, "[receiveJSONPolicy]");
		String policy = null;
		DecisionTable decisionTableElement = null;
		
		
		PolicyDT policyDT = new PolicyDT();
		policyDT.setRawPolicy(jsonPolicy);
				
        if((jsonPolicy != null) && (!jsonPolicy.equals(""))) {
    		try{		
    			JSONObject rootJSON = new JSONObject(jsonPolicy);		
    			policy = rootJSON.getString(JSONIdentifiers.DEVICE_POLICY);
    			
    			JSONObject policyJSON = new JSONObject(policy);
    			
    			//Create decision table entry containing action, resource, subject and decision
    			String files = policyJSON.getString(JSONIdentifiers.POLICY_SECTION_FILES);    			
    			JSONObject filesJSON = new JSONObject(files);
    			decisionTableElement = DevicePolicyHelper.getInstance().getDecisionTable(filesJSON, context);
    			
    		} catch (JSONException je) {
    	           je.printStackTrace();
    	           Log.d(TAG, "[receiveJSONPolicy]: Exception while parsing JSON Policy:" + je.getMessage());
    	           return FAILED_RECEPTION;
    	    }
        }

        
        if (decisionTableElement != null){
            
            if (decisionTableElement.getId()>0){
            	Log.d(TAG, "[receiveJSONPolicy]: Decision table element has been correctly added:"+decisionTableElement.getId());
            	Log.d(TAG, "[receiveJSONPolicy]: Action_id:"+decisionTableElement.getAction_id()+"-Resource:"+decisionTableElement.getResource_id()+"-Decision:"+decisionTableElement.getDecision_id()+"-RiskComm:"+decisionTableElement.getRiskcommunication_id());
            	
        		return SUCCESSFUL_RECEPTION;
            }else{
            	Log.d(TAG, "[receiveJSONPolicy]: Decision table element id is negative:" + decisionTableElement.getId());
            	return FAILED_RECEPTION;
            }
        }else{
        	Log.d(TAG, "[receiveJSONPolicy]: Decision table element not found");
        	return FAILED_RECEPTION;
        }
		
	}
	
		


}

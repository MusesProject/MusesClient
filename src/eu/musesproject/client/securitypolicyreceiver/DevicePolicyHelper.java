package eu.musesproject.client.securitypolicyreceiver;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import eu.musesproject.client.db.entity.Action;
import eu.musesproject.client.db.entity.Decision;
import eu.musesproject.client.db.entity.DecisionTable;
import eu.musesproject.client.db.entity.Resource;
import eu.musesproject.client.db.entity.ResourceType;
import eu.musesproject.client.db.entity.RiskCommunication;
import eu.musesproject.client.db.entity.RiskTreatment;
import eu.musesproject.client.db.entity.Role;
import eu.musesproject.client.db.entity.Subject;
import eu.musesproject.client.db.handler.DBManager;
import eu.musesproject.client.model.JSONIdentifiers;

public class DevicePolicyHelper {
	
	private static DevicePolicyHelper devicePolicyHelper = null;
	private static final String TAG = DevicePolicyHelper.class.getSimpleName();
	private static int decisionId;
	
	public static DevicePolicyHelper getInstance() {
        if (devicePolicyHelper == null) {
        	devicePolicyHelper = new DevicePolicyHelper();
        }
        return devicePolicyHelper;
    }
	
	
	
	public DecisionTable getDecisionTable(JSONObject filesJSON, Context context) {// Create decision table entry containing
		DecisionTable decisionTable = new DecisionTable();
		Action action = new Action();
		Resource resource = new Resource();
		Subject subject = new Subject();
		RiskCommunication riskCommunication = new RiskCommunication();
		DBManager dbManager = new DBManager(context);
        dbManager.openDB();
		try{
			//Action part
			String actionString = filesJSON.getString(JSONIdentifiers.POLICY_SECTION_ACTION);
			JSONObject actionJSON = new JSONObject(actionString);
			action = updateAction(actionJSON, context);
			//Resource part
			//String resources = filesJSON.getString(JSONIdentifiers.POLICY_SECTION_RESOURCE);
			String resources = filesJSON.getString(JSONIdentifiers.POLICY_SECTION_ACTION);
			JSONObject resourcesJSON = new JSONObject(resources);
			resource = updateResourceAction(resourcesJSON, context);
			//Subject part
			/*String subjectString = filesJSON.getString(JSONIdentifiers.POLICY_SECTION_SUBJECT);
			JSONObject subjectJSON = new JSONObject(subjectString);
			subject = updateSubject(subjectJSON, context);	
			//RiskCommunication part
			String communicationString = filesJSON.getString(JSONIdentifiers.POLICY_SECTION_RISKCOMMUNICATION);
			JSONObject commJSON = new JSONObject(communicationString);
			riskCommunication = updateRiskCommunication(commJSON, context);*/

		} catch (JSONException je) {
	           je.printStackTrace();
	    }
		if (action!=null){
			decisionTable.setAction_id(action.getId());
		}
		if (resource!=null){
			decisionTable.setResource_id(resource.getId());
		}
		if (subject!=null){
			decisionTable.setSubject_id(subject.getId());
		}
		if (riskCommunication!=null){
			decisionTable.setRiskcommunication_id(riskCommunication.getId());
		}
		
		decisionTable.setDecision_id(decisionId);
		
		
		
		//At the end, with all the inserted ids, update the decision table
		dbManager.addDecisionTable(decisionTable);
		dbManager.closeDB();
		
		return decisionTable;
	}
	
	private RiskCommunication updateRiskCommunication(JSONObject commJSON, Context context) {
		
		RiskCommunication riskCommunication = new RiskCommunication();
		RiskTreatment riskTreatment = new RiskTreatment();
		DBManager dbManager = new DBManager(context);
        dbManager.openDB();
		try {
			String treatmentComm = commJSON.getString(JSONIdentifiers.POLICY_SECTION_RISKTREATMENT);
			JSONObject treatmentJSON = new JSONObject(treatmentComm);
			String descTreatment = treatmentJSON.getString("textualdescription");// TODO Include in JSONIdentifiers
			riskTreatment.setTextualdescription(descTreatment);
			
			//Database insertion: Check if treatment exists. If not, insert it and use its id for resource	        
	        long indexTreatment = dbManager.addRiskTreatment(riskTreatment);

	        riskCommunication.setRisktreatment_id((int)indexTreatment);
			String seqComm = commJSON.getString("communication_sequence");// TODO Include in JSONIdentifiers
			riskCommunication.setCommunication_sequence(Integer.valueOf(seqComm));
			Log.d(TAG, "Risk Communication info:" + seqComm + "-" + descTreatment);
		} catch (JSONException je) {
			je.printStackTrace();
		}
		
		//Insert riskCommunication in db, if it does not exist		
	     long index = dbManager.addRiskCommunication(riskCommunication);
	     dbManager.closeDB();
		
		riskCommunication.setId((int)index);
		return riskCommunication;
	}



	private Subject updateSubject(JSONObject subjectJSON, Context context) {
		Subject subject = new Subject();
		Role role = new Role();
		DBManager dbManager = new DBManager(context);
	    dbManager.openDB();
		try {
			String roleSubject = subjectJSON.getString(JSONIdentifiers.POLICY_SECTION_ROLE);
			JSONObject roleJSON = new JSONObject(roleSubject);
			String descRole = roleJSON.getString("description");// TODO Include in JSONIdentifiers
			role.setDescription(descRole);
			
			//TODO Check if role exists. If not, insert it and use its id for subject
		    long indexRole = dbManager.addRole(role);
		    			
		    subject.setRoleID((int)indexRole);
			String descSubject = subjectJSON.getString("description");// TODO Include in JSONIdentifiers
			subject.setDescription(descSubject);
			java.util.Date now = new java.util.Date();
			subject.setTimestamp(String.valueOf(now.getTime()));
			Log.d(TAG, "Subject info:"+descSubject+"-"+descRole);
		} catch (JSONException je) {
			je.printStackTrace();
		}

		//Insert or update subject in db

	    long indexSubject = dbManager.addSubject(subject);
	    dbManager.closeDB();
		subject.setId((int)indexSubject);
	    
		return subject;
	}



	public Action updateAction(JSONObject actionJSON, Context context){
		Action action = new Action();
		Decision decision = new Decision();
		DBManager dbManager = new DBManager(context);
	    dbManager.openDB();
		try {
			if (actionJSON.toString().contains(JSONIdentifiers.POLICY_PROPERTY_ALLOW)){
				String allowAction = actionJSON.getString(JSONIdentifiers.POLICY_PROPERTY_ALLOW);
				JSONObject allowActionJSON = new JSONObject(allowAction);
				String idResourceAllowed = allowActionJSON.getString("id");//TODO Include in JSONIdentifiers
				Log.d(TAG, "Allowed:" + idResourceAllowed);
				decision.setName(JSONIdentifiers.POLICY_PROPERTY_ALLOW);
				String typeAction = actionJSON.getString(JSONIdentifiers.POLICY_PROPERTY_TYPE);
				action.setDescription(typeAction);
				Log.d(TAG, "Action type:" + typeAction);
			}else {
				String denyAction = actionJSON.getString(JSONIdentifiers.POLICY_PROPERTY_DENY);
				JSONObject denyActionJSON = new JSONObject(denyAction);
				String idResourceAllowed = denyActionJSON.getString("id");//TODO Include in JSONIdentifiers
				Log.d(TAG, "Denied:" + idResourceAllowed);
				String typeAction = actionJSON.getString(JSONIdentifiers.POLICY_PROPERTY_TYPE);
				action.setDescription(typeAction);
				decision.setName(JSONIdentifiers.POLICY_PROPERTY_DENY);
				Log.d(TAG, "Action type:" + typeAction);
				if (denyAction.contains(JSONIdentifiers.POLICY_CONDITION)){
					String conditionAction = denyActionJSON.getString(JSONIdentifiers.POLICY_CONDITION);
					decision.setCondition(conditionAction);
					Log.d(TAG, "Decision condition:" + conditionAction);
				}	
			}
		} catch (JSONException je) {
			je.printStackTrace();
		}
		
		//Insert action in db, if it does not exist
		long indexAction = dbManager.addAction(action);
		Log.d(TAG, "Action index:"+ indexAction);
	    
		action.setId((int)indexAction);
		//TODO Insert decision in db with the same description, if it does not exist
		long indexDecision = dbManager.addDecision(decision);
		decisionId = (int)indexDecision;
		Log.d(TAG, "Decision index:"+ indexDecision);
		dbManager.closeDB();
		
		return action;
	}
	
	public Resource updateResource(JSONObject resourceJSON, Context context){
		Resource resource = new Resource();
		ResourceType resourceType = new ResourceType();
		DBManager dbManager = new DBManager(context);
	    dbManager.openDB();
		try {
			String typeResource = resourceJSON.getString(JSONIdentifiers.POLICY_PROPERTY_RESOURCETYPE);
			//TODO Check if resourcetype exists
			resourceType.setName(typeResource);
			long indexResourceType = dbManager.addResourceType(resourceType);
		    
			resource.setResourcetype((int)indexResourceType);
			
			String idResource = resourceJSON.getString("id");//TODO Include in JSONIdentifiers
			String descResource = resourceJSON.getString(JSONIdentifiers.POLICY_PROPERTY_DESCRIPTION);
			String pathResource = resourceJSON.getString(JSONIdentifiers.POLICY_PROPERTY_PATH);
			Log.d(TAG, "Resource info:"+idResource+"-"+descResource+"-"+pathResource+"-"+typeResource);

		} catch (JSONException je) {
			je.printStackTrace();
		}
		
		//TODO Insert resource in db, if it does not exist
		long indexResource = dbManager.addResource(resource);
		dbManager.closeDB();
		Log.d(TAG, "Resource index:"+ indexResource);
		resource.setId((int)indexResource);

		return resource;
	}
	
	public Resource updateResourceAction(JSONObject actionJSON, Context context){
		Resource resource = new Resource();
		DBManager dbManager = new DBManager(context);
	    dbManager.openDB();
		try {
			if (actionJSON.toString().contains(JSONIdentifiers.POLICY_PROPERTY_ALLOW)){
				String allowAction = actionJSON.getString(JSONIdentifiers.POLICY_PROPERTY_ALLOW);
				JSONObject allowActionJSON = new JSONObject(allowAction);
				String idResourceAllowed = allowActionJSON.getString("path");//TODO Include in JSONIdentifiers
				Log.d(TAG, "Allowed:" + idResourceAllowed);
				resource.setPath(idResourceAllowed);
			}else {
				String denyAction = actionJSON.getString(JSONIdentifiers.POLICY_PROPERTY_DENY);
				JSONObject denyActionJSON = new JSONObject(denyAction);
				String idResourceDenied = denyActionJSON.getString("path");//TODO Include in JSONIdentifiers
				Log.d(TAG, "Denied:" + idResourceDenied);
				resource.setPath(idResourceDenied);
				resource.setDescription(idResourceDenied);
			}
		} catch (JSONException je) {
			je.printStackTrace();
		}
		
		//Insert resource in db, if it does not exist
		long indexResource = dbManager.addResource(resource);
	    dbManager.closeDB();
		resource.setId((int)indexResource);
		Log.d(TAG, "Resource index:"+ indexResource);
		//TODO Insert decision in db with the same description, if it does not exist
		
		
		return resource;
	}

}

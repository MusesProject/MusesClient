/*
 * MUSES High-Level Object Oriented Model
 * Copyright MUSES project (European Commission FP7) - 2013 
 */
package eu.musesproject.client.db.decisiontable;

import eu.musesproject.client.model.decisiontable.DecisionEntry;
import eu.musesproject.client.model.decisiontable.DecisionTable;



/**
 * The Class DecisionTableData.
 * 
 * @author Sergio Zamarripa (S2)
 * @version 26 sep 2013
 */
public class DecisionTableData {

	/**
	 * Info D
	 * 
	 * This method updates the decision table in the local database.
	 * 
	 * @param decisionTable
	 * 
	 * 
	 * @return int as status of the reception
	 */

	public int updateDecisionTable(DecisionTable decisionTable) {
		return 0;

	}

	/**
	 * Info D
	 * 
	 * This method retrieves current decision entries associated to a user
	 * request
	 * 
	 * @param request
	 * 
	 * 
	 * @return array of decision entries
	 */

	public DecisionEntry[] retrieveDecisionEntries(UserRequest request) {
		return null;

	}

	/**
	 * Info D
	 * 
	 * This method retrieves current decision entries associated to a user
	 * request, associated to offline mode (more restrictive decisions)
	 * 
	 * @param request
	 * 
	 * 
	 * @return array of decision entries
	 */

	public DecisionEntry[] retrieveDecisionEntriesOffline(UserRequest request) {
		return null;

	}
}
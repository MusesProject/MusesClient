package eu.musesproject.client.db.handler;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import eu.musesproject.client.model.decisiontable.DecisionTable;

public class DBManager {

	
	private static final String TAG = DBManager.class.getSimpleName();
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "muses_client_db";
	
	// Creating tables queries run at the start
	private static final String CREATE_POLICIES_TABLE_QUERY = "";
	private static final String CREATE_DECISIONTABLE_TABLE_QUERY = "CREATE TABLE decisiontable ( "
																	  + "id INTEGER PRIMARY KEY," 
																	  + "action_id INT NOT NULL,"   // fk action.id 
																	  + "resource_id INT NOT NULL," // fk resource.id 
																	  + "decision_id INT NOT NULL," // fk decision.id 
																	  + "subject_id INT NOT NULL,"  // fk subject.id 
																	  + "riskcommunication_id INT NOT NULL,"  // fk riskCommunication.id 
																	  + "modification TIMESTAMP NOT NULL);";
	private static final String CREATE_ACTION_TABLE_QUERY = "CREATE TABLE action ( "
																	  + "id INTEGER PRIMARY KEY," 
																	  + "description VARCHAR(45) NOT NULL,"
																	  + "modification TIMESTAMP NOT NULL);";
	private static final String CREATE_RESOURCE_TABLE_QUERY ="CREATE TABLE resource ( "
																	  + "id INTEGER PRIMARY KEY," 
																	  + "description VARCHAR(45) NOT NULL,"
																	  + "path VARCHAR(45) NOT NULL,"
																	  + "resourcetype INT NOT NULL," 		// fk resourceType.id
																	  + "modification TIMESTAMP NOT NULL);";
	private static final String CREATE_RESOURCE_TYPE_TABLE_QUERY = "CREATE TABLE resourcetype ( "
																	  + "id INTEGER PRIMARY KEY," 
																	  + "name VARCHAR(45) NOT NULL,"
																	  + "modification TIMESTAMP NOT NULL);";
	private static final String CREATE_DECISION_TABLE_QUERY = "CREATE TABLE decision ( "
																	  + "id INTEGER PRIMARY KEY," 
																	  + "name VARCHAR(45) NOT NULL,"
																	  + "modification TIMESTAMP NOT NULL);";
	private static final String CREATE_SUBJECT_TABLE_QUERY = "CREATE TABLE subject ( "
																	  + "id INTEGER PRIMARY KEY," 
																	  + "description VARCHAR(45) NOT NULL,"
																	  + "role_id INT NOT NULL,"				// fk role.id 
																	  + "modification TIMESTAMP NOT NULL);";
	private static final String CREATE_ROLE_TABLE_QUERY =  "CREATE TABLE role ( "
																	  + "id INTEGER PRIMARY KEY," 
																	  + "description VARCHAR(45) NOT NULL,"
																	  + "modification TIMESTAMP NOT NULL);";
	private static final String CREATE_RISK_TREATMENT_TABLE_QUERY =  "CREATE TABLE risktreatment ( "
																	  + "id INTEGER PRIMARY KEY," 
																	  + "textualdescription VARCHAR(45) NOT NULL);";
	private static final String CREATE_RISK_COMMUNICATION_TABLE_QUERY =  "CREATE TABLE riskcommunication	 ( "
																	  + "id INTEGER PRIMARY KEY," 
																	  + "communication_sequence INT NOT NULL,"
																	  + "risktreatment_id INT NOT NULL);";
	//  MusDM
	private static final String CREATE_CONTEXT_EVENTS_TABLE_QUERY =  "CREATE TABLE contextevent	 ( "
																	  + "id INTEGER PRIMARY KEY," 
																	  + "type VARCHAR(45) NOT NULL,"
																	  + "timestamp TIMESTAMP NOT NULL);";
	private static final String CREATE_PROPERTY_TABLE_QUERY =  "CREATE TABLE property	 ( "
																	  + "id INTEGER PRIMARY KEY," 
																	  + "contextevent_id INT NOT NULL,"
																	  + "key VARCHAR(45) NOT NULL,"
																	  + "value VARCHAR(45) NOT NULL);";
	private static final String CREATE_SERVER_CERT_TABLE_QUERY =  "CREATE TABLE property	 ( "
																	  + "id INTEGER PRIMARY KEY," 
																	  + "contextevent_id INT NOT NULL,"
																	  + "key VARCHAR(45) NOT NULL,"
																	  + "value VARCHAR(45) NOT NULL);";
	
	// Tables name 
	public static final String TABLE_POLICES = "polices";
	public static final String TABLE_DECISIONTABLE = "decisiontable";
	public static final String TABLE_DECISION = "decision";
	public static final String TABLE_ROLE = "role";
	public static final String TABLE_SUBJECT = "subject";
	public static final String TABLE_RESOURCE = "resource";
	public static final String TABLE_RESOURCE_TYPE = "resourcetype";
	public static final String TABLE_ACTION = "action";
	public static final String TABLE_RISK_TREATMENT = "risktreatment";
	public static final String TABLE_RISK_COMMUNICATION = "riskcommunication";
	public static final String TABLE_CONTEXT_EVENT = "contextevent";
	public static final String TABLE_PROPERTY = "property";

	// Columns name
	private static final String ID = "id";
	private static final String ACTION_ID = "action_id";
	private static final String RESOURCE_ID = "resource_id";
	private static final String DECISION_ID = "decision_id";
	private static final String SUBJECT_ID = "subject_id";
	private static final String RISKCOMMUNICATION_ID = "riskcommunication_id";
	private static final String MODIFICATION = "modification";
	private static final String DESCRIPTION = "decription";
	private static final String PATH = "path";
	private static final String RESOURCE_TYPE = "resourcetype";
	private static final String NAME = "name";
	private static final String ROLE_ID = "role_id";
	private static final String TEXTUAL_DESCRIPTION = "textualdescription";
	private static final String COMMUNICATION_SEQUENCE = "communication_sequence";
	private static final String RISKTREATMENT_ID = "risktreatment_id";
	private static final String CONTEXT_EVENT_ID = "contextevent_id";
	private static final String TYPE = "type";
	private static final String TIME_STAMP = "timestamp";
	private static final String KEY = "key";
	private static final String VALUE = "value";
	
	
	private Context context;
	private DatabaseHelper databaseHelper;
	private SQLiteDatabase sqLiteDatabase;
	
	public DBManager(Context context) {
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
	}

	
	public SQLiteDatabase openDB() { // always returns writableDB
		Log.d(TAG, "opening database..");
       	sqLiteDatabase = databaseHelper.getWritableDatabase();
		return sqLiteDatabase;
	}

	public void closeDB() {
    	if (sqLiteDatabase !=null){
    		databaseHelper.close();
    		sqLiteDatabase = null;
    	}		
	}

	public void encryptDB()  {
		// TBD
	}
	
	public void decryptDB(){
		// TBD
	}
	
	 /**
     * This is a private class which creates the database�when the application
     * starts or upgrades it if it already exist by removing the last version
     * of the databases
     * Create database .. and tables 
     *
     */
    public static class DatabaseHelper extends SQLiteOpenHelper {
		

		DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	Log.d( TAG,"Creating the DB" );
        	
        	db.execSQL(CREATE_ACTION_TABLE_QUERY);
        	db.execSQL(CREATE_DECISIONTABLE_TABLE_QUERY);
        	db.execSQL(CREATE_DECISION_TABLE_QUERY);
        	db.execSQL(CREATE_RESOURCE_TABLE_QUERY);
        	db.execSQL(CREATE_RESOURCE_TYPE_TABLE_QUERY);
        	db.execSQL(CREATE_ROLE_TABLE_QUERY);
        	db.execSQL(CREATE_SUBJECT_TABLE_QUERY);
        	db.execSQL(CREATE_RISK_COMMUNICATION_TABLE_QUERY);
        	db.execSQL(CREATE_RISK_TREATMENT_TABLE_QUERY);
        	db.execSQL(CREATE_CONTEXT_EVENTS_TABLE_QUERY);
        	db.execSQL(CREATE_PROPERTY_TABLE_QUERY);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)  {
        	Log.w(TAG, "Updating DB from previous version " + oldVersion 
                    + " to "
                    + newVersion);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DECISION);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DECISIONTABLE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTION);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESOURCE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESOURCE_TYPE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBJECT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROLE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RISK_COMMUNICATION);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RISK_TREATMENT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTEXT_EVENT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROPERTY);
            
            onCreate(db);
        }
        
        
        
    }     
    
    
    // All CRUD (Create, retrieve, update and delete ) operations here
    
    // Decision Maker related queries
    /**
     * Adds decision table in the DB
     * @param decisionTable
     */
    
    public void addDecisionTable(DecisionTable decisionTable){
    	
    	ContentValues values = new ContentValues();
    	values.put(ACTION_ID, "101");
    	values.put(RESOURCE_ID, "101");
    	values.put(DECISION_ID, "101");
    	values.put(SUBJECT_ID, "101");
    	values.put(RISKCOMMUNICATION_ID, "101");
    	values.put(MODIFICATION, "03-09-2011");
    	sqLiteDatabase.insert(TABLE_DECISIONTABLE, null	, values);
    }
    
    /**
     * Retrieve all decision tables 
     * @return list of Decision tables
     */
    
    public List<DecisionTable> getAllDecisionTables(){ // FIXME not right, there should be some criteria
    	
    	List<DecisionTable> decisionTableList = new ArrayList<DecisionTable>();
    	
    	// Select All Query
        String selectQuery = "select  * from " + TABLE_DECISIONTABLE;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                DecisionTable decisionTable = new DecisionTable();
                String id = cursor.getString(0);  // These values should in the DecisionTable object
                String action_id = cursor.getString(0);
                String resource_id = cursor.getString(0);
                String decision_id = cursor.getString(0);
                String subject_id = cursor.getString(0);
                String modification = cursor.getString(0);

                decisionTableList.add(null); // add created object here
            } while (cursor.moveToNext());
        }
        
        return decisionTableList;
    }
    
    /**
     * Retrieve decision table from action_id
     * @param action_id
     * @return DecisionTable
     */
    
    public DecisionTable getDecisionTableFromActionID(String action_id) {

    	Cursor cursor = sqLiteDatabase.query(TABLE_DECISIONTABLE, new String [] {
						ID, 
						ACTION_ID, 
						RESOURCE_ID, 
						DECISION_ID, 
						SUBJECT_ID, 
						RISKCOMMUNICATION_ID, 
						MODIFICATION},
						
    					ACTION_ID + "=?",
    					new String[] {String.valueOf(action_id)}, 
    					null, 
    					null, 
    					null);
        
    	if (cursor != null) {
    		cursor.moveToFirst();
    		
    		// Now create the decision table object from the cursor
    		Log.e(TAG, "id" + cursor.getString(0));
    		Log.e(TAG, "action_id" + cursor.getString(1));
    		Log.e(TAG, "resource_id" + cursor.getString(2));
    		Log.e(TAG, "decision_id" + cursor.getString(3));
    		Log.e(TAG, "subject_id" + cursor.getString(4));
    		Log.e(TAG, "riskcommunication_id" + cursor.getString(5));
    		Log.e(TAG, "modification" + cursor.getString(6));
    		
    	}
    	
    	return new DecisionTable();
    }
    
    /**
     * Retrieve decision table from action_id and resource_id
     * @param action_id
     * @param resource_id
     * @return DecisionTable
     */
    
    public DecisionTable getDecisionTableFromActionAndResource(String action_id, String resource_id/*Action action, Resource resource*/) {

    	Cursor cursor = sqLiteDatabase.query(TABLE_DECISIONTABLE, new String [] {
    					ID, 
    					ACTION_ID, 
    					RESOURCE_ID, 
    					DECISION_ID, 
    					SUBJECT_ID, 
    					RISKCOMMUNICATION_ID, 
    					MODIFICATION},
    					
    					ACTION_ID + " like " + action_id + " and " + RESOURCE_ID + " like " + resource_id,
    					null,			
    					null, 
    					null, 
    					null);
        
		if (cursor != null){
			cursor.moveToFirst();
			// Now create the decision table object from the cursor
			Log.e(TAG, "modification" + cursor.getString(6));
		}
    	return new DecisionTable();    	
    }
    
    /**
     * Retrieve decision table from action_id and subject_id
     * @param action_id
     * @param subject_id
     * @return DecisionTable
     */
    
    public DecisionTable getDecisionTableFromActionAndSubject(String action_id, String subject_id/*Action action, Subject subject*/) {
    	Cursor cursor = sqLiteDatabase.query(TABLE_DECISIONTABLE, new String [] {
						ID, 
						ACTION_ID, 
						RESOURCE_ID, 
						DECISION_ID, 
						SUBJECT_ID, 
						RISKCOMMUNICATION_ID, 
						MODIFICATION},
						
						ACTION_ID + " like " + action_id + " and " + SUBJECT_ID + " like " + subject_id,
						null,			
						null, 
						null, 
						null);
		
		if (cursor != null){
			cursor.moveToFirst();
			// Now create the decision table object from the cursor
			Log.e(TAG, "modification" + cursor.getString(6));
		}

		return new DecisionTable();     	
    }
    
    /**
     * Retrieve decision table from action_id, resource_id and subject_id 
     * @param action_id
     * @param resource_id
     * @param subject_id
     * @return DecisionTable
     */
    
    public DecisionTable getDecisionTableFromActionAndRecourceAndSubject(String action_id, String resource_id, String subject_id/*Action action, Resource resource, Subject subject*/) {
    	Cursor cursor = sqLiteDatabase.query(TABLE_DECISIONTABLE, new String [] {
						ID, 
						ACTION_ID, 
						RESOURCE_ID, 
						DECISION_ID, 
						SUBJECT_ID, 
						RISKCOMMUNICATION_ID, 
						MODIFICATION}, 
						
						ACTION_ID + " like " + action_id + " and " + RESOURCE_ID + " like " + resource_id 
								+ " and " + SUBJECT_ID + " like " + subject_id, // may be _id
						null,			
						null, 
						null, 
						null);

		if (cursor != null){
			cursor.moveToFirst();
			// Now create the decision table object from the cursor
			Log.e(TAG, "modification" + cursor.getString(6));
			
		}
		
		return new DecisionTable();      	
    }
    
    
    public void addAction(/*Action action*/){
    	
    	ContentValues values = new ContentValues();
    	values.put(DESCRIPTION, "fake descrtion ....");
    	values.put(MODIFICATION, "09-08-2012");
    	sqLiteDatabase.insert(TABLE_ACTION, null, values);

    }

    
    
    // Policy related queries
    
    public void addDevicePolicy(Policy policy){
    	String insertQuery = "TBD";
    	sqLiteDatabase.execSQL(insertQuery);
    	// TBD
    }
    
    public int updateDevicePolicy(Policy policy){
    	String updateQuery = "TBD";
    	sqLiteDatabase.execSQL(updateQuery);
    	// TBD
    	return 0;
    }	
    
    public int getNoOfDevicePoliciesStored(){
    	String selectQuery = "TBD";
    	sqLiteDatabase.execSQL(selectQuery);
    	// TBD
    	return 0;
    }
    
    
    public Policy getStoredDevicePolicy(int index){
    	String selectQuery = "TBD";
    	sqLiteDatabase.execSQL(selectQuery);
    	// TBD
    	return new Policy();
    }
    
    public void deleteDevicePolicy(Policy policy){
    	String deleteQuery = "TBD";
    	sqLiteDatabase.execSQL(deleteQuery);
    	// TBD
    }
    
    
    
    // Context Event related queries
    
    public void addContextEvent(/*ContextEvent event*/) {
    	ContentValues values = new ContentValues();
    	values.put(TYPE, "type1");
    	values.put(TIME_STAMP, System.currentTimeMillis());
    	sqLiteDatabase.insert(TABLE_CONTEXT_EVENT, null	, values);    
    }
    
    public int getNoOfContextEventsStored() {
        
    	String selectQuery = "select  * from " + TABLE_CONTEXT_EVENT;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        return cursor.getCount();

    }
    
    
    public void /*ContextEvent*/ getStoredContextEvent(String id) {
    	Cursor cursor = sqLiteDatabase.query(TABLE_CONTEXT_EVENT, new String [] {
    			ID, 
    			TYPE, 
    			TIME_STAMP}, 
    			
				ID + " like " + id,
				null,			
				null, 
				null, 
				null);

		if (cursor != null) {
			cursor.moveToFirst();
			// Now create the decision table object from the cursor
			Log.e(TAG, "timestamp" + cursor.getString(2));
		}
		

    }
    
    public void deleteStoredContextEvent(String id	){
    	
    	sqLiteDatabase.delete(TABLE_CONTEXT_EVENT, 
    			ID+ "=?", 
    			new String[] {String.valueOf(id)});
    }
    
    public void addProperty(/*Property property	*/) {
    	ContentValues values = new ContentValues();
    	values.put(CONTEXT_EVENT_ID, "1");
    	values.put(KEY, "protocol");
    	values.put(VALUE, "https");
    	sqLiteDatabase.insert(TABLE_PROPERTY, null	, values);    
    }
    
    
    
    
    // Server and Client Certificates related query
    
    public boolean setServerCert(ServerCertificate serverCertificate ){
    	return false;
    }
    
    // For future
    public ServerCertificate getServerCert(){
    	return new ServerCertificate();
    }
    
    // For future
    public boolean setUserDeviceCert(UserDeviceCertificate userDeviceCertificate) {
    	return false;
    }
    
    public UserDeviceCertificate getUserDeviceCert(){
    	return new UserDeviceCertificate();
    }
    
	
	
    


	
}

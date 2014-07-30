package eu.musesproject.client.db.handler;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.sensors.ISensor;
import eu.musesproject.client.contextmonitoring.sensors.SettingsSensor;
import eu.musesproject.client.db.entity.Action;
import eu.musesproject.client.db.entity.Configuration;
import eu.musesproject.client.db.entity.ContextEvent;
import eu.musesproject.client.db.entity.Decision;
import eu.musesproject.client.db.entity.DecisionTable;
import eu.musesproject.client.db.entity.Property;
import eu.musesproject.client.db.entity.Resource;
import eu.musesproject.client.db.entity.ResourceType;
import eu.musesproject.client.db.entity.RiskCommunication;
import eu.musesproject.client.db.entity.RiskTreatment;
import eu.musesproject.client.db.entity.Role;
import eu.musesproject.client.db.entity.SensorConfiguration;
import eu.musesproject.client.db.entity.Subject;

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
																	  + "condition VARCHAR(45) NOT NULL,"
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
	private static final String CREATE_SERVER_CERT_TABLE_QUERY =  "CREATE TABLE server_certificate	 ( "
																	  + "id INTEGER PRIMARY KEY," 
																	  + "contextevent_id INT NOT NULL,"
																	  + "key VARCHAR(45) NOT NULL,"
																	  + "value VARCHAR(45) NOT NULL);";
	private static final String CREATE_USER_CREDENTIALS_TABLE_QUERY =  "CREATE TABLE user_credentials	 ( "
																	  + "id INTEGER PRIMARY KEY," 
																	  + "device_id VARCHAR(45) NOT NULL,"
																	  + "username VARCHAR(45) NOT NULL,"
																	  + "password VARCHAR(45) NOT NULL);";
	private static final String CREATE_SENSOR_CONFIGURATION_TABLE_QUERY =  "CREATE TABLE sensor_configuration	 ( "
																	  + "id INTEGER PRIMARY KEY,"
																	  + "sensor_type VARCHAR(45) NOT NULL,"
																	  + "key VARCHAR(45) NOT NULL,"
																	  + "value VARCHAR(45) NOT NULL);";

	private static final String CREATE_CONFIGURATION_TABLE_QUERY =  "CREATE TABLE configuration	 ( "
			  + "id INTEGER PRIMARY KEY," 
			  + "server_ip VARCHAR(45) NOT NULL DEFAULT '192.168.44.101',"
			  + "server_port VARCHAR(45) NOT NULL DEFAULT '8443',"
			  + "server_context_path VARCHAR(45) NOT NULL DEFAULT '/server',"
			  + "server_servlet_path VARCHAR(45) NOT NULL DEFAULT '/commain',"
			  + "server_certificate VARCHAR(4500) NOT NULL,"
			  + "client_certificate VARCHAR(4500) NOT NULL,"
			  + "timeout INTEGER NOT NULL DEFAULT 5000,"
			  + "poll_timeout INTEGER NOT NULL DEFAULT 10000,"
			  + "sleep_poll_timeout INTEGER NOT NULL DEFAULT 60000,"
			  + "polling_enabled INTEGER NOT NULL DEFAULT 1);";

//	private static final String CREATE_CONFIGURATION_TABLE_QUERY =  "CREATE TABLE configuration	 ( "
//			  + "id INTEGER PRIMARY KEY," 
//			  + "server_ip VARCHAR(45) NOT NULL,"
//			  + "server_port VARCHAR(45) NOT NULL,"
//			  + "server_context_path VARCHAR(45) NOT NULL,"
//			  + "server_servlet_path VARCHAR(45) NOT NULL,"
//			  + "server_certificate VARCHAR(4500) NOT NULL,"
//			  + "client_certificate VARCHAR(4500) NOT NULL,"
//			  + "timeout INTEGER NOT NULL,"
//			  + "poll_timeout INTEGER NOT NULL,"
//			  + "sleep_poll_timeout INTEGER NOT NULL,"
//			  + "polling_enabled INTEGER NOT NULL);";


	
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
	public static final String TABLE_USER_CREADENTIALS = "user_credentials";
	public static final String TABLE_CONFIGURATION = "configuration";
	public static final String TABLE_SENSOR_CONFIGURATION = "sensor_configuration";
	

	// Columns name
	private static final String ID = "id";
	private static final String ACTION_ID = "action_id";
	private static final String RESOURCE_ID = "resource_id";
	private static final String DECISION_ID = "decision_id";
	private static final String SUBJECT_ID = "subject_id";
	private static final String RISKCOMMUNICATION_ID = "riskcommunication_id";
	private static final String DEVICE_ID = "device_id";
	private static final String MODIFICATION = "modification";
	private static final String DESCRIPTION = "description";
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
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String CONDITION = "condition";
	private static final String SERVER_IP = "server_ip";
	private static final String SERVER_PORT = "server_port";
	private static final String SERVER_CONTEXT_PATH = "server_context_path";
	private static final String SERVER_SERVLET_PATH = "server_servlet_path";
	private static final String TIMEOUT = "timeout";
	private static final String POLL_TIMEOUT = "poll_timeout";
	private static final String SLEEP_POLL_TIMEOUT = "sleep_poll_timeout";
	private static final String POLLING_ENABLED = "polling_enabled";
	private static final String SERVER_CERTIFICATE = "server_certificate";
	private static final String CLIENT_CERTIFICATE = "client_certificate";
	private static final String SENSOR_TYPE = "sensor_type";
	private static final String ENABLED = "enabled";
	
	
	
	private Context context;
	private DatabaseHelper databaseHelper;
	private SQLiteDatabase sqLiteDatabase;
	
	public DBManager(Context context) {
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
	}

	
	public synchronized DBManager openDB() { // always returns writableDB
		Log.d(TAG, "opening database..");
       	sqLiteDatabase = databaseHelper.getWritableDatabase();
		return this;
	}

	public synchronized void closeDB() {
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
     * This is a private class which creates the database when the application
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
        	db.execSQL(CREATE_USER_CREDENTIALS_TABLE_QUERY);
        	db.execSQL(CREATE_CONFIGURATION_TABLE_QUERY);
        	db.execSQL(CREATE_SENSOR_CONFIGURATION_TABLE_QUERY);
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
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_CREADENTIALS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONFIGURATION);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR_CONFIGURATION);
            onCreate(db);
            
        }
    }     
    
    
    // All CRUD (Create, retrieve, update and delete ) operations here
    
    public void insertSensorConfiguration(SensorConfiguration sensorConfiguration){
    	if(!sensorConfigExists(sensorConfiguration)) {
    		ContentValues values = new ContentValues();
    		values.put(SENSOR_TYPE, sensorConfiguration.getSensor_type());
    		values.put(KEY, sensorConfiguration.getKey());
    		values.put(VALUE, sensorConfiguration.getValue());
    		sqLiteDatabase.insert(TABLE_SENSOR_CONFIGURATION, null	, values);
    	}
    }
    
    // check if an equal config item exists to avoid duplicate entries
    private boolean sensorConfigExists(SensorConfiguration sensorConfiguration) {
    	Cursor cursor = sqLiteDatabase.query(
    			TABLE_SENSOR_CONFIGURATION, // table name
                null,                    // select
                SENSOR_TYPE + "=? AND " + 
        			KEY + "=? AND " +
        			VALUE + "=?", // where identifier
                new String[] {sensorConfiguration.getSensor_type(), sensorConfiguration.getKey(), sensorConfiguration.getValue()}, // where args
                null,null,null,null);
    	
    	if (cursor != null && cursor.moveToFirst()) {
    		return true;
    	}
    	else {
    		return false;
    	}
	}


	public List<SensorConfiguration> getAllSensorConfiguration(){
    	
    	List<SensorConfiguration> configurationList = new ArrayList<SensorConfiguration>();
    	
    	// Select All Query
        String selectQuery = "select  * from " + TABLE_SENSOR_CONFIGURATION;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
            	SensorConfiguration configuration = new SensorConfiguration();
            	configuration.setId(cursor.getInt(0));
                configuration.setSensor_type(cursor.getString(1));
                configuration.setKey(cursor.getString(2));
                configuration.setValue(cursor.getString(3));
                
                configurationList.add(configuration); 
            } while (cursor.moveToNext());
        }
        
        return configurationList;
    }
    
    public void insertCredentials(){
    	ContentValues values = new ContentValues();
    	values.put(DEVICE_ID, new SettingsSensor(context).getIMEI());
    	values.put(USERNAME, "muses");
    	values.put(PASSWORD, "muses");
    	sqLiteDatabase.insert(TABLE_USER_CREADENTIALS, null	, values);
    }
    
    public boolean isUserAuthenticated(String deviceId, String userName, String password) {
    	Cursor cursor = sqLiteDatabase.query(
    			TABLE_USER_CREADENTIALS, // table name
                null,                    // select
                DEVICE_ID + "=? AND " + 
        			USERNAME + "=? AND " +
        			PASSWORD + "=?", // where identifier
                new String[] {deviceId, userName, password}, // where args
                null,null,null,null);
    	
    	if (cursor != null && cursor.moveToFirst()) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    
    public String getDevId(){
    	String device_id = "";
        String selectQuery = "select  * from " + TABLE_USER_CREADENTIALS;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
            	if (cursor != null)
            		device_id = cursor.getString(1);
            } while (cursor.moveToNext());
        }
		return device_id;
    }
    
    // Configuration related queries
    public void insertConnectionProperties(Configuration config){
    	ContentValues values = new ContentValues();
    	
    	values.put(SERVER_IP, config.getServerIP());
    	values.put(SERVER_PORT, config.getServerPort());
    	values.put(SERVER_CONTEXT_PATH, config.getServerContextPath());
    	values.put(SERVER_SERVLET_PATH, config.getServerServletPath());
    	values.put(SERVER_CERTIFICATE, config.getServerCertificate());
    	values.put(CLIENT_CERTIFICATE, config.getServerCertificate());
    	values.put(TIMEOUT, config.getTimeout());
    	values.put(POLL_TIMEOUT, config.getPollTimeout());
    	values.put(SLEEP_POLL_TIMEOUT, config.getSleepPollTimeout());
    	values.put(POLLING_ENABLED, config.getPollingEnabled());
    	sqLiteDatabase.insert(TABLE_CONFIGURATION, null	, values);
    }
    
    public void insertServerCertificate(String certificate){
    	certificate = "-----BEGIN CERTIFICATE-----";
//					+ "MIID2DCCA0GgAwIBAgIBATANBgkqhkiG9w0BAQUFADCBvTELMAkGA1UEBhMCU1Yx"
//					+ "EjAQBgNVBAgMCVN0b2NraG9sbTEXMBUGA1UEBwwOU3RvY2tob2xtIENpdHkxHzAd"
//					+ "BgNVBAoMFlN3ZWRlbiBDb25uZWN0aXZpdHkgQUIxHzAdBgNVBAsMFkluZm9ybWF0"
//					+ "aW9uIFRlY2hub2xvZ3kxDjAMBgNVBAMMBUFkbWluMS8wLQYJKoZIhvcNAQkBFiB5"
//					+ "YXNpci5hbGlAc3dlZGVuY29ubmVjdGl2aXR5LmNvbTAeFw0xMzEwMjEwNzU4MTNa"
//					+ "Fw0xNDEwMjEwNzU4MTNaMIGkMQswCQYDVQQGEwJTVjESMBAGA1UECAwJU3RvY2to"
//					+ "b2xtMR8wHQYDVQQKDBZTd2VkZW4gQ29ubmVjdGl2aXR5IEFCMR8wHQYDVQQLDBZJ"
//					+ "bmZvcm1hdGlvbiBUZWNobm9sb2d5MQ4wDAYDVQQDDAVBZG1pbjEvMC0GCSqGSIb3"
//					+ "DQEJARYgeWFzaXIuYWxpQHN3ZWRlbmNvbm5lY3Rpdml0eS5jb20wggEiMA0GCSqG"
//					+ "SIb3DQEBAQUAA4IBDwAwggEKAoIBAQCkCpYMjTdaRuPWRH8olEn8172ZDlxLBKeg"
//					+ "ZhPijRJp0fKUK/UDq+1uccPvCL/CG3fie+w6VJGCMYnm+FZlWc8/6hZEb0Cn6Upy"
//					+ "fg9SwP7bgiXzHBlpv9UoD40nQ9LVzM/GZyCaThFRXIhQPGstuJv5JufjXHT9BGm5"
//					+ "/9ijQWinipwbrIBMxor5ZIJGS4lvJF+Lyo2N0zBXIi4nZfjDPbFWrpbx6tuHCdy0"
//					+ "4VpLw9Qd7Os4M1uD7NXKNcZrMOb6zS7Z2n3M5fosdEeojhCSe0wPTEQMmbeyXDJJ"
//					+ "urbcQQQ6JwLSJ/X00rHeRSb9HaJvehLZ1Lr+vevTTJ/TOz958JSRAgMBAAGjezB5"
//					+ "MAkGA1UdEwQCMAAwLAYJYIZIAYb4QgENBB8WHU9wZW5TU0wgR2VuZXJhdGVkIENl"
//					+ "cnRpZmljYXRlMB0GA1UdDgQWBBR3cFVlprhgomG3Ekpfv1vpjaPXgTAfBgNVHSME"
//					+ "GDAWgBRDB0qWULr2wI/yUbCpnwjwBZQsZzANBgkqhkiG9w0BAQUFAAOBgQBbkN+F"
//					+ "sd7JVDBRXU242iWJ3KO6XSQnLn2P90mZE2Y2L7amJgFEYSZOfjL2BzgePVcP94An"
//					+ "LYlbYm+iPMK5dpplbuGrGiLK7HRx3rfrjimLtbqrLMLnGUzv7YdL0c110iVf29Lq"
//					+ "lwKeHb+x0NgwdzCLcLfyC59rr55EII1U58cf3Q=="
//					+ "-----END CERTIFICATE-----";
		;
    	ContentValues values = new ContentValues();
    	values.put(SERVER_CERTIFICATE, certificate);
    	sqLiteDatabase.insert(TABLE_CONFIGURATION, null, values);
    }
    
    public String getServerCertificate() {
    	// Select All Query
    	String certificate = "";
        String selectQuery = "select  * from " + TABLE_CONFIGURATION;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
            	certificate = cursor.getString(5);
            }while (cursor.moveToNext());
        }
        return certificate;
	}
    
    public String getClientCertificate() {
    	// Select All Query
    	String certificate = "";
        String selectQuery = "select  * from " + TABLE_CONFIGURATION;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
            	certificate = cursor.getString(6);
            }while (cursor.moveToNext());
        }
        return certificate;
	}

    public Configuration getConfigurations(){
    	// Select All Query
        String selectQuery = "select  * from " + TABLE_CONFIGURATION;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        Configuration configuration = new Configuration();
        
        if (cursor.moveToFirst()) {
            do {
            	// configuration.setId(cursor.getInt(0)); FIXME commented for time being
            	configuration.setServerIP(cursor.getString(1));
            	configuration.setServerPort(cursor.getInt(2));
            	configuration.setServerContextPath(cursor.getString(3));
            	configuration.setServerServletPath(cursor.getString(4));
            	configuration.setServerCertificate(cursor.getString(5));
            	configuration.setClientCertificate(cursor.getString(6));
            	configuration.setTimeout(cursor.getInt(5));
            	configuration.setPollTimeout(cursor.getInt(6));
            	configuration.setSleepPollTimeout(cursor.getInt(7));
            	configuration.setPollingEnabled(cursor.getInt(8));
            } while (cursor.moveToNext());
        }
        return configuration;
        
    }
    
    // Decision Maker related queries
    /**
     * Adds decision table in the DB
     * @param decisionTable
     */
    
    public long addDecisionTable(DecisionTable decisionTable){
    	
    	ContentValues values = new ContentValues();
    	values.put(ACTION_ID, decisionTable.getAction_id());
    	values.put(RESOURCE_ID, decisionTable.getResource_id());
    	values.put(DECISION_ID, decisionTable.getDecision_id());
    	values.put(SUBJECT_ID, decisionTable.getSubject_id());
    	values.put(RISKCOMMUNICATION_ID, decisionTable.getRiskcommunication_id());
    	values.put(MODIFICATION, "03-09-2011");
    	
    	Log.d("DBManager", "Adding DT with action_id:"+decisionTable.getAction_id()+" decision_id:"+decisionTable.getDecision_id()+" riskCommunication_id:"+decisionTable.getRiskcommunication_id());
    	return sqLiteDatabase.insert(TABLE_DECISIONTABLE, null	, values);
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
    		while (!cursor.isAfterLast()){
    			// Now create the decision table object from the cursor
        		Log.d(TAG, "id" + cursor.getString(0));
        		Log.d(TAG, "action_id" + cursor.getString(1));
        		Log.d(TAG, "resource_id" + cursor.getString(2));
        		Log.d(TAG, "decision_id" + cursor.getString(3));
        		Log.d(TAG, "subject_id" + cursor.getString(4));
        		Log.d(TAG, "riskcommunication_id" + cursor.getString(5));
        		Log.d(TAG, "modification" + cursor.getString(6));
				cursor.moveToNext();
			}
    		
    		
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

    	DecisionTable decisionTable = null;
    	Cursor cursor = sqLiteDatabase.query(TABLE_DECISIONTABLE, new String [] {
    					ID, 
    					ACTION_ID, 
    					RESOURCE_ID, 
    					DECISION_ID, 
    					SUBJECT_ID, 
    					RISKCOMMUNICATION_ID, 
    					MODIFICATION},    					
    					//ACTION_ID + " LIKE " + action_id + " AND " + RESOURCE_ID + " LIKE " + resource_id,
    					ACTION_ID + "=? AND " + RESOURCE_ID + "=?",
    					//RESOURCE_ID + "=?",
    					new String[] {String.valueOf(action_id),String.valueOf(resource_id)},
    					//new String[] {String.valueOf(resource_id)},
    					null, 
    					null, 
    					null);
        
    	if (cursor != null){
			cursor.moveToFirst();
			Log.d(TAG, String.valueOf(cursor.getCount())+ " isAfterLast:"+cursor.isAfterLast());
			while (!cursor.isAfterLast()){
				// Now create the decision table object from the cursor
				decisionTable = new DecisionTable();
				decisionTable.setId(Integer.parseInt(cursor.getString(0)));
				decisionTable.setAction_id(Integer.parseInt(cursor.getString(1)));
				decisionTable.setResource_id(Integer.parseInt(cursor.getString(2)));
				decisionTable.setDecision_id(Integer.parseInt(cursor.getString(3)));
				cursor.moveToNext();
			}	
		}

    	return new DecisionTable();    	
    }
    
    public DecisionTable getDecisionTableFromResourceId(String resource_id, String action_id) {

    	DecisionTable decisionTable = new DecisionTable();
    	Cursor cursor = sqLiteDatabase.query(TABLE_DECISIONTABLE, new String [] {
						ID, 
						ACTION_ID, 
    					RESOURCE_ID, 
    					DECISION_ID, 
    					SUBJECT_ID, 
    					RISKCOMMUNICATION_ID, 
    					MODIFICATION}, 						
    					//ID + "=?",
    					//RESOURCE_ID + "=?",
    					RESOURCE_ID + "=?"+" AND " + ACTION_ID + "=?",
    					new String[] {String.valueOf(resource_id),String.valueOf(action_id)},
    					//null,
    					null, 
    					null, 
    					null);
        
    	if (cursor != null) {
    		cursor.moveToFirst();
    		Log.d(TAG, String.valueOf(cursor.getCount())+ " isAfterLast:"+cursor.isAfterLast());
    		while (!cursor.isAfterLast()){
    			// Now create the decision object from the cursor
    			decisionTable = new DecisionTable();
				decisionTable.setId(Integer.parseInt(cursor.getString(0)));
				decisionTable.setAction_id(Integer.parseInt(cursor.getString(1)));
				decisionTable.setResource_id(Integer.parseInt(cursor.getString(2)));
				decisionTable.setDecision_id(Integer.parseInt(cursor.getString(3)));
				decisionTable.setSubject_id(Integer.parseInt(cursor.getString(4)));
				decisionTable.setRiskcommunication_id(Integer.parseInt(cursor.getString(5)));
				cursor.moveToNext();
			}
    		
    		
    	}
    	
    	return decisionTable;
    }
    
    
    public DecisionTable getDecisionTableFromID(String decisiontable_id) {

    	DecisionTable decisionTable = new DecisionTable();
    	Cursor cursor = sqLiteDatabase.query(TABLE_DECISIONTABLE, new String [] {
						ID, 
						ACTION_ID, 
    					RESOURCE_ID, 
    					DECISION_ID, 
    					SUBJECT_ID, 
    					RISKCOMMUNICATION_ID, 
    					MODIFICATION}, 						
    					ID + "=?",
    					//"",
    					new String[] {String.valueOf(decisiontable_id)},
    					//null,
    					null, 
    					null, 
    					null);
        
    	if (cursor != null) {
    		cursor.moveToFirst();
    		Log.d(TAG, String.valueOf(cursor.getCount())+ " isAfterLast:"+cursor.isAfterLast());
    		while (!cursor.isAfterLast()){
    			// Now create the decision object from the cursor
    			decisionTable = new DecisionTable();
				decisionTable.setId(Integer.parseInt(cursor.getString(0)));
				decisionTable.setAction_id(Integer.parseInt(cursor.getString(1)));
				decisionTable.setResource_id(Integer.parseInt(cursor.getString(2)));
				decisionTable.setDecision_id(Integer.parseInt(cursor.getString(3)));
				cursor.moveToNext();
			}
    		
    		
    	}
    	
    	return decisionTable;
    }
    
    /**
     * Retrieve decision table from action_id and subject_id
     * @param action_id
     * @param subject_id
     * @return DecisionTable
     */
    
    public DecisionTable getDecisionTableFromActionAndSubject(String action_id, String subject_id/*Action action, Subject subject*/) {
    	
    	DecisionTable decisionTable = null;
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
			Log.d(TAG, String.valueOf(cursor.getCount())+ " isAfterLast:"+cursor.isAfterLast());
			while (!cursor.isAfterLast()){
				// Now create the decision table object from the cursor
				decisionTable = new DecisionTable();
				decisionTable.setId(Integer.parseInt(cursor.getString(0)));
				decisionTable.setAction_id(Integer.parseInt(cursor.getString(1)));
				decisionTable.setResource_id(Integer.parseInt(cursor.getString(2)));
				decisionTable.setDecision_id(Integer.parseInt(cursor.getString(3)));
				cursor.moveToNext();
			}	
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
    	DecisionTable decisionTable = null;
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
			decisionTable = new DecisionTable();
			decisionTable.setId(cursor.getInt(0));
			decisionTable.setAction_id(cursor.getInt(1));
			decisionTable.setResource_id(cursor.getInt(2));
			decisionTable.setDecision_id(cursor.getInt(3));
			Log.d(TAG, "modification" + cursor.getString(6));
			
		}else{
			Log.e(TAG, "No decision table element found with action_id: "+action_id + " and resource_id:" + resource_id);
		}
		
		return decisionTable;      	
    }
    
    
    public long addAction(Action action){
    	//TODO Manage the insertion or update, avoiding duplicated entries
    	
    	ContentValues values = new ContentValues();
    	values.put(DESCRIPTION, action.getDescription());
    	values.put(MODIFICATION, "09-08-2012");
    	return sqLiteDatabase.insert(TABLE_ACTION, null, values);

    }
    
    
    /**
     * Inserts into riskTreatment table in the DB
     * @param riskTreatment
     */
    
    public long addRiskTreatment(RiskTreatment riskTreatment){
    	//TODO Manage the insertion or update, avoiding duplicated entries
    	
    	ContentValues values = new ContentValues();
    	values.put(TEXTUAL_DESCRIPTION, riskTreatment.getTextualdescription());
    	return sqLiteDatabase.insert(TABLE_RISK_TREATMENT, null	, values);
    }
    
    
    /**
     * Inserts into resourceType table in the DB
     * @param resourceType
     */
    
    public long addResourceType(ResourceType resourceType){
    	//TODO Manage the insertion or update, avoiding duplicated entries
    	
    	ContentValues values = new ContentValues();
    	values.put(NAME, resourceType.getName());
    	values.put(MODIFICATION, "03-09-2011");
    	return sqLiteDatabase.insert(TABLE_RESOURCE_TYPE, null	, values);
    }
    
    /**
     * Inserts into resource table in the DB
     * @param resource
     */
    
    public long addResource(Resource resource){
    	//TODO Manage the insertion or update, avoiding duplicated entries
    	
    	ContentValues values = new ContentValues();
    	values.put(DESCRIPTION, resource.getDescription());
    	values.put(PATH, resource.getPath());
    	values.put(RESOURCE_TYPE, resource.getResourcetype());
    	values.put(MODIFICATION, "03-09-2011");
    	return sqLiteDatabase.insert(TABLE_RESOURCE, null, values);
    }
    
    
    /**
     * Inserts into riskCommunication table in the DB
     * @param riskCommunication
     */
    
    public long addRiskCommunication(RiskCommunication riskCommunication){
    	//TODO Manage the insertion or update, avoiding duplicated entries
    	
    	ContentValues values = new ContentValues();
    	values.put(COMMUNICATION_SEQUENCE, riskCommunication.getCommunication_sequence());
    	values.put(RISKTREATMENT_ID, riskCommunication.getRisktreatment_id());
    	return sqLiteDatabase.insert(TABLE_RISK_COMMUNICATION, null	, values);
    }

    /**
     * Inserts into role table in the DB
     * @param role
     */
    
    public long addRole(Role role){
    	//TODO Manage the insertion or update, avoiding duplicated entries
    	
    	
    	ContentValues values = new ContentValues();
    	values.put(DESCRIPTION, role.getDescription());
    	values.put(TIME_STAMP, role.getTimestamp());
    	values.put(MODIFICATION, "03-09-2011");
    	return sqLiteDatabase.insert(TABLE_ROLE, null	, values);
    }
    
    /**
     * Inserts into subject table in the DB
     * @param role
     */
    
    public long addSubject(Subject subject){
    	//TODO Manage the insertion or update, avoiding duplicated entries
    	
    	ContentValues values = new ContentValues();
    	values.put(DESCRIPTION, subject.getDescription());
    	values.put(ROLE_ID, subject.getRoleID());
    	values.put(MODIFICATION, "03-09-2011");
    	return sqLiteDatabase.insert(TABLE_SUBJECT, null	, values);
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
    
    public long addContextEvent(ContextEvent event) {
    	ContentValues values = new ContentValues();
    	values.put(TYPE, event.getType());
    	values.put(TIME_STAMP, event.getTimestamp());
    	return sqLiteDatabase.insert(TABLE_CONTEXT_EVENT, null	, values);    
    }
    
    public int getNoOfContextEventsStored() {
        
    	String selectQuery = "select  * from " + TABLE_CONTEXT_EVENT;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        return cursor.getCount();

    }
    public List<Property> getPropertiesOfContextEvent(int contextevent_id) {
    	List<Property> propertyList = new ArrayList<Property>();

    	Cursor cursor = sqLiteDatabase.query(TABLE_PROPERTY, new String [] {
    			ID, 
    			CONTEXT_EVENT_ID, 
    			KEY,
    			VALUE}, 
    			
				CONTEXT_EVENT_ID + " like " + contextevent_id,
				null,			
				null, 
				null, 
				null);

    	// loop through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Property property = new Property();
                property.setId(Integer.parseInt(cursor.getString(0)));
                property.setContextevent_id(Integer.parseInt(cursor.getString(1)));
                property.setKey(cursor.getString(2));
                property.setValue(cursor.getString(3));
                propertyList.add(property);
                
            } while (cursor.moveToNext());
        }

        return propertyList;    	
    }
    
    public ContextEvent getStoredContextEvent(String id) {
    	Cursor cursor = sqLiteDatabase.query(TABLE_CONTEXT_EVENT, new String [] {
    			ID, 
    			TYPE, 
    			TIME_STAMP}, 
    			
				ID + " like " + id,
				null,			
				null, 
				null, 
				null);

    	ContextEvent contextEvent = new ContextEvent();
		if (cursor != null) {
			cursor.moveToFirst();
			// Now create the decision table object from the cursor
            contextEvent.setId(Integer.parseInt(cursor.getString(0)));
            contextEvent.setType(cursor.getString(1));
            contextEvent.setTimestamp(cursor.getString(2));
		}
		return contextEvent;

    }
    
    public void deleteStoredContextEvent(String id	){
    	
    	sqLiteDatabase.delete(TABLE_CONTEXT_EVENT, 
    			ID+ "=?", 
    			new String[] {String.valueOf(id)});
    }
    
    public void addProperty(Property property) {
    	ContentValues values = new ContentValues();
    	values.put(CONTEXT_EVENT_ID, property.getContextevent_id());
    	values.put(KEY, property.getKey());
    	values.put(VALUE, property.getValue());
    	sqLiteDatabase.insert(TABLE_PROPERTY, null	, values);    
    }
    
    public List<ContextEvent> getAllStoredContextEvents() {
    	List<ContextEvent> contextEventsList = new ArrayList<ContextEvent>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTEXT_EVENT;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        // loop through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ContextEvent contextEvent = new ContextEvent();
                contextEvent.setId(Integer.parseInt(cursor.getString(0)));
                contextEvent.setType(cursor.getString(1));
                contextEvent.setTimestamp(cursor.getString(2));
                contextEventsList.add(contextEvent);
            } while (cursor.moveToNext());
        }

        return contextEventsList;
    }
    
    
    public List<Property> getAllProperties() {
    	List<Property> propertyList = new ArrayList<Property>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PROPERTY;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        // loop through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	Property property = new Property();
                property.setId(Integer.parseInt(cursor.getString(0)));
                property.setContextevent_id(Integer.parseInt(cursor.getString(1)));
                property.setKey(cursor.getString(2));
                property.setValue(cursor.getString(3));
                propertyList.add(property);
            } while (cursor.moveToNext());
        }

        return propertyList;
    }
    
    /**
     * Retrieve decision from id
     * @param decision_id
     * @return Decision
     */
    
    public Decision getDecisionFromID(String decision_id) {

    	Decision decision = new Decision();
    	Cursor cursor = sqLiteDatabase.query(TABLE_DECISION, new String [] {
						ID, 
						NAME,
						CONDITION,
						MODIFICATION},
						
    					ID + "=?",
    					new String[] {String.valueOf(decision_id)}, 
    					null, 
    					null, 
    					null);
        
    	if (cursor != null) {
    		cursor.moveToFirst();
    		while (!cursor.isAfterLast()){
    			// Now create the decision object from the cursor
        		Log.d(TAG, "id " + cursor.getString(0));
        		decision.setId(Integer.valueOf(cursor.getString(0)));
        		Log.d(TAG, "name " + cursor.getString(1));
        		decision.setName(cursor.getString(1));
        		Log.d(TAG, "condition " + cursor.getString(2));
        		decision.setCondition(cursor.getString(2));
        		Log.d(TAG, "modification " + cursor.getString(3));
        		//decision.setModification(Long.valueOf(cursor.getString(2)));
				cursor.moveToNext();
			}
    		
    		
    	}
    	
    	return decision;
    }
    

    
    /**
     * Retrieve risk_communication from id
     * @param risk_communication_id
     * @return RiskCommunication
     */
    
    public RiskCommunication getRiskCommunicationFromID(String risk_communication_id) {

    	RiskCommunication comm = new RiskCommunication();
    	Cursor cursor = sqLiteDatabase.query(TABLE_RISK_COMMUNICATION, new String [] {
						ID, 
						COMMUNICATION_SEQUENCE,
						RISKTREATMENT_ID},
						
    					ID + "=?",
    					new String[] {String.valueOf(risk_communication_id)}, 
    					null, 
    					null, 
    					null);
        
    	if (cursor != null) {
    		cursor.moveToFirst();
    		Log.d(TAG, String.valueOf(cursor.getCount())+ " isAfterLast:"+cursor.isAfterLast());
    		while (!cursor.isAfterLast()){
    			// Now create the decision object from the cursor
        		Log.d(TAG, "id" + cursor.getString(0));
        		comm.setId(Integer.valueOf(cursor.getString(0)));
        		Log.d(TAG, "comm_sequence" + cursor.getString(1));
        		comm.setCommunication_sequence(Integer.valueOf(cursor.getString(1)));
        		Log.d(TAG, "risk_treatment_id" + cursor.getString(2));
        		comm.setRisktreatment_id(Integer.valueOf(cursor.getString(2)));
				cursor.moveToNext();
			}
    		
    		
    	}
    	
    	return comm;
    }
    
    /**
     * Retrieve risk_treatment from id
     * @param risk_treatment_id
     * @return RiskTreatment
     */
    
    public RiskTreatment getRiskTreatmentFromID(String risk_treatment_id) {

    	RiskTreatment treatment = new RiskTreatment();
    	Cursor cursor = sqLiteDatabase.query(TABLE_RISK_TREATMENT, new String [] {
						ID, 
						TEXTUAL_DESCRIPTION},
						
    					ID + "=?",
    					new String[] {String.valueOf(risk_treatment_id)}, 
    					null, 
    					null, 
    					null);
        
    	if (cursor != null) {
    		cursor.moveToFirst();
    		Log.d(TAG, String.valueOf(cursor.getCount())+ " isAfterLast:"+cursor.isAfterLast());
			while (!cursor.isAfterLast()){
				Log.d(TAG, "id" + cursor.getString(0));
	    		treatment.setId(Integer.valueOf(cursor.getString(0)));
	    		Log.d(TAG, "textual_description" + cursor.getString(1));
	    		treatment.setTextualdescription(cursor.getString(1));
				cursor.moveToNext();
			}

    	}
    	
    	return treatment;
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


	public long addDecision(Decision decision) {
		long result = 0;
		Cursor cursor = sqLiteDatabase.query(TABLE_DECISION, new String [] {
				ID, 
				NAME, 
				MODIFICATION},
				
				NAME + " like '" + decision.getName()+"'",
				null,			
				null, 
				null, 
				null);
		
		
		//if (cursor.getCount() == 1){
	    	ContentValues values = new ContentValues();
	    	values.put(NAME, decision.getName());
	    	values.put(CONDITION, decision.getCondition());
	    	values.put(MODIFICATION, "09-08-2012");
	    	result = sqLiteDatabase.insert(TABLE_DECISION, null, values);
		/*}else{
			Log.d(TAG, "Decision count:" + cursor.getCount()); //+ "id:" + cursor.getInt(0) + "name:" + cursor.getString(1));
			return 0;
		}
		result = Long.valueOf(cursor.getString(0));*/

		return result;
	}
    
	public Resource getResourceFromPath(String path) {
    	Cursor cursor = sqLiteDatabase.query(TABLE_RESOURCE, new String [] {
    			ID, 
    			DESCRIPTION,
    			PATH,
    			RESOURCE_TYPE,
    			MODIFICATION}, 
    			
				PATH + " LIKE '" + path + "'",
				null,			
				null, 
				null, 
				null);

    	Resource resource = new Resource();
		if (cursor != null) {
			cursor.moveToFirst();
			Log.d(TAG, String.valueOf(cursor.getCount())+ " isAfterLast:"+cursor.isAfterLast());
			while (!cursor.isAfterLast()){
				Log.d(TAG, cursor.getString(0));
				resource.setId(Integer.parseInt(cursor.getString(0)));
				resource.setDescription(cursor.getString(1));
				resource.setPath(cursor.getString(2));
				cursor.moveToNext();
			}	
		}
		return resource;

    }
    
    
	
	public Action getActionFromType(String type) {
    	Cursor cursor = sqLiteDatabase.query(TABLE_ACTION, new String [] {
    			ID, 
    			DESCRIPTION,
    			MODIFICATION}, 
    			
				DESCRIPTION + " LIKE '" + type + "'",
				null,			
				null, 
				null, 
				null);

    	Action action = new Action();
		if (cursor != null) {
			cursor.moveToFirst();
			Log.d(TAG, String.valueOf(cursor.getCount())+ " isAfterLast:"+cursor.isAfterLast());
			while (!cursor.isAfterLast()){
				action.setId(Integer.parseInt(cursor.getString(0)));
				action.setDescription(cursor.getString(1));
				cursor.moveToNext();
			}	
		}
		return action;

    }
    
	public List<SensorConfiguration> getAllSensorConfigItemsBySensorType(String type) {
		List<SensorConfiguration> configurationList = new ArrayList<SensorConfiguration>();
		Log.d(TAG, "type="  + type); 
		Cursor cursor = sqLiteDatabase.rawQuery("SELECT key, value FROM sensor_configuration WHERE sensor_type=?", new String[] {type});
    	
    	if (cursor != null && cursor.moveToFirst()) {
    		while (!cursor.isAfterLast()) {
    			SensorConfiguration configItem = new SensorConfiguration();
    			configItem.setSensor_type(type);
    			configItem.setKey(cursor.getString(0));
    			configItem.setValue(cursor.getString(1));
    			
    			configurationList.add(configItem);
				cursor.moveToNext();
			}
    	}
		
		return configurationList;
	}
	
	public List<String> getAllEnabledSensorTypes() {
		List<String> enabledSensors = new ArrayList<String>();
		
		Cursor cursor = sqLiteDatabase.query(
				TABLE_SENSOR_CONFIGURATION, // table name
				new String[] {SENSOR_TYPE}, // select
				KEY + "=? AND " + 
					VALUE + "=?",
				new String[] {ISensor.CONFIG_KEY_ENABLED, "true"},// where args
				null,null,null,null);
		
		if (cursor != null && cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				enabledSensors.add(cursor.getString(0));
				cursor.moveToNext();
			}
		}
		
		return enabledSensors;
	}
	
	public boolean hasSensorConfig() {
		String selectQuery = "select  COUNT(*) from " + TABLE_SENSOR_CONFIGURATION;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
		
		if (cursor != null && cursor.moveToFirst()) {
			return true;
		}
		return false;
	}
}

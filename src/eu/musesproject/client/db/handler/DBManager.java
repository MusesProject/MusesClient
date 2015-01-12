package eu.musesproject.client.db.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.sensors.ISensor;
import eu.musesproject.client.db.entity.Action;
import eu.musesproject.client.db.entity.Configuration;
import eu.musesproject.client.db.entity.ContextEvent;
import eu.musesproject.client.db.entity.Decision;
import eu.musesproject.client.db.entity.DecisionTable;
import eu.musesproject.client.db.entity.Property;
import eu.musesproject.client.db.entity.RequiredApp;
import eu.musesproject.client.db.entity.Resource;
import eu.musesproject.client.db.entity.ResourceProperty;
import eu.musesproject.client.db.entity.ResourceType;
import eu.musesproject.client.db.entity.RiskCommunication;
import eu.musesproject.client.db.entity.RiskTreatment;
import eu.musesproject.client.db.entity.Role;
import eu.musesproject.client.db.entity.SensorConfiguration;
import eu.musesproject.client.db.entity.Subject;
import eu.musesproject.client.utils.MusesUtils;

public class DBManager {
	private static final String TAG = DBManager.class.getSimpleName();
	
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "muses_client_db";
	
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
																	  + "condition VARCHAR(200),"
																	  + "resourcetype INT NOT NULL," 		// fk resourceType.id
																	  + "name VARCHAR(45) NOT NULL,"
																	  + "severity VARCHAR(45) NOT NULL,"
																	  + "type VARCHAR(45) NOT NULL,"
																	  + "modification TIMESTAMP NOT NULL);";
	private static final String CREATE_RESOURCE_TYPE_TABLE_QUERY = "CREATE TABLE resourcetype ( "
																	  + "id INTEGER PRIMARY KEY," 
																	  + "name VARCHAR(45) NOT NULL,"
																	  + "modification TIMESTAMP NOT NULL);";
	private static final String CREATE_RESOURCE_PROPERTY_TABLE_QUERY = "CREATE TABLE resource_property ( "
																	  + "id INTEGER PRIMARY KEY,"
																	  + "resource_id INT NOT NULL,"
																	  + "key VARCHAR(45) NOT NULL,"
																	  + "value VARCHAR(500) NOT NULL);";
	private static final String CREATE_DECISION_TABLE_QUERY = "CREATE TABLE decision ( "
																	  + "id INTEGER PRIMARY KEY," 
																	  + "name VARCHAR(45) NOT NULL,"
																	  + "condition VARCHAR(45),"
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

	private static final String CREATE_REQUIRED_APPS_TABLE_QUERY = "CREATE TABLE required_apps ( "
																	  + "id INTEGER PRIMARY KEY,"
																	  + "name VARCHAR(45) NOT NULL,"
																	  + "version VARCHAR(45) NOT NULL,"
																	  + "unique_name VARCHAR(45) NOT NULL);";
	
	private static final String CREATE_CONFIGURATION_TABLE_QUERY =  "CREATE TABLE configuration	 ( "
			  + "id INTEGER PRIMARY KEY," 
			  + "server_ip VARCHAR(45) NOT NULL DEFAULT '192.168.44.101',"
			  //+ "server_ip VARCHAR(45) NOT NULL DEFAULT '192.168.1.11',"
			  + "server_port VARCHAR(45) NOT NULL DEFAULT '8443',"
			  + "server_context_path VARCHAR(45) NOT NULL DEFAULT '/server',"
			  + "server_servlet_path VARCHAR(45) NOT NULL DEFAULT '/commain',"
			  + "server_certificate VARCHAR(4500) NOT NULL,"
			  + "client_certificate VARCHAR(4500) NOT NULL,"
			  + "timeout INTEGER NOT NULL DEFAULT 5000,"
			  + "poll_timeout INTEGER NOT NULL DEFAULT 10000,"
			  + "sleep_poll_timeout INTEGER NOT NULL DEFAULT 60000,"
			  + "polling_enabled INTEGER NOT NULL DEFAULT 1,"
			  + "login_attempts INTEGER NOT NULL DEFAULT 5,"
			  + "silent_mode INTEGER NOT NULL DEFAULT 0);";

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
	public static final String TABLE_RESOURCE_PROPERTY = "resource_property";
	public static final String TABLE_ACTION = "action";
	public static final String TABLE_RISK_TREATMENT = "risktreatment";
	public static final String TABLE_RISK_COMMUNICATION = "riskcommunication";
	public static final String TABLE_CONTEXT_EVENT = "contextevent";
	public static final String TABLE_PROPERTY = "property";
	public static final String TABLE_USER_CREADENTIALS = "user_credentials";
	public static final String TABLE_CONFIGURATION = "configuration";
	public static final String TABLE_SENSOR_CONFIGURATION = "sensor_configuration";
	public static final String TABLE_REQUIRED_APPS_CONFIGURATION = "required_apps";
	
	// Columns name
	private static final String ID = "id";
	//private static final String _ID = "_id";
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
	private static final String LOGIN_ATTEMPTS = "login_attempts";
	private static final String VERSION = "version";
	private static final String UNIQUE_NAME = "unique_name";
	private static final String SEVERITY = "severity";
	
	
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
        	db.execSQL(CREATE_RESOURCE_PROPERTY_TABLE_QUERY);
        	db.execSQL(CREATE_ROLE_TABLE_QUERY);
        	db.execSQL(CREATE_SUBJECT_TABLE_QUERY);
        	db.execSQL(CREATE_RISK_COMMUNICATION_TABLE_QUERY);
        	db.execSQL(CREATE_RISK_TREATMENT_TABLE_QUERY);
        	db.execSQL(CREATE_CONTEXT_EVENTS_TABLE_QUERY);
        	db.execSQL(CREATE_PROPERTY_TABLE_QUERY);
        	db.execSQL(CREATE_USER_CREDENTIALS_TABLE_QUERY);
        	db.execSQL(CREATE_CONFIGURATION_TABLE_QUERY);
        	db.execSQL(CREATE_SENSOR_CONFIGURATION_TABLE_QUERY);
        	db.execSQL(CREATE_REQUIRED_APPS_TABLE_QUERY);
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
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESOURCE_PROPERTY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBJECT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROLE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RISK_COMMUNICATION);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RISK_TREATMENT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTEXT_EVENT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROPERTY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_CREADENTIALS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONFIGURATION);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR_CONFIGURATION);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_REQUIRED_APPS_CONFIGURATION);
            onCreate(db);
            
        }
    }     
    
    
    // All CRUD (Create, retrieve, update and delete ) operations here
    
    public void insertSensorConfiguration(SensorConfiguration sensorConfiguration){
    	if(!sensorConfigExists(sensorConfiguration)) {
    		ContentValues values = new ContentValues();
    		values.put(SENSOR_TYPE, sensorConfiguration.getSensorType());
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
                new String[] {sensorConfiguration.getSensorType(), sensorConfiguration.getKey(), sensorConfiguration.getValue()}, // where args
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
                configuration.setSensorType(cursor.getString(1));
                configuration.setKey(cursor.getString(2));
                configuration.setValue(cursor.getString(3));
                
                configurationList.add(configuration); 
            } while (cursor.moveToNext());
        }
        
        return configurationList;
    }
    
	public void inserRequiredAppList() {
    	ContentValues values = new ContentValues();
    	values.put(NAME, "Avast");
    	values.put(VERSION, "3.10");
    	values.put(UNIQUE_NAME, "com.avast.security.antivirus");
    	sqLiteDatabase.insert(TABLE_REQUIRED_APPS_CONFIGURATION, null	, values);

       	values.put(NAME, "AnyConnect VPN Client");
    	values.put(VERSION, "2.20");
    	values.put(UNIQUE_NAME, "com.anyconnect.vpn.client");
    	sqLiteDatabase.insert(TABLE_REQUIRED_APPS_CONFIGURATION, null	, values);

       	values.put(NAME, "Lotus");
    	values.put(VERSION, "1.11");
    	values.put(UNIQUE_NAME, "com.lotus.email.client");
    	sqLiteDatabase.insert(TABLE_REQUIRED_APPS_CONFIGURATION, null	, values);
    	
       	values.put(NAME, "Encrypt Plus");
    	values.put(VERSION, "1.08");
    	values.put(UNIQUE_NAME, "com.secure.encryptplus");
    	sqLiteDatabase.insert(TABLE_REQUIRED_APPS_CONFIGURATION, null	, values);

	}
	
	public List<RequiredApp> getRequiredAppList(){
    	List<RequiredApp> appsList = new ArrayList<RequiredApp>();
    	
    	// Select All Query
        String selectQuery = "select  * from " + TABLE_REQUIRED_APPS_CONFIGURATION;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        RequiredApp requiredApp = new RequiredApp();
        
        if (cursor.moveToFirst()) {
            do {
            	requiredApp.setId(cursor.getInt(0));
            	requiredApp.setName(cursor.getString(1));
            	requiredApp.setVersion(cursor.getString(2));
            	requiredApp.setUniqueName(cursor.getString(3));

            	appsList.add(requiredApp);
            } while (cursor.moveToNext());
        }
        return appsList;
	}
	
    public void insertCredentials(String deviceId, String userName, String password){
    	ContentValues values = new ContentValues();
    	values.put(DEVICE_ID, deviceId);
    	values.put(USERNAME, userName);
    	values.put(PASSWORD, password);
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
    public void insertConnectionProperties(){
    	ContentValues values = new ContentValues();
    	values.put(SERVER_IP, MusesUtils.getMusesConf());
    	values.put(SERVER_PORT, 8443);
    	values.put(SERVER_CONTEXT_PATH, "/server");
    	values.put(SERVER_SERVLET_PATH, "/commain");
    	values.put(SERVER_CERTIFICATE, MusesUtils.getCertificateFromSDCard(context));
    	values.put(CLIENT_CERTIFICATE, "");
    	values.put(TIMEOUT, 5000);
    	values.put(POLL_TIMEOUT, 5000);
    	values.put(SLEEP_POLL_TIMEOUT, 10000);
    	values.put(POLLING_ENABLED, 1);
    	values.put(LOGIN_ATTEMPTS, 5);
    	sqLiteDatabase.insert(TABLE_CONFIGURATION, null	, values);
    }

    public void insertConfiguration(Configuration configuration){
    	ContentValues values = new ContentValues();
    	values.put(SERVER_IP, configuration.getServerIP());
    	values.put(SERVER_PORT, configuration.getServerPort());
    	values.put(SERVER_CONTEXT_PATH, configuration.getServerContextPath());
    	values.put(SERVER_SERVLET_PATH, configuration.getServerServletPath());
    	values.put(SERVER_CERTIFICATE, configuration.getServerCertificate());
    	values.put(CLIENT_CERTIFICATE, configuration.getClientCertificate());
    	values.put(TIMEOUT, configuration.getTimeout());
    	values.put(POLL_TIMEOUT, configuration.getPollTimeout());
    	values.put(SLEEP_POLL_TIMEOUT, configuration.getSleepPollTimeout());
    	values.put(POLLING_ENABLED, configuration.getPollingEnabled());
    	values.put(LOGIN_ATTEMPTS, configuration.getLoginAttempts());
    	sqLiteDatabase.insert(TABLE_CONFIGURATION, null	, values);
    }
    
    public void deleteConnectionProperties(int id){
    	sqLiteDatabase.delete(TABLE_CONFIGURATION, "id="+id, null);
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
            	configuration.setTimeout(cursor.getInt(7));
            	configuration.setPollTimeout(cursor.getInt(8));
            	configuration.setSleepPollTimeout(cursor.getInt(9));
            	configuration.setPollingEnabled(cursor.getInt(10));
            	configuration.setLoginAttempts(cursor.getInt(11));
            	configuration.setSilentMode(cursor.getInt(12));
            } while (cursor.moveToNext());
        }
        return configuration;
        
    }
    
    public List<Configuration> getConfiguration(){
    	List<Configuration> conList = new ArrayList<Configuration>();
    	
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
            	configuration.setTimeout(cursor.getInt(7));
            	configuration.setPollTimeout(cursor.getInt(8));
            	configuration.setSleepPollTimeout(cursor.getInt(9));
            	configuration.setPollingEnabled(cursor.getInt(10));
            	configuration.setLoginAttempts(cursor.getInt(11));
            	configuration.setSilentMode(cursor.getInt(12));
            	
            	conList.add(configuration);
            } while (cursor.moveToNext());
        }
        return conList;
        
    }
    
    
    public boolean isSilentModeActive() {
    	boolean isSilentModeActive = false;
    	
    	// Select All Query
        String selectQuery = "select silent_mode from " + TABLE_CONFIGURATION;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
            	isSilentModeActive = cursor.getInt(12) == 1;
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        return isSilentModeActive;
        
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
    	
    	Action actionInDb = getActionFromDescription(action.getDescription());
    	if (actionInDb.getId()==0){
    		Log.d(TAG,"Action not found, inserting a new one...");
    		return sqLiteDatabase.insert(TABLE_ACTION, null, values);
    	}else{
    		Log.d(TAG,"Action found, returning the existing one..."+actionInDb.getId());
    		return actionInDb.getId();
    	}
    }
    
    
    private Action getActionFromDescription(String description) {
    	Cursor cursor = sqLiteDatabase.query(TABLE_ACTION, new String [] {
    			ID, 
    			DESCRIPTION,
    			MODIFICATION}, 
    			
    			DESCRIPTION + " LIKE '" + description + "'",
				null,			
				null, 
				null, 
				null);

    	Action action = new Action();
		if (cursor != null) {
			cursor.moveToFirst();
			Log.d(TAG, String.valueOf(cursor.getCount())+ " isAfterLast:"+cursor.isAfterLast());
			while (!cursor.isAfterLast()){
				Log.d(TAG, cursor.getString(0));
				action.setId(Integer.parseInt(cursor.getString(0)));
				action.setDescription(cursor.getString(1));
				cursor.moveToNext();
			}	
		}
		return action;
	}


	/**
     * Inserts into riskTreatment table in the DB
     * @param riskTreatment
     */
    
    public long addRiskTreatment(RiskTreatment riskTreatment){
    	//TODO Manage the insertion or update, avoiding duplicated entries
    	
    	ContentValues values = new ContentValues();
    	values.put(TEXTUAL_DESCRIPTION, riskTreatment.getTextualdescription());
    	
    	RiskTreatment riskTreatmentInDb = getRiskTreatmentFromDescription(riskTreatment.getTextualdescription());
    	if (riskTreatmentInDb.getId()==0){
    		Log.d(TAG,"Risktreatment not found, inserting a new one...");
    		return sqLiteDatabase.insert(TABLE_RISK_TREATMENT, null	, values);
    	}else{
    		Log.d(TAG,"Risktreatment found, returning the existing one..."+riskTreatmentInDb.getId());
    		return riskTreatmentInDb.getId();
    	}
    	
    }
    
    
    private RiskTreatment getRiskTreatmentFromDescription(String textualdescription) {
    	Cursor cursor = sqLiteDatabase.query(TABLE_RISK_TREATMENT, new String [] {
    			ID, 
    			TEXTUAL_DESCRIPTION}, 
    			
    			TEXTUAL_DESCRIPTION + " LIKE '" + textualdescription + "'",
				null,			
				null, 
				null, 
				null);

    	RiskTreatment riskTreatment = new RiskTreatment();
		if (cursor != null) {
			cursor.moveToFirst();
			Log.d(TAG, String.valueOf(cursor.getCount())+ " isAfterLast:"+cursor.isAfterLast());
			while (!cursor.isAfterLast()){
				Log.d(TAG, cursor.getString(0));
				riskTreatment.setId(Integer.parseInt(cursor.getString(0)));
				riskTreatment.setTextualdescription(cursor.getString(1));
				cursor.moveToNext();
			}	
		}
		return riskTreatment;
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
    	
    	Log.d(TAG,"");
    	int size = controlDB("before");
    	
    	ContentValues values = new ContentValues();
    	values.put(DESCRIPTION, resource.getDescription());
    	values.put(PATH, resource.getPath());
    	values.put(RESOURCE_TYPE, resource.getResourcetype());
    	values.put(CONDITION, resource.getCondition());
    	values.put(MODIFICATION, "03-09-2011");
    	values.put(NAME, resource.getName());
    	values.put(SEVERITY, resource.getSeverity());
    	values.put(TYPE, resource.getType());
    	
    	Resource resourceInDb = getResourceFromPathAndCondition(resource.getPath(), resource.getCondition());
    	Log.d(TAG, "Resource path: "+resource.getPath());
    	Log.d(TAG, "Resource condition: "+resource.getCondition());
    	Log.d(TAG, "ResourceInDb id: "+resourceInDb.getId());
    	Log.d(TAG, "ResourceInDb condition: "+resourceInDb.getCondition());
    	Log.d(TAG, "ResourceInDb path: "+resourceInDb.getPath());
    	if (resourceInDb.getId()==0){
    		Log.d(TAG,"Resource not found, inserting a new one...");
    		//long id = sqLiteDatabase.insertWithOnConflict(TABLE_RESOURCE, null, values, SQLiteDatabase.CONFLICT_ABORT);
    		long id = sqLiteDatabase.insert(TABLE_RESOURCE, null, values);
    		controlDB("after "+ id);
    		Log.d(TAG,"");
    		return id;
    	}else{
    		Log.d(TAG,"Resource found, returning the existing one..."+resourceInDb.getId());
    		controlDB("after "+ resourceInDb.getId());
    		Log.d(TAG,"");
    		return resourceInDb.getId();
    	}

    	
    }
    
    
    public int controlDB(String control){
    	List<Resource> allConditionResources = getAllResources();
    	
		Log.d(TAG, control +" Found..."+allConditionResources.size());
		
		for (Iterator iterator = allConditionResources.iterator(); iterator
				.hasNext();) {
			Resource resource = (Resource) iterator.next();
			if (resource.getCondition()!=null){
				Log.d(TAG, "Condition:"+resource.getCondition());
			}
			if (resource.getPath()!=null){
				Log.d(TAG, "Path:"+resource.getPath());
			}
			Log.d(TAG, "	Id:"+resource.getId());
			
		}
		return allConditionResources.size();
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
    	
    	RiskCommunication riskCommunicationInDb = getRiskCommunicationFromTreatmentId(riskCommunication.getRisktreatment_id());
    	if (riskCommunicationInDb.getId()==0){
    		Log.d(TAG,"RiskCommunication not found, inserting a new one...");
    		return sqLiteDatabase.insert(TABLE_RISK_COMMUNICATION, null	, values);
    	}else{
    		Log.d(TAG,"RiskCommunication found, returning the existing one...");
    		return riskCommunicationInDb.getId();
    	}
    	
    	
    }

    private RiskCommunication getRiskCommunicationFromTreatmentId(
			int risktreatment_id) {
    	Cursor cursor = sqLiteDatabase.query(TABLE_RISK_COMMUNICATION, new String [] {
    			ID,
    			COMMUNICATION_SEQUENCE,
    			RISKTREATMENT_ID}, 
    			
    			RISKTREATMENT_ID + " = " + risktreatment_id + "",
				null,			
				null, 
				null, 
				null);

    	RiskCommunication riskCommunication = new RiskCommunication();
		if (cursor != null) {
			cursor.moveToFirst();
			Log.d(TAG, String.valueOf(cursor.getCount())+ " isAfterLast:"+cursor.isAfterLast());
			while (!cursor.isAfterLast()){
				Log.d(TAG, cursor.getString(0));
				riskCommunication.setId(Integer.parseInt(cursor.getString(0)));
				riskCommunication.setCommunication_sequence(Integer.parseInt(cursor.getString(1)));
				riskCommunication.setRisktreatment_id(Integer.parseInt(cursor.getString(2)));
				cursor.moveToNext();
			}	
		}
		return riskCommunication;
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

	    ContentValues values = new ContentValues();
	    values.put(NAME, decision.getName());
	    values.put(CONDITION, decision.getCondition());
	    values.put(MODIFICATION, "09-08-2012");
	    
	    Decision decisionInDb = getDecisionFromNameAndCondition(decision.getName(), decision.getCondition());
    	if (decisionInDb.getId()==0){
    		Log.d(TAG,"Decision not found, inserting a new one...");
    		result = sqLiteDatabase.insert(TABLE_DECISION, null, values);
    	}else{
    		Log.d(TAG,"Decision found, returning the existing one..."+decisionInDb.getId());
    		return decisionInDb.getId();
    	}
    	
		return result;
	}
    
	private Decision getDecisionFromNameAndCondition(String name,
			String condition) {
		Cursor cursor = sqLiteDatabase.query(TABLE_DECISION, new String [] {
    			ID, 
    			NAME,
    			CONDITION,
    			MODIFICATION}, 
    			
				NAME + " LIKE '" + name + "' AND " + CONDITION + " LIKE '" + condition + "'"  ,
				null,			
				null, 
				null, 
				null);

    	Decision decision = new Decision();
		if (cursor != null) {
			cursor.moveToFirst();
			Log.d(TAG, String.valueOf(cursor.getCount())+ " isAfterLast:"+cursor.isAfterLast());
			while (!cursor.isAfterLast()){
				Log.d(TAG, cursor.getString(0));
				decision.setId(Integer.parseInt(cursor.getString(0)));
				decision.setName(cursor.getString(1));
				decision.setCondition(cursor.getString(2));
				cursor.moveToNext();
			}	
		}
		return decision;
	}


	public Resource getResourceFromPath(String path) {
    	Cursor cursor = sqLiteDatabase.query(TABLE_RESOURCE, new String [] {
    			ID, 
    			DESCRIPTION,
    			PATH,
    			CONDITION,
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
				resource.setCondition(cursor.getString(3));
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
    			configItem.setSensorType(type);
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
	
    public List<ResourceProperty> getPropertiesFromResourceId(String resource_id) {

    	List<ResourceProperty> properties = new ArrayList<ResourceProperty>();
    	Cursor cursor = sqLiteDatabase.query(TABLE_RESOURCE_PROPERTY, new String [] {
						ID,  
    					RESOURCE_ID, 
    					KEY,
    					VALUE}, 						
    					RESOURCE_ID + "=?",
    					new String[] {String.valueOf(resource_id)},
    					//null,
    					null, 
    					null, 
    					null);
    	
    	//Cursor cursor = sqLiteDatabase.rawQuery("SELECT key, value FROM properties WHERE resource_id=?", new String[] {String.valueOf(resource_id)});
    	
    	
    	if (cursor != null && cursor.moveToFirst()) {
    		while (!cursor.isAfterLast()) {
    			ResourceProperty property = new ResourceProperty();    			
    			property.setKey(cursor.getString(0));
    			property.setValue(cursor.getString(1));    			
    			properties.add(property);
				cursor.moveToNext();
			}
    	}
    	
    	return properties;
    }
    
    public Resource getResourceFromCondition(String condition) {
    	Cursor cursor = sqLiteDatabase.query(TABLE_RESOURCE, new String [] {
    			ID, 
    			DESCRIPTION,
    			PATH,
    			CONDITION,
    			RESOURCE_TYPE,
    			MODIFICATION}, 
    			
				CONDITION + " LIKE '" + condition + "'",
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
				resource.setCondition(cursor.getString(3));
				cursor.moveToNext();
			}	
		}
		return resource;

    }
    
    public List<Resource> getAllResourcesWithCondition() {
    	
    	List<Resource> resourceList = new ArrayList<Resource>();
    	Cursor cursor = sqLiteDatabase.query(TABLE_RESOURCE, new String [] {
    			ID, 
    			DESCRIPTION,
    			PATH,
    			CONDITION,
    			RESOURCE_TYPE,
    			MODIFICATION}, 
    			ID + " IS NOT NULL",
				//CONDITION + " IS NOT NULL",
				null,			
				null, 
				null, 
				null);

    	Resource resource = new Resource();
    	if (cursor != null && cursor.moveToFirst()) {
    		while (!cursor.isAfterLast()) {
    			Log.d(TAG, cursor.getString(0));
				resource.setId(Integer.parseInt(cursor.getString(0)));
				resource.setDescription(cursor.getString(1));
				resource.setPath(cursor.getString(2));
				resource.setCondition(cursor.getString(3));		
    			resourceList.add(resource);
				cursor.moveToNext();
			}
    	}
    	
		return resourceList;

    }
    
    public List<Resource> getAllResources() {
		List<Resource> resourceList = new ArrayList<Resource>();
		
		Cursor cursor = sqLiteDatabase.rawQuery("SELECT id, description, path, condition, resourceType FROM resource",null);
    	
		
    	if (cursor != null && cursor.moveToFirst()) {
    		while (!cursor.isAfterLast()) {
    			Resource resource = new Resource();
    			int id = Integer.parseInt(cursor.getString(0));
    			resource.setId(id);
    			Log.d(TAG, String.valueOf(id));				
				resource.setDescription(cursor.getString(1));
				resource.setPath(cursor.getString(2));
				String condition = cursor.getString(3);
				resource.setCondition(condition);
				Log.d(TAG, "getAllResources condition:"+condition);
    			resourceList.add(resource);
				cursor.moveToNext();
			}
    	}
		
		return resourceList;
	}
    
	public Resource getResourceFromPathAndCondition(String path, String condition) {
		
		Cursor cursor = null;
		
		if ((condition==null)||(condition.equals("null"))){
			
			cursor = sqLiteDatabase.query(TABLE_RESOURCE, new String [] {
	    			ID, 
	    			DESCRIPTION,
	    			PATH,
	    			CONDITION,
	    			RESOURCE_TYPE,
	    			MODIFICATION}, 
	    			
					PATH + " LIKE '" + path + "'" ,
					null,			
					null, 
					null, 
					null);
			
		}else{
		
			cursor = sqLiteDatabase.query(TABLE_RESOURCE, new String [] {
    			ID, 
    			DESCRIPTION,
    			PATH,
    			CONDITION,
    			RESOURCE_TYPE,
    			MODIFICATION}, 
    			
				PATH + " LIKE '" + path + "' AND " + CONDITION + " LIKE '" + condition + "'"  ,
				null,			
				null, 
				null, 
				null);
		}
    	Resource resource = new Resource();
		if (cursor != null) {
			cursor.moveToFirst();
			Log.d(TAG, String.valueOf(cursor.getCount())+ " isAfterLast:"+cursor.isAfterLast());
			while (!cursor.isAfterLast()){
				Log.d(TAG, cursor.getString(0));
				resource.setId(Integer.parseInt(cursor.getString(0)));
				resource.setDescription(cursor.getString(1));
				resource.setPath(cursor.getString(2));
				resource.setCondition(cursor.getString(3));
				cursor.moveToNext();
			}	
		}
		return resource;

    }
    
}

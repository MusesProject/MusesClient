package eu.musesproject.client.db.handler;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import eu.musesproject.client.contextmonitoring.sensors.DeviceProtectionSensor;
import eu.musesproject.client.contextmonitoring.sensors.FileSensor;
import eu.musesproject.client.contextmonitoring.sensors.LocationSensor;
import eu.musesproject.client.db.entity.SensorConfiguration;

public class MockUpHandler {
	private Context context;
	
	public MockUpHandler(Context context) {
		this.context = context;
	}
    
    public void createMockUpSensorConfiguration() {
		List<SensorConfiguration> configList = new ArrayList<SensorConfiguration>();
		/*
		 *  device protection sensor
		 */
		SensorConfiguration deviceProtectionConfig = new SensorConfiguration();
		deviceProtectionConfig.setSensor_type(DeviceProtectionSensor.TYPE);
		deviceProtectionConfig.setKey(DeviceProtectionSensor.CONFIG_KEY_TRUSTED_AV);
		deviceProtectionConfig.setValue("avast! Mobile Security");
		configList.add(deviceProtectionConfig);
		deviceProtectionConfig.setValue("Mobile Security & Antivirus");
		configList.add(deviceProtectionConfig);
		deviceProtectionConfig.setValue("Avira Antivirus Security");
		configList.add(deviceProtectionConfig);
		deviceProtectionConfig.setValue("Norton Security & Antivirus");
		configList.add(deviceProtectionConfig);
		deviceProtectionConfig.setValue("CM Security & Find My Phone");
		configList.add(deviceProtectionConfig);
    	
		/*
		 * Location sensor
		 */
		SensorConfiguration locationConfig = new SensorConfiguration();
		locationConfig.setSensor_type(LocationSensor.TYPE);
		locationConfig.setKey(LocationSensor.CONFIG_KEY_MIN_DIS);
		locationConfig.setValue("10");
		configList.add(locationConfig);
		locationConfig.setKey(LocationSensor.CONFIG_KEY_MIN_TIME);
		locationConfig.setValue("400");
		configList.add(locationConfig);
		locationConfig.setKey(LocationSensor.CONFIG_KEY_SECURE_ZONE_RADIUS);
		locationConfig.setValue("12.0");
		configList.add(locationConfig);

		/*
		 * Location sensor
		 */
		SensorConfiguration fileConfig = new SensorConfiguration();
		locationConfig.setSensor_type(FileSensor.TYPE);
		locationConfig.setKey(FileSensor.CONFIG_KEY_PATH);
		locationConfig.setValue("/Pictures/Screenshots/");
		configList.add(locationConfig);
		
		
		/*
		 * insert all config items into the db
		 */
    	DBManager dbManager = new DBManager(context);
		dbManager.openDB();
		for(SensorConfiguration configItem : configList) {
			dbManager.insertSensorConfiguration(configItem);
		}
		dbManager.closeDB();
    }
}
package eu.musesproject.client.db.handler;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import eu.musesproject.client.contextmonitoring.sensors.AppSensor;
import eu.musesproject.client.contextmonitoring.sensors.ConnectivitySensor;
import eu.musesproject.client.contextmonitoring.sensors.DeviceProtectionSensor;
import eu.musesproject.client.contextmonitoring.sensors.ISensor;
import eu.musesproject.client.contextmonitoring.sensors.InteractionSensor;
import eu.musesproject.client.contextmonitoring.sensors.LocationSensor;
import eu.musesproject.client.contextmonitoring.sensors.NotificationSensor;
import eu.musesproject.client.contextmonitoring.sensors.PackageSensor;
import eu.musesproject.client.contextmonitoring.sensors.RecursiveFileSensor;
import eu.musesproject.client.contextmonitoring.sensors.SettingsSensor;
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
		deviceProtectionConfig.setSensorType(DeviceProtectionSensor.TYPE);
		deviceProtectionConfig.setKey(DeviceProtectionSensor.CONFIG_KEY_TRUSTED_AV);
		deviceProtectionConfig.setValue("CM Security AppLock &AntiVirus");
		configList.add(deviceProtectionConfig);

		deviceProtectionConfig = new SensorConfiguration();
		deviceProtectionConfig.setSensorType(DeviceProtectionSensor.TYPE);
		deviceProtectionConfig.setKey(DeviceProtectionSensor.CONFIG_KEY_TRUSTED_AV);
		deviceProtectionConfig.setValue("Mobile Security & Antivirus");
		configList.add(deviceProtectionConfig);

		deviceProtectionConfig = new SensorConfiguration();
		deviceProtectionConfig.setSensorType(DeviceProtectionSensor.TYPE);
		deviceProtectionConfig.setKey(DeviceProtectionSensor.CONFIG_KEY_TRUSTED_AV);
		deviceProtectionConfig.setValue("Avira Antivirus Security");
		configList.add(deviceProtectionConfig);

		deviceProtectionConfig = new SensorConfiguration();
		deviceProtectionConfig.setSensorType(DeviceProtectionSensor.TYPE);
		deviceProtectionConfig.setKey(DeviceProtectionSensor.CONFIG_KEY_TRUSTED_AV);
		deviceProtectionConfig.setValue("Norton Security & Antivirus");
		configList.add(deviceProtectionConfig);

		deviceProtectionConfig = new SensorConfiguration();
		deviceProtectionConfig.setSensorType(DeviceProtectionSensor.TYPE);
		deviceProtectionConfig.setKey(DeviceProtectionSensor.CONFIG_KEY_TRUSTED_AV);
		deviceProtectionConfig.setValue("CM Security & Find My Phone");
		configList.add(deviceProtectionConfig);

		deviceProtectionConfig = new SensorConfiguration();
		deviceProtectionConfig.setSensorType(DeviceProtectionSensor.TYPE);
		deviceProtectionConfig.setKey(ISensor.CONFIG_KEY_ENABLED);
		deviceProtectionConfig.setValue("true");
		configList.add(deviceProtectionConfig);
    	
		/*
		 * Location sensor
		 */
		SensorConfiguration locationConfig = new SensorConfiguration();
		locationConfig.setSensorType(LocationSensor.TYPE);
		locationConfig.setKey(LocationSensor.CONFIG_KEY_MIN_DIS);
		locationConfig.setValue("10");
		configList.add(locationConfig);
		
		locationConfig = new SensorConfiguration();
		locationConfig.setSensorType(LocationSensor.TYPE);
		locationConfig.setKey(LocationSensor.CONFIG_KEY_MIN_TIME);
		locationConfig.setValue("400");
		configList.add(locationConfig);
		
		locationConfig = new SensorConfiguration();
		locationConfig.setSensorType(LocationSensor.TYPE);
		locationConfig.setKey(LocationSensor.CONFIG_KEY_SECURE_ZONE_RADIUS);
		locationConfig.setValue("12.0");
		configList.add(locationConfig);
		
		locationConfig = new SensorConfiguration();
		locationConfig.setSensorType(LocationSensor.TYPE);
		locationConfig.setKey(ISensor.CONFIG_KEY_ENABLED);
		locationConfig.setValue("true");
		configList.add(locationConfig);

		/*
		 * File sensor
		 */
		SensorConfiguration fileConfig = new SensorConfiguration();
		fileConfig.setSensorType(RecursiveFileSensor.TYPE);
		fileConfig.setKey(RecursiveFileSensor.CONFIG_KEY_PATH);
		fileConfig.setValue("/Swe/");
		configList.add(fileConfig);
		
		fileConfig = new SensorConfiguration();
		fileConfig.setSensorType(RecursiveFileSensor.TYPE);
		fileConfig.setKey(ISensor.CONFIG_KEY_ENABLED);
		fileConfig.setValue("true");
		configList.add(fileConfig);
		
		/*
		 * App sensor
		 */
		SensorConfiguration appConfig = new SensorConfiguration();
		appConfig.setSensorType(AppSensor.TYPE);
		appConfig.setKey(ISensor.CONFIG_KEY_ENABLED);
		appConfig.setValue("true");
		configList.add(appConfig);
		
		/*
		 * Connectivity sensor
		 */
		SensorConfiguration connectivityConfig = new SensorConfiguration();
		connectivityConfig.setSensorType(ConnectivitySensor.TYPE);
		connectivityConfig.setKey(ISensor.CONFIG_KEY_ENABLED);
		connectivityConfig.setValue("true");
		configList.add(connectivityConfig);

		
		/*
		 * Interaction sensor
		 */
		SensorConfiguration interactionConfig = new SensorConfiguration();
		interactionConfig.setSensorType(InteractionSensor.TYPE);
		interactionConfig.setKey(ISensor.CONFIG_KEY_ENABLED);
		interactionConfig.setValue("true");
		configList.add(interactionConfig);
		
		/*
		 * Package sensor
		 */
		SensorConfiguration packageConfig = new SensorConfiguration();
		packageConfig.setSensorType(PackageSensor.TYPE);
		packageConfig.setKey(ISensor.CONFIG_KEY_ENABLED);
		packageConfig.setValue("true");
		configList.add(packageConfig);
		
		/*
		 * Settings sensor
		 */
		SensorConfiguration settingsConfig = new SensorConfiguration();
		settingsConfig.setSensorType(SettingsSensor.TYPE);
		settingsConfig.setKey(ISensor.CONFIG_KEY_ENABLED);
		settingsConfig.setValue("true");
		configList.add(settingsConfig);
		
		/*
		 * Notification sensor
		 */
		SensorConfiguration notificationConfig = new SensorConfiguration();
		notificationConfig.setSensorType(NotificationSensor.TYPE);
		notificationConfig.setKey(ISensor.CONFIG_KEY_ENABLED);
		notificationConfig.setValue("true");
		configList.add(notificationConfig);
		
		
		/*
		 * insert all config items into the db
		 */
		/*
    	DBManager dbManager = new DBManager(context);
		dbManager.openDB();
		for(SensorConfiguration configItem : configList) {
			dbManager.insertSensorConfiguration(configItem);
		}
		dbManager.closeDB();
		
		Log.d("test", "config test:mock up data created");
		*/
    }
}
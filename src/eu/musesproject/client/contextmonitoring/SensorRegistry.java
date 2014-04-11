/**
 * 
 */
package eu.musesproject.client.contextmonitoring;

import java.net.URI;

import eu.musesproject.client.contextmonitoring.sensors.ISensor;
import eu.musesproject.client.model.contextmonitoring.SensorInfo;

/**
 * @author zardosht
 *
 */
public interface SensorRegistry {
	
	void installSensor(URI packageURI);
	void uninstallSensor(ISensor iSensor);
	void registerSensor(ISensor iSensor);
	void deregisterSensor(ISensor iSensor);
	SensorInfo getSensorInfo(ISensor iSensor);

}

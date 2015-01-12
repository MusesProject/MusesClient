package eu.musesproject.client.db.entity;

public class SensorConfiguration {
	
	private int id;
	private String sensorType;
	private String key;
	private String value;
	
	public SensorConfiguration() {

	}
	
	public SensorConfiguration(String sensorType, String key, String value) {
		this.sensorType = sensorType;
		this.key = key;
		this.value = value;
	}
	
	public SensorConfiguration(int id, String sensorType, String key, String value) {
		this.id = id;
		this.sensorType = sensorType;
		this.key = key;
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSensorType() {
		return sensorType;
	}

	public void setSensorType(String sensor_type) {
		this.sensorType = sensor_type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "SensorConfiguration [id=" + id + ", sensor_type=" + sensorType
				+ ", key=" + key + ", value=" + value
				+ "]";
	}
}
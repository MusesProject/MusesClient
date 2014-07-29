package eu.musesproject.client.db.entity;

public class SensorConfiguration {
	
	private int id;
	private String sensor_type;
	private int enabled;
	private String key;
	private String value;
	
	public SensorConfiguration() {

	}
	public SensorConfiguration(int id, String sensor_type, int enabled,
			String key, String value) {
		super();
		this.id = id;
		this.sensor_type = sensor_type;
		this.enabled = enabled;
		this.key = key;
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSensor_type() {
		return sensor_type;
	}

	public void setSensor_type(String sensor_type) {
		this.sensor_type = sensor_type;
	}

	public int getEnabled() {
		return enabled;
	}

	public void setEnabled(int enabled) {
		this.enabled = enabled;
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
		return "SensorConfiguration [id=" + id + ", sensor_type=" + sensor_type
				+ ", enabled=" + enabled + ", key=" + key + ", value=" + value
				+ "]";
	}
	
	
	
}

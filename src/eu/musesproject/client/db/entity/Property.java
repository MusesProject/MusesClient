package eu.musesproject.client.db.entity;

public class Property {
	
	private int id;
	private int contextevent_id;
	private String key;
	private String value;
	
	public Property() {

	}
	public Property(int id, int contextevent_id, String key, String value) {
		this.id = id;
		this.contextevent_id = contextevent_id;
		this.key = key;
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getContextevent_id() {
		return contextevent_id;
	}

	public void setContextevent_id(int contextevent_id) {
		this.contextevent_id = contextevent_id;
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
		return "Property [id=" + id + ", contextevent_id=" + contextevent_id
				+ ", key=" + key + ", value=" + value + "]";
	}
	
	
	
}

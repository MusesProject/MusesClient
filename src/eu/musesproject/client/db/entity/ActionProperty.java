package eu.musesproject.client.db.entity;

public class ActionProperty {
	
	private int id;
	private int actionId;
	private String key;
	private String value;
	
	public ActionProperty() {

	}
	public ActionProperty(int id, int actionId, String key, String value) {
		this.id = id;
		this.actionId = actionId;
		this.key = key;
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getActionId() {
		return actionId;
	}

	public void setActionId(int actionId) {
		this.actionId = actionId;
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
		return "Property [id=" + id + ", actionId=" + actionId
				+ ", key=" + key + ", value=" + value + "]";
	}
}
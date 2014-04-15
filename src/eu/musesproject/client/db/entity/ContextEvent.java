package eu.musesproject.client.db.entity;

public class ContextEvent {
	
	private int id;
	private String type;
	private String timestamp;

	public ContextEvent() {
	}
	public ContextEvent(int id, String type, String timestamp) {
		this.id = id;
		this.type = type;
		this.timestamp = timestamp;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "ContextEvent [id=" + id + ", type=" + type + ", timestamp="
				+ timestamp + "]";
	} 
	
	
	
}

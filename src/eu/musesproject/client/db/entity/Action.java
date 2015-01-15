package eu.musesproject.client.db.entity;

public class Action {
	
	private int id;
	private String description;
	private String actionType;
	private long timestamp;
	
	public Action() {
	}
	public Action(int id, String description, String actionType, long timestamp) {
		super();
		this.id = id;
		this.description = description;
		this.actionType = actionType;
		this.timestamp = timestamp;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "Action [id=" + id + ", description=" + description
				+ ", actionType=" + actionType + ", timestamp=" + timestamp
				+ "]";
	}

	
	
}

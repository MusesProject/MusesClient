package eu.musesproject.client.db.entity;

public class Subject {
	
	private int id;
	private String description;
	private int roleID;
	private String timestamp;
	
	public Subject(){
		super();
	}
	
	public Subject(int id, String description, int roleID, String timestamp) {
		super();
		this.id = id;
		this.description = description;
		this.roleID = roleID;
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

	public int getRoleID() {
		return roleID;
	}

	public void setRoleID(int roleID) {
		this.roleID = roleID;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "Subject [id=" + id + ", description=" + description
				+ ", roleID=" + roleID + ", timestamp=" + timestamp + "]";
	}
	
	
	
}

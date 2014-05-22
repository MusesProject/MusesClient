package eu.musesproject.client.db.entity;

public class Role {

	private int id;
	private String description;
	private String timestamp;
	
	public Role(){
		super();
	}
	
	public Role(int id, String description, String timestamp) {
		super();
		this.id = id;
		this.description = description;
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

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "Role [id=" + id + ", description=" + description
				+ ", timestamp=" + timestamp + "]";
	}
	
	
}

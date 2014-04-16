package eu.musesproject.client.db.entity;

public class Action {
	
	private int id;
	private String description;
	private long modification;
	
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
	public long getModification() {
		return modification;
	}
	public void setModification(long modification) {
		this.modification = modification;
	}
	
	

}

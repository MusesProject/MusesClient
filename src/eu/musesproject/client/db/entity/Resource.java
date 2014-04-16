package eu.musesproject.client.db.entity;

public class Resource {
	
	private int id;
	private String description;
	private String path;
	private int resourcetype;
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
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getResourcetype() {
		return resourcetype;
	}
	public void setResourcetype(int resourcetype) {
		this.resourcetype = resourcetype;
	}
	public long getModification() {
		return modification;
	}
	public void setModification(long modification) {
		this.modification = modification;
	}
	

}

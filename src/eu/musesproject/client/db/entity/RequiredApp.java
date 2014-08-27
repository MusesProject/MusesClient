package eu.musesproject.client.db.entity;

public class RequiredApp {
	
	private int id;
	private String name;
	private String version;
	private String uniqueName;
	
	public RequiredApp() {
	}

	public RequiredApp(int id, String name, String version, String uniqueName) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.uniqueName = uniqueName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	@Override
	public String toString() {
		return "RequiredApp [id=" + id + ", name=" + name + ", version="
				+ version + ", uniqueName=" + uniqueName + "]";
	}
	
	
}

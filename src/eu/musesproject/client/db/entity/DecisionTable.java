package eu.musesproject.client.db.entity;

public class DecisionTable {

	private int id;
	private int action_id;
	private int resource_id;
	private int decision_id;
	private int subject_id;
	private int riskcommunication_id;
	private long modification;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getAction_id() {
		return action_id;
	}
	public void setAction_id(int action_id) {
		this.action_id = action_id;
	}
	public int getResource_id() {
		return resource_id;
	}
	public void setResource_id(int resource_id) {
		this.resource_id = resource_id;
	}
	public int getDecision_id() {
		return decision_id;
	}
	public void setDecision_id(int decision_id) {
		this.decision_id = decision_id;
	}
	public int getSubject_id() {
		return subject_id;
	}
	public void setSubject_id(int subject_id) {
		this.subject_id = subject_id;
	}
	public int getRiskcommunication_id() {
		return riskcommunication_id;
	}
	public void setRiskcommunication_id(int riskcommunication_id) {
		this.riskcommunication_id = riskcommunication_id;
	}
	public long getModification() {
		return modification;
	}
	public void setModification(long modification) {
		this.modification = modification;
	}
	
	
}

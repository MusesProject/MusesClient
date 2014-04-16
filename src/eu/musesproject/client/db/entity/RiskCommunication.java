package eu.musesproject.client.db.entity;

public class RiskCommunication {
	
	private int id;
	private int communication_sequence;
	private int risktreatment_id;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCommunication_sequence() {
		return communication_sequence;
	}
	public void setCommunication_sequence(int communication_sequence) {
		this.communication_sequence = communication_sequence;
	}
	public int getRisktreatment_id() {
		return risktreatment_id;
	}
	public void setRisktreatment_id(int risktreatment_id) {
		this.risktreatment_id = risktreatment_id;
	}
	
	

}

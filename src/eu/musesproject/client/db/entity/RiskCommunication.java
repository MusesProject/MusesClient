package eu.musesproject.client.db.entity;

/*
 * #%L
 * MUSES Client
 * %%
 * Copyright (C) 2013 - 2014 Sweden Connectivity
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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

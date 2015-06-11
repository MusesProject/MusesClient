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

public class Decision {
	
	private int id;
	private String name;
	private String decision_id;
	private int solving_risktreatment;
	private long modification;
	private String condition;
	
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
	public long getModification() {
		return modification;
	}
	public void setModification(long modification) {
		this.modification = modification;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public String getDecision_id() {
		return decision_id;
	}
	public void setDecision_id(String decision_id) {
		this.decision_id = decision_id;
	}
	public int getSolving_risktreatment() {
		return solving_risktreatment;
	}
	public void setSolving_risktreatment(int solving_risktreatment) {
		this.solving_risktreatment = solving_risktreatment;
	}
	
	

}

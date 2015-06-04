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

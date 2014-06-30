package eu.musesproject.client.connectionmanager;
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
/**
 * This class is Request object to be to the server
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */

public class Request {
	
	private String type;
	private String url;
	private String pollInterval;
	private String data;
	
	
	/**
	 * Constructor initializes the request object
	 * @param type
	 * @param url
	 * @param pollInterval
	 * @param data
	 */
	public Request(String type, String url, String pollInterval, String data) {
		this.type = type;
		this.url = url;
		this.pollInterval = pollInterval;
		this.data = data;
	}

	/**
	 * Get request type
	 * @return type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Set request type
	 * @param type
	 * @return void
	 */
	
	public void setType(String type) {
		this.type = type;
	}

	/**Handle TLS/SSL communication with the s
	 * Convert poll interval to seconds
	 * @return pollInterval
	 */
	public String getPollIntervalInSeconds() {
		int pollIntervalInSeconds = (int) (Integer.parseInt(pollInterval) / 1000) ;
		pollInterval = Integer.toString(pollIntervalInSeconds);
		return pollInterval;
	}
	
	/**
	 * Get poll interval
	 * @return pollInterval
	 */
	
	public String getPollInterval() {
		return pollInterval;
	}
	
	/**
	 * Get server url
	 * @return url
	 */
	
	public String getUrl() {
		return url;
	}
	
	/**
	 * Set url
	 * @param url
	 * @return void
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Get data
	 * @return data
	 */
	
	public String getData() {
		return data;
	}
	
	/**
	 * Set data
	 * @param data
	 * @return void
	 */

	public void setData(String data) {
		this.data = data;
	}
	
	
}

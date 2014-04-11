/*
 * version 1.0 - MUSES prototype software
 * Copyright MUSES project (European Commission FP7) - 2013 
 * 
 */

package eu.musesproject.client.connectionmanager;

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

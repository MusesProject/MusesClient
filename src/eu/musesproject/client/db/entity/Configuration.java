package eu.musesproject.client.db.entity;

public class Configuration {
	
	private int id;
	private String serverIP;
	private int serverPort;
	private String serverContextPath;
	private String serverServletPath;
	private String serverCertificate;
	private String clientCertificate;
	private int timeout;
	private int pollTimeout;
	private int sleepPollTimeout;
	private int pollingEnabled;
	private int loginAttempts;
	private int silentMode;
	
	public Configuration() {
	}

	public Configuration(int id, String serverIP, int serverPort,
			String serverContextPath, String serverServletPath,
			String serverCertificate, String clientCertificate, int timeout,
			int pollTimeout, int sleepPollTimeout, int pollingEnabled,
			int loginAttempts, int silentMode) {
		super();
		this.id = id;
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.serverContextPath = serverContextPath;
		this.serverServletPath = serverServletPath;
		this.serverCertificate = serverCertificate;
		this.clientCertificate = clientCertificate;
		this.timeout = timeout;
		this.pollTimeout = pollTimeout;
		this.sleepPollTimeout = sleepPollTimeout;
		this.pollingEnabled = pollingEnabled;
		this.loginAttempts = loginAttempts;
		this.silentMode = silentMode;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getServerContextPath() {
		return serverContextPath;
	}

	public void setServerContextPath(String serverContextPath) {
		this.serverContextPath = serverContextPath;
	}

	public String getServerServletPath() {
		return serverServletPath;
	}

	public void setServerServletPath(String serverServletPath) {
		this.serverServletPath = serverServletPath;
	}

	public String getServerCertificate() {
		return serverCertificate;
	}

	public void setServerCertificate(String serverCertificate) {
		this.serverCertificate = serverCertificate;
	}

	public String getClientCertificate() {
		return clientCertificate;
	}

	public void setClientCertificate(String clientCertificate) {
		this.clientCertificate = clientCertificate;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getPollTimeout() {
		return pollTimeout;
	}

	public void setPollTimeout(int pollTimeout) {
		this.pollTimeout = pollTimeout;
	}

	public int getSleepPollTimeout() {
		return sleepPollTimeout;
	}

	public void setSleepPollTimeout(int sleepPollTimeout) {
		this.sleepPollTimeout = sleepPollTimeout;
	}

	public int getPollingEnabled() {
		return pollingEnabled;
	}

	public void setPollingEnabled(int pollingEnabled) {
		this.pollingEnabled = pollingEnabled;
	}

	public int getLoginAttempts() {
		return loginAttempts;
	}

	public void setLoginAttempts(int loginAttempts) {
		this.loginAttempts = loginAttempts;
	}

	public int getSilentMode() {
		return silentMode;
	}

	public void setSilentMode(int silentMode) {
		this.silentMode = silentMode;
	}

	@Override
	public String toString() {
		return "Configuration [id=" + id + ", serverIP=" + serverIP
				+ ", serverPort=" + serverPort + ", serverContextPath="
				+ serverContextPath + ", serverServletPath="
				+ serverServletPath + ", serverCertificate="
				+ serverCertificate + ", clientCertificate="
				+ clientCertificate + ", timeout=" + timeout + ", pollTimeout="
				+ pollTimeout + ", sleepPollTimeout=" + sleepPollTimeout
				+ ", pollingEnabled=" + pollingEnabled + ", loginAttempts="
				+ loginAttempts + ", silentMode=" + silentMode + "]";
	}

}

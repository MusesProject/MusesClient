package eu.musesproject.client.utils;

public class BuildConfig {

	public enum RUNNING_MODE {
		DEBUG,
		RELEASE,
		MOCK
	}
	
	public static final RUNNING_MODE CURRENT_RUNNING_MODE = RUNNING_MODE.RELEASE;
}


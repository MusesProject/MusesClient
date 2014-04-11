package eu.musesproject.client.contextmonitoring.service.aidl;


public class ServiceModel {
	
	private static ServiceModel instance;
	private MusesServiceProvider musesService;	
	
	public ServiceModel() {
	
	}
	
	public static ServiceModel getInstance() {
		if (instance == null)
			instance = new ServiceModel();
		return instance;

	}
	
	public void setServiceObject(MusesServiceProvider service) {
		musesService = service;
	}

	public MusesServiceProvider getService(){
		return musesService;
	}

	
}

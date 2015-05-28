package eu.musesproject.client.contextmonitoring.service.aidl;

interface IMusesServiceCallback {
	void onAccept(String response);
   	void onDeny(String response);
}
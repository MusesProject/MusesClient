package eu.musesproject.client.contextmonitoring.service.aidl;

import eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback;
import eu.musesproject.client.contextmonitoring.service.aidl.Action;

interface IMusesService {
	void sendUserAction(in Action action, in Map properties);
	void registerCallback(IMusesServiceCallback callback);
	void unregisterCallback(IMusesServiceCallback callback);
}
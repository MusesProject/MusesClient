package eu.musesproject.client.contextmonitoring;

import java.util.List;
import java.util.Map;

import eu.musesproject.client.contextmonitoring.service.aidl.Action;
import eu.musesproject.client.model.actuators.Setting;

public interface IUserContextMonitoringController {
	 void sendUserAction(Action action, Map<String, String> properties);
     void changeSettings(List<Setting> settings);
     void login(String userName, String password);
     void registerCallback(IUserContextMonitoringControllerCallback callback);
     void unregisterCallback(IUserContextMonitoringControllerCallback callback);
}
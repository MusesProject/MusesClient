package eu.musesproject.client.contextmonitoring;

import java.util.List;
import java.util.Map;

import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.actuators.Setting;
import eu.musesproject.contextmodel.ContextEvent;

public interface IUserContextMonitoringController {
    /**
     * Method that takes an {@link eu.musesproject.client.model.decisiontable.Action} and the
     * related properties of that action from a MUSES aware application. This method sends all information
     * to the {@link eu.musesproject.client.usercontexteventhandler.UserContextEventHandler}.
     * @param action {@link eu.musesproject.client.model.decisiontable.Action}. action received from a MUSES aware app
     * @param properties {@link java.util.Map}. properties related to the action
     */
    void sendUserAction(Action action, Map<String, String> properties);

    /**
     * Method to change the settings / configuration of the sensors
     * ({@link eu.musesproject.client.contextmonitoring.sensors.ISensor})
     * @param settings {@link java.util.List} of settings
     */
    void changeSettings(List<Setting> settings);

    /**
     * Method to forward the login data to the {@link eu.musesproject.client.usercontexteventhandler.UserContextEventHandler}
     * @param userName String. user name
     * @param password String. password
     */
    void login(String userName, String password);

    void registerCallback(IUserContextMonitoringControllerCallback callback);
    void unregisterCallback(IUserContextMonitoringControllerCallback callback);
}
package eu.musesproject.client.contextmonitoring;

import java.util.List;
import java.util.Map;

import android.content.Context;
import eu.musesproject.client.model.actuators.Setting;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.usercontexteventhandler.UserContextEventHandler;

/**
 * @author Christoph
 * @version 28 feb 2014
 *
 * Class to control the workflow of the user context monitoring architecture
 * component. The class is able to:
 *  - start the context observation
 *  - stop the context observation
 *  - send sensor settings/configuration coming from the MUSES UI to the sensor controller
 *  - handle incoming information from MUSES aware apps
 *  - handle incoming information from the MUSES UI
 */
public class UserContextMonitoringController implements
        IUserContextMonitoringController {
    private static UserContextMonitoringController ucmController = null;
    private IUserContextMonitoringControllerCallback callback;
    private final UserContextEventHandler handler = UserContextEventHandler.getInstance();

    private Context context;

    private UserContextMonitoringController(Context context) {
        this.context = context;
        handler.setContext(context);
    }

    public static UserContextMonitoringController getInstance(Context context) {
        if (ucmController == null) {
            ucmController = new UserContextMonitoringController(context);
        }
        return ucmController;
    }

    /**
     * starts every sensor for the context observation
     */
    public void startContextObservation() {
        SensorController.getInstance(context).startAllSensors();
    }

    /**
     * stops the context observation by
     * disabling every enabled sensor
     */
    public void stopContextObservation() {
        SensorController.getInstance(context).stopAllSensors();
    }

    @Override
    public void sendUserAction(Action action, Map<String, String> properties) {
        handler.send(action, properties, SensorController.getInstance(context).getLastFiredEvents());
    }

    @Override
    public void changeSettings(List<Setting> settings) {
        for (Setting setting : settings) {
            SensorController.getInstance(context).changeSetting(setting);
        }
    }

    @Override
    public void login(String userName, String password) {
        handler.login(userName, password);
    }

    @Override
    public void registerCallback(
            IUserContextMonitoringControllerCallback callback) {
        this.callback = callback;
    }

    @Override
    public void unregisterCallback(
            IUserContextMonitoringControllerCallback callback) {
        this.callback = callback;
    }

    public void sendLoginResponseToUI(boolean result) {
        callback.onLogin(result);
    }
}
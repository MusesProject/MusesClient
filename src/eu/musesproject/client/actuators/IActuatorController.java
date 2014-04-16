package eu.musesproject.client.actuators;

/**
 * Created by christophstanik on 4/16/14.
 */
public interface IActuatorController {
    void registerCallback(IUICallback callback);
    void unregisterCallback(IUICallback callback);
}
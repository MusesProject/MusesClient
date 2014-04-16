package eu.musesproject.client.actuators;

import eu.musesproject.client.model.decisiontable.Decision;

public interface IUICallback {
	void onLogin(boolean result);
    void onAccept();
    void onDeny(Decision decision);
    void onMaybe(Decision decision);
    void onUpToUser(Decision decision);
}
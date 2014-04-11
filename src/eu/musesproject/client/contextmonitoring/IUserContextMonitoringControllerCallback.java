package eu.musesproject.client.contextmonitoring;

import eu.musesproject.client.model.actuators.RiskTreatment;

public interface IUserContextMonitoringControllerCallback {
	void onLogin(boolean result);
    void onAccept(RiskTreatment riskTreatment );
    void onDeny(RiskTreatment riskTreatment );
}
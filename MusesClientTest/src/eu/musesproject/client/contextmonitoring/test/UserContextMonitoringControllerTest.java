package eu.musesproject.client.contextmonitoring.test;

import android.content.Context;
import android.test.AndroidTestCase;
import eu.musesproject.client.contextmonitoring.IUserContextMonitoringControllerCallback;
import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;
import eu.musesproject.client.model.actuators.RiskTreatment;

/**
 * Created by christophstanik on 3/11/14.
 */
public class UserContextMonitoringControllerTest extends AndroidTestCase {
    private Context context;

    private UserContextMonitoringController ucmController;

    private IUserContextMonitoringControllerCallback callback;

    private RiskTreatment riskTreatment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = getContext();
        ucmController =UserContextMonitoringController.getInstance(context);

        riskTreatment = new RiskTreatment(1, "text", 2);

    }
    public void testUCMControllerInitialization() {
        assertNotNull(ucmController);
    }

    public void testCallback() {
        callback = new IUserContextMonitoringControllerCallback() {
            @Override
            public void onLogin(boolean result) {
                assertEquals(false, result);
            }

            @Override
            public void onAccept(RiskTreatment riskTreatment) {
                assertEquals("risk level:", 1, riskTreatment.getRiskLevel());
                assertEquals("risk text:", "text", riskTreatment.getRiskTreatmentText());
                assertEquals("risk answer alternatives:", 2, riskTreatment.getAnswerAlternatives());
            }

            @Override
            public void onDeny(RiskTreatment riskTreatment) {
                assertEquals("risk level:", 1, riskTreatment.getRiskLevel());
                assertEquals("risk text:", "text", riskTreatment.getRiskTreatmentText());
                assertEquals("risk answer alternatives:", 2, riskTreatment.getAnswerAlternatives());
            }

            @Override
            public void onMaybe(RiskTreatment riskTreatment) {

            }

            @Override
            public void onUpToUser(RiskTreatment riskTreatment) {

            }
        };

        callback.onAccept(riskTreatment);
        callback.onDeny(riskTreatment);
        callback.onLogin(false);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
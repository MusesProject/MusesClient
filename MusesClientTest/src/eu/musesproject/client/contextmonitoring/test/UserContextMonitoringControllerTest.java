package eu.musesproject.client.contextmonitoring.test;

import android.content.Context;
import android.test.AndroidTestCase;
import eu.musesproject.client.actuators.IUICallback;
import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;
import eu.musesproject.client.model.decisiontable.Decision;

/**
 * Created by christophstanik on 3/11/14.
 */
public class UserContextMonitoringControllerTest extends AndroidTestCase {
    private Context context;

    private UserContextMonitoringController ucmController;

    private IUICallback callback;

    private Decision decision;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = getContext();
        ucmController =UserContextMonitoringController.getInstance(context);

        decision = new Decision();

    }
    public void testUCMControllerInitialization() {
        assertNotNull(ucmController);
    }

    public void testCallback() {
        callback = new IUICallback() {
            @Override
            public void onLogin(boolean result) {
                assertEquals(false, result);
            }

            @Override
            public void onAccept() {
                assertEquals("GRANTED", decision.getName());
            }

            @Override
            public void onDeny(Decision decision) {
                assertEquals("STRONG_DENY", decision.getName());
            }

            @Override
            public void onMaybe(Decision decision) {
                assertEquals("MAYBE", decision.getName());
            }

            @Override
            public void onUpToUser(Decision decision) {
                assertEquals("UP_TO_YOU", decision.getName());
            }
        };

        decision.setName(Decision.GRANTED_ACCESS);
        callback.onAccept();

        decision.setName(Decision.STRONG_DENY_ACCESS);
        callback.onDeny(decision);

        decision.setName(Decision.MAYBE_ACCESS_WITH_RISKTREATMENTS);
        callback.onMaybe(decision);


        decision.setName(Decision.UPTOYOU_ACCESS_WITH_RISKCOMMUNICATION);
        callback.onUpToUser(decision);

        callback.onLogin(false);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
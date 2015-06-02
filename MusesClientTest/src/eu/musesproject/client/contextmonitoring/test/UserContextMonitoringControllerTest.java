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
            public void onLogin(boolean result, String msg, int errorCode) {
                assertEquals(false, result);
            }
        };

        callback.onLogin(false, "user not authenticated", -1);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
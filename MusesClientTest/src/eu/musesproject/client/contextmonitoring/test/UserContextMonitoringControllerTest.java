package eu.musesproject.client.contextmonitoring.test;

/*
 * #%L
 * musesclient
 * %%
 * Copyright (C) 2013 - 2014 HITEC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
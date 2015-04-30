package eu.musesproject.client.contextmonitoring;

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
import android.util.Log;
import eu.musesproject.client.contextmonitoring.service.aidl.DummyCommunication;
import eu.musesproject.client.model.actuators.ResponseInfoAP;
import eu.musesproject.client.model.contextmonitoring.UISource;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.client.usercontexteventhandler.UserContextEventHandler;
import eu.musesproject.server.risktrust.RiskTreatment;

import java.util.Map;

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
    private static final String INTERNAL_SENSOR_TAG = "INTERNAL_SENSOR_TAG";
	private static UserContextMonitoringController ucmController = null;
    private final UserContextEventHandler uceHandler = UserContextEventHandler.getInstance();

    private Context context;

    private UserContextMonitoringController(Context context) {
        this.context = context;
        uceHandler.setContext(context);
        uceHandler.connectToServer();
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
        SensorController.getInstance(context).startSensors();
    }

    /**
     * stops the context observation by
     * disabling every enabled sensor
     */
    public void stopContextObservation() {
        SensorController.getInstance(context).stopAllSensors();
    }

    @Override
    public void sendUserAction(UISource src, Action action, Map<String, String> properties) {
        if(src == UISource.MUSES_UI) {
            // send the user decision back to the server
            sendUserBehavior(action);
        }
        else if(src == UISource.MUSES_AWARE_APP_UI) {
            Action musesAwareAction = action;
            musesAwareAction.setRequestedByMusesAwareApp(true);
            uceHandler.send(musesAwareAction, properties, SensorController.getInstance(context).getLastFiredEvents());
        }
        else if(src == UISource.INTERNAL) {
        	Log.d(INTERNAL_SENSOR_TAG, "action:"+action.getActionType()+ " properties:"+properties.toString());
            uceHandler.send(action, properties, SensorController.getInstance(context).getLastFiredEvents());
        }
    }


    @Override
    public void sendUserBehavior(Action action) {
        uceHandler.sendUserBehavior(action);
    }


    @Override
    public void onSensorConfigurationChanged() {
        SensorController.getInstance(context).onSensorConfigurationChanged();
    }

    @Override
    public void login(String userName, String password) {
        uceHandler.login(userName, password);
    }
}
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

import java.util.List;
import java.util.Map;

import eu.musesproject.client.model.contextmonitoring.UISource;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.actuators.Setting;

public interface IUserContextMonitoringController {
    /**
     * Method that takes an {@link eu.musesproject.client.model.decisiontable.Action} and the
     * related properties of that action from a MUSES aware application. This method sends all information
     * to the {@link eu.musesproject.client.usercontexteventhandler.UserContextEventHandler}.
     * @param action {@link eu.musesproject.client.model.decisiontable.Action}. action received from a MUSES aware app
     * @param properties {@link java.util.Map}. properties related to the action
     */
    void sendUserAction(UISource src, Action action, Map<String, String> properties);

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
}
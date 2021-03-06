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
import android.util.Log;
import eu.musesproject.client.connectionmanager.RequestHolder;
import eu.musesproject.client.contextmonitoring.sensors.AppSensor;
import eu.musesproject.client.db.entity.Configuration;
import eu.musesproject.client.db.entity.SensorConfiguration;
import eu.musesproject.client.model.JSONIdentifiers;
import eu.musesproject.client.model.RequestType;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.client.usercontexteventhandler.JSONManager;
import eu.musesproject.client.utils.MusesUtils;
import eu.musesproject.contextmodel.ContextEvent;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by christophstanik on 3/12/14.
 */
public class JSONManagerTest extends AndroidTestCase {
    private Context context;

    private Action action;
    private Map<String, String> actionProperties;
    private List<ContextEvent> contextEvents;

    private String deviceId;
    private String userName;
    private String password;
    private String requestType;
    private long actionTimestamp;
    private String actionType;
    private String userBehavior;
    private String successfulAuthenticationJSON;
    private String unSuccessfulAuthenticationJSON;
    private String responseJSON;
    private String configUpdateJSON;
    private String policyCondition;
    private String decisionId;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // create dummy data
        context = getContext();

        requestType = RequestType.UPDATE_CONTEXT_EVENTS;

        deviceId = "123456";
        userName = "muses";
        password = "muses";

        decisionId = "1";

        actionTimestamp = System.currentTimeMillis();
        actionType = ActionType.ACCESS;
        action = new Action();
        action.setActionType(actionType);
        action.setTimestamp(actionTimestamp);

        actionProperties = new HashMap<String, String>();
        actionProperties.put("protocol", "https");
        actionProperties.put("url", "https://");
        actionProperties.put("resourceid", "file.png");
        actionProperties.put("method", "post");

        ContextEvent contextEvent = new ContextEvent();
        contextEvent.setTimestamp(actionTimestamp);
        contextEvent.setType(AppSensor.TYPE);
        contextEvent.addProperty(AppSensor.PROPERTY_KEY_APP_NAME, "app");
        contextEvent.addProperty(AppSensor.PROPERTY_KEY_ID, "1");
        contextEvent.addProperty(AppSensor.PROPERTY_KEY_BACKGROUND_PROCESS, "process1,process2,process3");

        contextEvents = new ArrayList<ContextEvent>();
        contextEvents.add(contextEvent);

        userBehavior = ActionType.CANCEL;

        successfulAuthenticationJSON = "{\"auth-message\":\"Successfully authenticated\",\"auth-result\":\"SUCCESS\",\"requesttype\":\"auth-response\"}";
        unSuccessfulAuthenticationJSON = "{\"auth-message\":\"Incorrect password\",\"auth-result\":\"FAIL\",\"requesttype\":\"auth-response\"}";

        responseJSON = "{\"muses-device-policy\":{\"files\":{\"action\":{\"request_id\":-1627519220,\"deny\":{\"id\":0,\"condition\":{\"appname\":\"Wifi Analyzer\"},\"path\":\"Wifi Analyzer\",\"riskTreatment\":\"You are trying to open an application which is considered harmful.\nOther people can gain control over your device.\"},\"type\":\"open_application\"}},\"revision\":1,\"schema-version\":1},\"requesttype\":\"update_policies\"}";
        configUpdateJSON = "{\n \"sensor-configuration\": {\n \"sensor-property\": [\n {\n \"value\": \"avast! Mobile Security\",\n \"key\": \"trustedav\",\n \"sensor-type\": \"CONTEXT_SENSOR_DEVICE_PROTECTION\"\n },\n {\n \"value\": \"Mobile Security & Antivirus\",\n \"key\": \"trustedav\",\n \"sensor-type\": \"CONTEXT_SENSOR_DEVICE_PROTECTION\"\n },\n {\n \"value\": \"Avira Antivirus Security\",\n \"key\": \"trustedav\",\n \"sensor-type\": \"CONTEXT_SENSOR_DEVICE_PROTECTION\"\n },\n {\n \"value\": \"Norton Security & Antivirus\",\n \"key\": \"trustedav\",\n \"sensor-type\": \"CONTEXT_SENSOR_DEVICE_PROTECTION\"\n },\n {\n \"value\": \"CM Security & Find My Phone\",\n \"key\": \"trustedav\",\n \"sensor-type\": \"CONTEXT_SENSOR_DEVICE_PROTECTION\"\n },\n {\n \"value\": true,\n \"key\": \"enabled\",\n \"sensor-type\": \"CONTEXT_SENSOR_DEVICE_PROTECTION\"\n },\n {\n \"value\": 10,\n \"key\": \"mindistance\",\n \"sensor-type\": \"CONTEXT_SENSOR_LOCATION\"\n },\n {\n \"value\": 400,\n \"key\": \"mindtime\",\n \"sensor-type\": \"CONTEXT_SENSOR_LOCATION\"\n },\n {\n \"value\": 12,\n \"key\": \"radius\",\n \"sensor-type\": \"CONTEXT_SENSOR_LOCATION\"\n },\n {\n \"value\": true,\n \"key\": \"enabled\",\n \"sensor-type\": \"CONTEXT_SENSOR_LOCATION\"\n },\n {\n \"value\": \"/SWE/\",\n \"key\": \"path\",\n \"sensor-type\": \"CONTEXT_SENSOR_FILEOBSERVER\"\n },\n {\n \"value\": true,\n \"key\": \"enabled\",\n \"sensor-type\": \"CONTEXT_SENSOR_FILEOBSERVER\"\n },\n {\n \"value\": true,\n \"key\": \"enabled\",\n \"sensor-type\": \"CONTEXT_SENSOR_APP\"\n },\n {\n \"value\": true,\n \"key\": \"enabled\",\n \"sensor-type\": \"CONTEXT_SENSOR_CONNECTIVITY\"\n },\n {\n \"value\": true,\n \"key\": \"enabled\",\n \"sensor-type\": \"CONTEXT_SENSOR_INTERACTION\"\n },\n {\n \"value\": true,\n \"key\": \"enabled\",\n \"sensor-type\": \"CONTEXT_SENSOR_PACKAGE\"\n },\n {\n \"value\": true,\n \"key\": \"enabled\",\n \"sensor-type\": \"CONTEXT_SENSOR_SETTINGS\"\n },\n {\n \"value\": true,\n \"key\": \"enabled\",\n \"sensor-type\": \"CONTEXT_SENSOR_NOTIFICATION\"\n }\n ]\n },\n \"zone-config\": {\n \"zone\": [\n {\n \"description\": \"Office Valencia\",\n \"radius\": 500,\n \"latitude\": -0.349593,\n \"longitud\": 39.467912,\n \"zoneId\": 1\n },\n {\n \"description\": \"Office Madrid\",\n \"radius\": 500,\n \"latitude\": -3.682402,\n \"longitud\": 40.443132,\n \"zoneId\": 2\n }\n ]\n },\n \"connection-config\": {\n \"sleep_poll_timeout\": 60000,\n \"poll_timeout\": 10000,\n \"login_attempts\": 5,\n \"polling_enabled\": 1,\n \"timeout\": 5000\n },\n \"muses-config\": {\n \"config-name\": \"SILENT\",\n \"silent-mode\": true\n },\n \"requesttype\": \"config_update\"\n}";

        policyCondition = "{\\\"files\\\":{\\\"action\\\":{\\\"request_id\\\":289285980,\\\"deny\\\":{\\\"id\\\":0,\\\"condition\\\":{\\\"screenTimeoutInSeconds\\\":30},\\\"path\\\":\\\"device\\\",\\\"riskTreatment\\\":\\\"\\\"},\\\"type\\\":\\\"security_property_changed\\\"}},\\\"revision\\\":1,\\\"schema-version\\\":1},\\\"requesttype\\\":\\\"update_policies\\\"}";
    }

    public void testCreateJSON() throws JSONException {
        RequestHolder requestHolder = new RequestHolder(action, actionProperties, contextEvents);
        JSONObject resultJSON = JSONManager.createJSON(deviceId, userName, requestHolder.getId(), requestType, action, actionProperties, contextEvents);
        assertNotNull(resultJSON);

        assertEquals("request type", "update_context_events", resultJSON.getString(JSONIdentifiers.REQUEST_TYPE_IDENTIFIER));

        JSONObject actionJSON = resultJSON.getJSONObject(JSONIdentifiers.ACTION_IDENTIFIER);
        assertNotNull(actionJSON);
        assertEquals("action identifier in JSON :", actionType, actionJSON.getString(JSONIdentifiers.ACTION_TYPE));
        assertEquals("action timestamp:", String.valueOf(actionTimestamp), actionJSON.getString(JSONIdentifiers.ACTION_TIMESTAMP));

        JSONObject actionPropertiesJSON = actionJSON.getJSONObject(JSONIdentifiers.PROPERTIES_IDENTIFIER);
        assertNotNull(actionPropertiesJSON);
        assertEquals("property: protocol", "https", actionPropertiesJSON.getString("protocol"));
        assertEquals("property: url", "https://", actionPropertiesJSON.getString("url"));
        assertEquals("property: resourceid", "file.png", actionPropertiesJSON.getString("resourceid"));
        assertEquals("property: method", "post", actionPropertiesJSON.getString("method"));

        JSONObject sensorJSON = resultJSON.getJSONObject(JSONIdentifiers.SENSOR_IDENTIFIER);
        assertNotNull(sensorJSON);
        JSONObject appSensorJSONObject = sensorJSON.getJSONObject(AppSensor.TYPE);
        assertNotNull(appSensorJSONObject);
        assertEquals("app name:", "app", appSensorJSONObject.getString(AppSensor.PROPERTY_KEY_APP_NAME));
        assertEquals("app id:", "1", appSensorJSONObject.getString(AppSensor.PROPERTY_KEY_ID));
        assertEquals("app background processes:", "process1,process2,process3", appSensorJSONObject.getString(AppSensor.PROPERTY_KEY_BACKGROUND_PROCESS));
    }

    public void testCreateUserBehaviorJSON() throws JSONException {
        JSONObject resultJSON = JSONManager.createUserBehaviorJSON(deviceId, userName, userBehavior, decisionId);
        assertNotNull(resultJSON);

        assertEquals("request type", RequestType.USER_ACTION, resultJSON.getString(JSONIdentifiers.REQUEST_TYPE_IDENTIFIER));
        assertEquals("device id", deviceId, resultJSON.getString(JSONIdentifiers.AUTH_DEVICE_ID));
        assertEquals("user name", userName, resultJSON.getString(JSONIdentifiers.AUTH_USERNAME));

        JSONObject userBehaviorJSON = resultJSON.getJSONObject(JSONIdentifiers.USER_BEHAVIOR);
        assertNotNull(userBehaviorJSON);
        assertEquals("user behavior", userBehavior, userBehaviorJSON.getString(JSONIdentifiers.ACTION_IDENTIFIER));
        assertEquals("decision id", decisionId, resultJSON.getString(JSONIdentifiers.DECISION_IDENTIFIER));
    }

    public void testCreateLoginJSON() throws JSONException {
        JSONObject resultJSON = JSONManager.createLoginJSON(userName, password, deviceId);
        assertNotNull(resultJSON);

        assertEquals("request type", RequestType.LOGIN, resultJSON.getString(JSONIdentifiers.REQUEST_TYPE_IDENTIFIER));
        assertEquals("device id", deviceId, resultJSON.getString(JSONIdentifiers.AUTH_DEVICE_ID));
        assertEquals("password", password, resultJSON.getString(JSONIdentifiers.AUTH_PASSWORD));
        assertEquals("userName", userName, resultJSON.getString(JSONIdentifiers.AUTH_USERNAME));
    }

    public void testGetRequestType() {
        RequestHolder requestHolder = new RequestHolder(action, actionProperties, contextEvents);
        // create a test JSON object
        JSONObject testJSON = JSONManager.createJSON(deviceId, userName, requestHolder.getId(), requestType, action, actionProperties, contextEvents);
        assertNotNull(testJSON);

        assertEquals(requestType, JSONManager.getRequestType(testJSON.toString()));
    }

    public void testGetAuthResult() {
        assertEquals(true, JSONManager.getAuthResult(successfulAuthenticationJSON));
        assertEquals(false, JSONManager.getAuthResult(unSuccessfulAuthenticationJSON));
    }

    public void testGetRequestId() {
        assertEquals(-1627519220, JSONManager.getRequestId(responseJSON));
    }

    public void testIsSilentModeActivated() {
        assertEquals(true, JSONManager.isSilentModeActivated(configUpdateJSON));
    }

    public void testGetSensorConfig() {
        List<SensorConfiguration> configList = JSONManager.getSensorConfig(configUpdateJSON);
        assertEquals(20, configList.size());
        for (SensorConfiguration item : configList) {
            Log.d("JUnit", "key: " + item.getKey() + " value: " + item.getValue() + " sensorType: " + item.getSensorType());
        }
    }

    public void testAddZoneConfigIfExists() {
        List<SensorConfiguration> configList = JSONManager.addZoneConfigIfExists(configUpdateJSON);
        assertEquals(2, configList.size());
    }

    public void testGetConnectionConfiguration() {
        Configuration connectionConfig = JSONManager.getConnectionConfiguration(configUpdateJSON, context);
        assertNotNull(connectionConfig);
        assertEquals(connectionConfig.getServerIP(), MusesUtils.getMusesConf());
        assertEquals(connectionConfig.getServerPort(), 8443);
        assertEquals(connectionConfig.getServerServletPath(), "/commain");
        assertEquals(connectionConfig.getServerContextPath(), "/server");
        assertEquals(connectionConfig.getServerCertificate(), MusesUtils.getCertificateFromSDCard(context));
        assertEquals(connectionConfig.getClientCertificate(), "");
        assertEquals(connectionConfig.getTimeout(), 5000);
        assertEquals(connectionConfig.getPollTimeout(), 10000);
        assertEquals(connectionConfig.getSleepPollTimeout(), 60000);
        assertEquals(connectionConfig.getPollingEnabled(), 1);
        assertEquals(connectionConfig.getLoginAttempts(), 5);
    }

    public void testCreateConfigSyncJSON() {
        JSONObject configSyncRequest = JSONManager.createConfigSyncJSON("14121313", "5.0.2", "muses");
        Log.d("SYNC_TEST", configSyncRequest.toString());
        try {
            assertEquals(configSyncRequest.getString(JSONIdentifiers.REQUEST_TYPE_IDENTIFIER), RequestType.CONFIG_SYNC);
            assertEquals(configSyncRequest.getString(JSONIdentifiers.AUTH_DEVICE_ID), "14121313");
            assertEquals(configSyncRequest.getString(JSONIdentifiers.AUTH_USERNAME), "muses");
            assertEquals(configSyncRequest.getString(JSONIdentifiers.OPERATING_SYSTEM_VERSION), "5.0.2");
            assertEquals(configSyncRequest.getString(JSONIdentifiers.OPERATING_SYSTEM), "Android");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void testGetPolicyCondition() {
        String condition = JSONManager.getPolicyCondition(policyCondition);
        System.out.println(condition);
        Log.d("TEST", condition);
    }

    public void testGetAuthMessage() {
        assertEquals(JSONManager.getAuthMessage(successfulAuthenticationJSON), "Successfully authenticated");
        assertEquals(JSONManager.getAuthMessage(unSuccessfulAuthenticationJSON), "Incorrect password");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
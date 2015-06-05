package eu.musesproject.client.actuators;

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

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

import java.io.File;

/**
 * Created by Ahmed Saad on 6/5/15.
 * Modified by christophstanik
 */
public class ActuatorCommandAPI {
    private static final String TAG = ActuatorCommandAPI.class.getSimpleName();

    private Context context;

    public ActuatorCommandAPI(Context context) {
        this.context = context;
    }

    public void enableWifi() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
    }

    public void disableWifi() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
    }

    public void disableBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.disable();
        }
        else {
            Intent intentBluetooth = new Intent();
            intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            context.startActivity(intentBluetooth);
        }
    }

    public void enableBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.enable();
        }
        else {
            Intent intentBluetooth = new Intent();
            intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            context.startActivity(intentBluetooth);
        }
    }

    public void setScreenTimeOut(int screenTimeout) {
        android.provider.Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, screenTimeout);
    }


    public void navigateUserToAccessbilitySettings() {
        context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    public void navigateUserToAirPlaneMode() {
        context.startActivity(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS));
    }

    /**
     * Method to erase files from a given folder path.
     * The folder structure (sub-folders) are not deleted, just the files itself
     * @param folderPath
     */
    public void eraseFolderContent(String folderPath) {
        try {
            File[] folder = new File(folderPath).listFiles();
            for (File file : folder) {
                if (file.isDirectory()) {
                    eraseFolderContent(file.getAbsolutePath());
                } else if (file.isFile()) {
                    String fileName = file.getName();
                    boolean deleted = file.delete();
                    Log.d(TAG, fileName + " is deleted = " + deleted);
                }
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "path does not exist");
        }
    }
}
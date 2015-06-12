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

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

import java.io.File;

/**
 * Created by Ahmed Saad on 6/5/15.
 * Modified by christophstanik
 */
public class ActuatorCommandAPI implements IBlockActuator, IConnectionActuator, INavigationActuator,
        IFIleEraserActuator, IPreferenceActuator {
    private static final String TAG = ActuatorCommandAPI.class.getSimpleName();

    private Context context;

    public ActuatorCommandAPI(Context context) {
        this.context = context;
    }

    @Override
    public void enableWifi() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
    }

    @Override
    public void disableWifi() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
    }

    @Override
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

    @Override
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

    @Override
    public void setScreenTimeOut(int screenTimeout) {
        android.provider.Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, screenTimeout);
    }

    @Override
    public void navigateUserToAccessibilitySettings() {
        context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    @Override
    public void navigateUserToAirPlaneMode() {
        context.startActivity(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS));
    }

    @Override
    public void openOrInstallApp(String packageName) {
        /*
         * 1. Check if app is installed
         * 1.1 if installed open
         * 1.2 if not, open Market place
         */
        try {
            //1
            boolean isInstalled = false;
            PackageManager pm = context.getPackageManager();

            try {
                pm.getPackageInfo(packageName, PackageManager.GET_META_DATA);
                isInstalled = true;
            } catch (PackageManager.NameNotFoundException e) {
                // app is not installed
            }

            //1.1
            if (isInstalled) {
                context.startActivity(context.getPackageManager().getLaunchIntentForPackage(packageName));
            }
            // 1.2
            else {
                Intent goToMarket = new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("market://details?id="+packageName));
                context.startActivity(goToMarket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
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

    @Override
    public void block(String packageName) {
        /*
		 * 1. Change to home screen, because currently visible apps cannot be killed
		 * 2. Kill the app while it is in the background
		 */

        // 1.
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);

        // 3.
        activityManager.killBackgroundProcesses(packageName);
    }
}
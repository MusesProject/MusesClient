package eu.musesproject.client.utils;

/*
 * #%L
 * MUSES Client
 * %%
 * Copyright (C) 2013 - 2014 Sweden Connectivity
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class MusesUtils {
	static Context sContext;
	public static String getIMEINumberFromPhone(Context context) {
		String identifier = null;
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm != null)
			identifier = tm.getDeviceId();
		if (identifier == null || identifier.length() == 0)
			identifier = Secure.getString(context.getContentResolver(),
					Secure.ANDROID_ID);
		return identifier;
	}

	public static String getCertificateFromAssets(Context context) {
		String certificate = "";
		AssetManager assetManager = context.getAssets();
		InputStream input;
		try {
			input = assetManager.open("localhost.crt");

			int size = input.available();
			byte[] buffer = new byte[size];
			input.read(buffer);
			input.close();

			// byte buffer into a string
			certificate = new String(buffer);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return certificate;
			
	}
	
	public static File convertToFile(String buffer){
		File file = null;
		try {
			file = new File("localhost_m.crt");
			FileWriter writer = new FileWriter(file);
			writer.write(buffer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}
	
	public static Context getMusesAppContext(){
		return sContext;
	}
	
	public static void setAppContext(Context context){
		sContext = context;
	}
	
}


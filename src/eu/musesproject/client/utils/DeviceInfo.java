package eu.musesproject.client.utils;

import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class DeviceInfo {
	
	public static String getIMEINumberFromPhone(Context context){
		String identifier = null;
		TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm != null)
		      identifier = tm.getDeviceId();
		if (identifier == null || identifier .length() == 0)
		      identifier = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
		return identifier;
	}
}

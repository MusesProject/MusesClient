package eu.musesproject.client.application;

import android.app.Application;

import org.acra.*;
import org.acra.annotation.*;
import org.acra.ReportField;

@ReportsCrashes(
    formKey = "", // This is required for backward compatibility but not used
    mailTo = "muses_fb@swe-con.se",
    mode = ReportingInteractionMode.TOAST,
    resToastText = eu.musesproject.client.R.string.crash_toast_text,
    // excluded USER_CONTENT and CUSTOM_DATA
	customReportContent = {ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.STACK_TRACE, ReportField.LOGCAT },
    logcatArguments = { "-t", "100", "-v", "long", "ActivityManager:I", "*:I" }
)


public class MusesApplication extends Application {

	
	@Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}

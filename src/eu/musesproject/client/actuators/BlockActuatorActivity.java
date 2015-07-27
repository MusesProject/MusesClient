package eu.musesproject.client.actuators;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import eu.musesproject.client.R;

/**
 * This activity is used for the block actuator, as in Android you are just able to kill background processes
 * but no apps in the front. Therefore, we need to start an activity and put it in the front (so the activity we want to
 * block is in the background) and kill the process of the harmful activity/app.
 *
 * Starting the launcher app of the device, to but the harmful app in the background, was not always working, therefore
 * this activity is created
 *
 * Created by christophstanik on 7/27/15.
 */
public class BlockActuatorActivity extends Activity {
    public static final String KEY_PACKAGE_NAME = "key_package_name";
    private String packageName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.block_actuator_activity);
        Bundle bundle = getIntent().getExtras();
        packageName= bundle.getString(KEY_PACKAGE_NAME);

        if(packageName != null && !packageName.isEmpty()) {
            ActivityManager activityManager = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
            activityManager.killBackgroundProcesses(packageName);
        }
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK |
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(startMain);

        this.finish();
    }
}
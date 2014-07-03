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
import android.content.Context;
import android.content.Intent;


/**
 * Created by christophstanik on 7/3/14.
 *
 * Class to block specific apps
 */
public class BlockActuator implements IBlockActuator {
	private Context context;
	
	
	public BlockActuator(Context context) {
		this.context = context;
	}
	
	@Override
	public void block(String packageName) {
		/*
		 * 1. check if the current foreground app is still the app that should be killed
		 * 2. if so change to home screen, because currently visible apps cannot be killed
		 * 3. kill the app while it is in the background
		 */
		
		// 1. 
		final ActivityManager activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        ActivityManager.RunningTaskInfo foregroundTaskInfo = activityManager.getRunningTasks(1).get(0);

        String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
        if(foregroundTaskPackageName.equals(packageName)) {
			// 2.
			Intent startMain = new Intent(Intent.ACTION_MAIN);
		    startMain.addCategory(Intent.CATEGORY_HOME);
		    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    context.startActivity(startMain);

		    // 3.
		    activityManager.killBackgroundProcesses(foregroundTaskPackageName);
		}
    }
}
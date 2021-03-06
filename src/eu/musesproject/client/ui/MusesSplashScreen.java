package eu.musesproject.client.ui;

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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import eu.musesproject.client.R;
import eu.musesproject.client.utils.MusesUtils;

public class MusesSplashScreen extends Activity{

	private static final int TIME_OUT = 3000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.muses_splash);
        new Handler().postDelayed(new Runnable() {
           @Override
            public void run() {
                Intent i = new Intent(MusesSplashScreen.this, MainActivity.class);
                UIFileLog.write("Muses started");
                Log.d(MusesUtils.LOGIN_TAG, "Muses started");
                startActivity(i);
                finish();
            }
        }, TIME_OUT);
    }
}
	


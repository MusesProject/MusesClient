/*
 * version 1.0 - MUSES prototype software
 * Copyright MUSES project (European Commission FP7) - 2013 
 * 
 */

package eu.musesproject.client.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import eu.musesproject.MUSESBackgroundService;
import eu.musesproject.client.R;

/**
 * MainActivity class handles List buttons on the main GUI
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */

public class MainActivity extends Activity implements View.OnClickListener {

	private static String TAG = MainActivity.class.getSimpleName();
	private LinearLayout topLayout;
	private Button loginListBtn, securityInformationListbtn, privacyPolicyListBtn;
	private static Context context;
	private LoginView loginView;
	private SecurityInformationView securityInformationView;
	private PrivacyPolicyView privacyPolicyView;
	public static boolean isLoggedIn = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.muses_main);
		context = getApplicationContext();
		topLayout = (LinearLayout) findViewById(R.id.top_layout);
		loginListBtn = (Button)findViewById(R.id.login_list_button);
		securityInformationListbtn = (Button) findViewById( R.id.security_info_list_button);
		privacyPolicyListBtn = (Button) findViewById( R.id.policy_info_list_button);
		loginListBtn.setOnClickListener(this);
		securityInformationListbtn.setOnClickListener(this);
		privacyPolicyListBtn.setOnClickListener(this);

        // starts the background service of MUSES
        startService(new Intent(this, MUSESBackgroundService.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.login_list_button:
			topLayout.removeAllViews();
			loginView = new LoginView(context, this);
			topLayout.addView(loginView);
			break;
		case R.id.security_info_list_button:
			if (LoginView.isLoggedIn) {
				topLayout.removeAllViews();
				securityInformationView = new SecurityInformationView(context);
				topLayout.addView(securityInformationView);
			}
			break;
		case R.id.policy_info_list_button:
			if (LoginView.isLoggedIn) {
				topLayout.removeAllViews();
				privacyPolicyView = new PrivacyPolicyView(context);
				topLayout.addView(privacyPolicyView);
			}
			break;
		}
	}

    @Override
    public void onResume() {
        super.onResume();       
    }

	/**
	 * Shows the result dialog to the user
	 * @param message
	 */
	
	public void showResultDialog(String message){
		Log.d(TAG, "Showing result dialog");
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
		.setTitle("MUSES Policy Warning")
		.setMessage(message)
		.setCancelable(true)
		.setPositiveButton("Details", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		alertDialog.show();								
		
	}	
    
}
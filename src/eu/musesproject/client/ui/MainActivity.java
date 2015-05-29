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

import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import eu.musesproject.MUSESBackgroundService;
import eu.musesproject.client.R;
import eu.musesproject.client.actuators.ActuatorController;
import eu.musesproject.client.connectionmanager.Statuses;
import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;
import eu.musesproject.client.db.handler.DBManager;
import eu.musesproject.client.model.JSONIdentifiers;
import eu.musesproject.client.usercontexteventhandler.UserContextEventHandler;
import eu.musesproject.client.utils.MusesUtils;

/**
 * MainActivity class handles List buttons on the main GUI
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */

public class MainActivity extends Activity implements View.OnClickListener {

	public static final String DECISION_OK = "ok";
	public static final String DECISION_CANCEL = "cancel";
	public static final String DECISION_KEY = "decision";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String SAVE_CREDENTIALS = "save_credentials";
	public static final String PREFERENCES_KEY = "eu.musesproject.client";
	public static final String REGISTER_UI_CALLBACK = "eu.musesproject.client.action.CALLBACK";
	private static final String TAG = MainActivity.class.getSimpleName();
	private LinearLayout topLayout;
	private Button loginListBtn, securityQuizListbtn;
	private Context context;
	private LoginView loginView;
	private SecurityQuizView securityQuizView;
	private UserContextMonitoringController userContextMonitoringController;
	public static boolean isLoggedIn = false;
	private SharedPreferences prefs;
	private ProgressDialog progressDialog;
	private Timer autoUpdate;
	private int serverStatus = -1;
		
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null){
			if (bundle.getBoolean("is_from_service_restart")){
				registerCallbacks();
				finish();
			}
		}
		
		setContentView(R.layout.muses_main);
		context = getApplicationContext();
		getActionBar().setDisplayShowTitleEnabled(false);
		
		topLayout = (LinearLayout) findViewById(R.id.top_layout);
		loginListBtn = (Button) findViewById(R.id.login_list_button);
		securityQuizListbtn = (Button) findViewById(R.id.security_quiz_list_button);
		loginListBtn.setOnClickListener(this);
		securityQuizListbtn.setOnClickListener(this);

		userContextMonitoringController = UserContextMonitoringController
				.getInstance(context);
		
		registerCallbacks();
		
		prefs = context.getSharedPreferences(MainActivity.PREFERENCES_KEY,
				Context.MODE_PRIVATE);
		
		// starts the background service of MUSES
		startService(new Intent(this, MUSESBackgroundService.class));
		Log.v(TAG, "muses service started ...");
		
		loginView = new LoginView(context);
		securityQuizView = new SecurityQuizView(context);
		topLayout.removeAllViews();
		topLayout.addView(loginView);
		topLayout.addView(securityQuizView);

	}


	@Override
	protected void onPause() {
		super.onPause();
		autoUpdate.cancel();
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_list_button:
			topLayout.removeAllViews();
			topLayout.addView(loginView);
			break;
		case R.id.security_quiz_list_button:
			topLayout.removeAllViews();
			topLayout.addView(securityQuizView);
			break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		DBManager dbManager = new DBManager(getApplicationContext());
		dbManager.openDB();
		boolean isActive = dbManager.isSilentModeActive();
		dbManager.closeDB();
		if (!isActive) {
			if (loginView == null) {
				loginView = new LoginView(context);
			}
			
			if (securityQuizView == null){
				securityQuizView = new SecurityQuizView(context);
			}
			topLayout.removeAllViews();
			topLayout.addView(loginView);
			topLayout.addView(securityQuizView);
			
		}
		
		
		loginView.setServerStatus();
		
		
		autoUpdate = new Timer();
		  autoUpdate.schedule(new TimerTask() {
		   @Override
		   public void run() {
		    runOnUiThread(new Runnable() {
		     public void run() {
		    	 loginView.updateLoginWithNewServerStatus();
		     }
		    });
		   }
		  }, 6000, 30000); // updates each 30 secs
		
	}


	private Handler callbackHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MusesUICallbacksHandler.LOGIN_SUCCESSFUL:
                Log.e(TAG, msg.getData().get(JSONIdentifiers.AUTH_MESSAGE).toString());
				stopProgress();
				isLoggedIn = true;
				loginView.updateLoginView(true);
				securityQuizView.updateSecurityQuizView(true);
                toastMessage(msg.getData().get(JSONIdentifiers.AUTH_MESSAGE).toString());
				break;
			case MusesUICallbacksHandler.LOGIN_UNSUCCESSFUL:
                Log.e(TAG, msg.getData().get(JSONIdentifiers.AUTH_MESSAGE).toString());
				stopProgress();
				securityQuizView.updateSecurityQuizView(false);
				toastMessage(msg.getData().get(JSONIdentifiers.AUTH_MESSAGE).toString());
				break;
			}
		}

	};
	
	private void startProgress(){
		progressDialog = new ProgressDialog(MainActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setTitle(getResources().getString(
				R.string.logging_in));
		progressDialog.setMessage(getResources().getString(
				R.string.wait));
		progressDialog.setCancelable(true);
		progressDialog.show();
	}
	
	private void stopProgress(){
		if (progressDialog!=null){
			progressDialog.dismiss();
		}
	}

	/**
	 * Registers for callbacks using MusesUICallbacksHandler in
	 * UserContextMonitoringImplementation.
	 */
	private void registerCallbacks() {
		Log.v(MusesUtils.TEST_TAG, "Registring callbacks from MainActivity!");
		MusesUICallbacksHandler musesUICallbacksHandler = new MusesUICallbacksHandler(
				context, callbackHandler);
		ActuatorController.getInstance(this).registerCallback(
				musesUICallbacksHandler);
	}

	/**
	 * Toast messages to UI
	 * 
	 * @param message
	 */

	private void toastMessage(String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Check the login fields and Then tries login to the server
	 */

	public void doLogin(String userName, String password) {
		if (checkLoginInputFields(userName, password)) {
			startProgress();
			userContextMonitoringController.login(userName, password);
			
		} else {
			toastMessage(getResources().getString(
					R.string.empty_login_fields_msg));
		}
	}

	/**
	 * Check input fields are not empty before sending it for authentication
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */

	private boolean checkLoginInputFields(String userName, String password) {
		if (userName != null || password != null) { 
			if (userName.equals("") || password.equals("") )
				return false;								// FIXME need some new checking in future
		} else return false;
		return true;
	}

	/**
	 * LoginView class handles Login GUI (Username, passwords etc ) on the main
	 * GUI
	 * 
	 * @author Yasir Ali
	 * @version Jan 27, 2014
	 */

	private class LoginView extends LinearLayout implements
			View.OnClickListener, OnCheckedChangeListener {

		private EditText userNameTxt, passwordTxt;
		private LinearLayout /*loginLayout1,*/ loginLayout2;
		private Button loginBtn, logoutBtn;
		private TextView loginLabelTextView, loginDetailTextView;
		private CheckBox rememberCheckBox, agreeTermsCheckBox;
		private String userName, password;
		boolean isPrivacyPolicyAgreementChecked = false;
		boolean isSaveCredentialsChecked = false;
		
		public LoginView(Context context) {
			super(context);
			inflate(context, R.layout.login_view, this);
			loginLabelTextView = (TextView) findViewById(R.id.login_text_view);
			userNameTxt = (EditText) findViewById(R.id.username_text);
			passwordTxt = (EditText) findViewById(R.id.pass_text);
			userName = userNameTxt.getText().toString();
			password = passwordTxt.getText().toString();
			loginDetailTextView = (TextView) findViewById(R.id.login_detail_text_view);
			rememberCheckBox = (CheckBox) findViewById(R.id.remember_checkbox);
			rememberCheckBox.setOnCheckedChangeListener(this);
			agreeTermsCheckBox = (CheckBox) findViewById(R.id.agree_terms_checkbox);
			agreeTermsCheckBox.setOnCheckedChangeListener(this);
			//loginLayout1 = (LinearLayout) findViewById(R.id.login_layout_1);
			loginLayout2 = (LinearLayout) findViewById(R.id.login_layout_2);
			loginBtn = (Button) findViewById(R.id.login_button);
			loginBtn.setOnClickListener(this);
			logoutBtn = (Button) findViewById(R.id.logout_button);
			logoutBtn.setOnClickListener(this);
			setUsernamePasswordIfSaved();
			populateLoggedInView();
		}

		/**
		 * Populate logged in view if user is successfully logged in.
		 * 
		 */

		private void populateLoggedInView() {
			if (isLoggedIn != UserContextEventHandler.getInstance().isUserAuthenticated()) {
				Log.d(TAG, "isLoggedIn status mismatch, GUI: "+(isLoggedIn?"true":"false")+" Service: "+(UserContextEventHandler.getInstance().isUserAuthenticated()?"true":"false"));
				isLoggedIn = UserContextEventHandler.getInstance().isUserAuthenticated();
			}
			
			if (isLoggedIn) {
				loginLayout2.setVisibility(View.GONE);
				logoutBtn.setVisibility(View.VISIBLE);
				loginDetailTextView.setText(String.format("%s %s", getResources()
						.getString(R.string.logged_in_info_txt), userNameTxt.getText().toString()));
			}
		}

		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			switch (arg0.getId()) {
			case R.id.remember_checkbox:
				SharedPreferences.Editor prefEditor = prefs.edit();	
				if (isChecked){
				    isSaveCredentialsChecked = true;
				} else { 
					isSaveCredentialsChecked = false;
					prefEditor.clear();
					prefEditor.putBoolean(SAVE_CREDENTIALS, false);
					prefEditor.commit();
				}
				break;
			case R.id.agree_terms_checkbox:
				if (isChecked){
					isPrivacyPolicyAgreementChecked = true;
				} else isPrivacyPolicyAgreementChecked = false;
				break;
			}
		}

		/**
		 * Handles all the button on the screen, overridden method for
		 * onClickLitsener
		 * 
		 * @param View
		 * 
		 */

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.login_button:
				if (isPrivacyPolicyAgreementChecked){
					userName = userNameTxt.getText().toString();
					password = passwordTxt.getText().toString();
					doLogin(userName, password);
				} else {
					toastMessage(getResources().getString(R.string.make_sure_privacy_policy_read_txt));
				}
				break;
			case R.id.logout_button:
				UserContextEventHandler.getInstance().logout();
				securityQuizView.updateSecurityQuizView(false);
				logoutBtn.setVisibility(View.GONE);
				loginDetailTextView.setText(getResources().getString(
						R.string.login_detail_view_txt));
				loginLayout2.setVisibility(View.VISIBLE);
				toastMessage(getResources().getString(
						R.string.logout_successfully_msg));
				isLoggedIn = false;
				setUsernamePasswordIfSaved();
				break;
			}

		}

		public void updateLoginView(Boolean loginSuccess) {
			
			if (loginSuccess){
				userName = userNameTxt.getText().toString();
				password = passwordTxt.getText().toString();
				SharedPreferences.Editor prefEditor = prefs.edit();	
				if (isSaveCredentialsChecked){
					
					prefEditor.putString(USERNAME, userName);
					prefEditor.putString(PASSWORD, password);
					prefEditor.putBoolean(SAVE_CREDENTIALS, isSaveCredentialsChecked);
					prefEditor.commit();
					
				}
			}
			else {	
				setUsernamePasswordIfSaved();
			}
			
			loginLayout2.setVisibility(View.GONE);
			logoutBtn.setVisibility(View.VISIBLE);
			loginDetailTextView.setText(String.format("%s %s", getResources()
					.getString(R.string.logged_in_info_txt), userNameTxt.getText().toString()));
			
			
			setServerStatus();
			loginLabelTextView.setFocusable(true);
			loginLabelTextView.requestFocus();
			
		}
		
		private void setServerStatus() {
			if (isLoggedIn) {
				serverStatus = Statuses.CURRENT_STATUS;
				String detailedText = String.format("%s %s", getResources()
						.getString(R.string.logged_in_info_txt), userNameTxt.getText().toString());
					detailedText += "\n" + getResources().getString(R.string.current_com_status_pre);
					detailedText += serverStatus == Statuses.ONLINE ? getResources().getString(R.string.current_com_status_2):
						getResources().getString(R.string.current_com_status_3);
					loginDetailTextView.setText(detailedText);
			}
		}

		private void updateLoginWithNewServerStatus(){
			if (serverStatus != Statuses.CURRENT_STATUS ) {
				serverStatus = Statuses.CURRENT_STATUS;
				/* Not showing status in login screen */
				if (isLoggedIn) {
					setServerStatus();
				}
			}
		}

		
		public void setUsernamePasswordIfSaved(){
			if (prefs.contains(USERNAME)) {
				userName = prefs.getString(USERNAME, "");
				password = prefs.getString(PASSWORD, "");
				userNameTxt.setText(userName);
				passwordTxt.setText(password);
				
			} else {
				userNameTxt.setText("");
				passwordTxt.setText("");
				
				Log.d(TAG, "No username-pass found in preferences");
			}
			
			// Set rememberCheckBox, if no choice done default to true
			isSaveCredentialsChecked = prefs.getBoolean(SAVE_CREDENTIALS, false);
			rememberCheckBox.setChecked(isSaveCredentialsChecked);
			
		}
		
		
		
	}

	/**
	 * SecurityInformationView class shows security information on the main GUI
	 * 
	 * @author Yasir Ali
	 * @version Jan 27, 2014
	 */

	private class SecurityQuizView extends LinearLayout implements 
					View.OnClickListener {

		private TextView securityQuizTextView;

		public SecurityQuizView(Context context) {
			super(context);
			inflate(context, R.layout.security_quiz, this);
			securityQuizTextView = (TextView) findViewById(R.id.security_quiz_txtView);
			securityQuizTextView.setOnClickListener(this);
		}
		
		public void updateSecurityQuizView(Boolean loginSuccess) {
			
			if (loginSuccess) {
				securityQuizTextView.setVisibility(View.VISIBLE);
			}
			else {	
				securityQuizTextView.setVisibility(View.GONE);
			}
			
		}
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.security_quiz_txtView:
				startSecurityQuiz();
				break;
			}
		}

	}

	public void startSecurityQuiz() {
		Log.d(TAG, "Taking user to security quiz.");

        String userName = prefs.getString(USERNAME, "");
        String password = prefs.getString(PASSWORD, "");

        String finalUrl = "javascript:" +
                "var to = 'https://muses-securityquizz.rhcloud.com/LoginServlet';" +
                "var p = {j_username:'"+userName+"',j_password:'"+password+"'};"+
                "var myForm = document.createElement('form');" +
                "myForm.method='post' ;" +
                "myForm.action = to;" +
                "for (var k in p) {" +
                "var myInput = document.createElement('input') ;" +
                "myInput.setAttribute('type', 'text');" +
                "myInput.setAttribute('name', k) ;" +
                "myInput.setAttribute('value', p[k]);" +
                "myForm.appendChild(myInput) ;" +
                "}" +
                "document.body.appendChild(myForm) ;" +
                "myForm.submit() ;" +
                "document.body.removeChild(myForm) ;";
        Intent browserIntent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse(finalUrl));
        startActivity(browserIntent);
		
	}

}
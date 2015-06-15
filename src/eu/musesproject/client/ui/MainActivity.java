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
import org.apache.http.util.EncodingUtils;
import android.app.AlertDialog;
import android.content.DialogInterface;

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
import java.util.List;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String IS_MUSES_SERVICE_INITIALIZED = "is_muses_service_initialized";
	private static final String IS_LOGGED_IN = "is_logged_in";
	private static final String APP_TAG = "APP_TAG";
	public static final String SELECTED_LAYOUT = "selected_layout";
	private LinearLayout topLayout;
	private Button loginListBtn, informationSecurityBehaviourListbtn, securityQuizListbtn, statisticsListButton;
	private ScrollView mainScrollView;
	private Context context;
	private LoginView loginView;
	private InformationSecurityBehaviourView informationSecurityBehaviourView;
	private SecurityQuizView securityQuizView;
	private StatisticsView statisticsView;
	private UserContextMonitoringController userContextMonitoringController;
	public static boolean isLoggedIn = false;
	public static boolean isMUSESServiceInitialized = false;
	private SharedPreferences prefs;
	private ProgressDialog progressDialog;
	private Timer autoUpdate;
	private int serverStatus = -1;
    protected WebView webview;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(MusesUtils.LOGIN_TAG, "onCreate called in MainActivity");
		Bundle bundle = getIntent().getExtras();
		if (bundle != null){
			if (bundle.getBoolean("is_from_service_restart")){
				Log.d(MusesUtils.LOGIN_TAG, "from service restart, register callback and finish activity");
				registerCallbacks();
				finish();
			}
		}
		
		setContentView(R.layout.muses_main);
		context = getApplicationContext();
		setActionBarTitle(getString(R.string.action_bar_name));
		getActionBar().setDisplayShowTitleEnabled(true);

		topLayout = (LinearLayout) findViewById(R.id.top_layout);
		loginListBtn = (Button) findViewById(R.id.login_list_button);
		informationSecurityBehaviourListbtn = (Button) findViewById(R.id.info_security_behaviour_list_button);
		securityQuizListbtn = (Button) findViewById(R.id.security_quiz_list_button);
		statisticsListButton = (Button) findViewById(R.id.statistics_list_button);
		
		mainScrollView = (ScrollView) findViewById(R.id.main_scroll_view);
		
		loginListBtn.setOnClickListener(this);
		informationSecurityBehaviourListbtn.setOnClickListener(this);
		securityQuizListbtn.setOnClickListener(this);
		statisticsListButton.setOnClickListener(this);
		
		loginListBtn.setSelected(true);
		
		userContextMonitoringController = UserContextMonitoringController
				.getInstance(context);
		
		registerCallbacks();
		
		prefs = context.getSharedPreferences(MainActivity.PREFERENCES_KEY,
				Context.MODE_PRIVATE);
		
		// starts the background service of MUSES
		isMUSESServiceInitialized = isMUSESServiceInitializedInPrefs();
		
		
		startService(new Intent(this, MUSESBackgroundService.class));
		Log.v(MusesUtils.LOGIN_TAG, "muses service started ... from MainActivity");
		Log.v(APP_TAG, "muses service started ... from MainActivity");
		if (!isMUSESServiceInitialized) { // If not initialized
			// FIXME onCreate can be called because of screen orientation need to check if the service is not running then restart it-
	//		setMUSESServiceInitializedInPrefs(); 
		}

		loginView = new LoginView(context);
		informationSecurityBehaviourView = new InformationSecurityBehaviourView(context);
		securityQuizView = new SecurityQuizView(context);
		statisticsView = new StatisticsView(context);
		
		topLayout.removeAllViews();
		topLayout.addView(loginView);

	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(MusesUtils.LOGIN_TAG, "onResume called in MainActivity");
		DBManager dbManager = new DBManager(getApplicationContext());
		dbManager.openDB();
		boolean isActive = dbManager.isSilentModeActive();
		dbManager.closeDB();
		if (!isActive) {
			// FIXME what should be done here??
			
//			topLayout.removeAllViews();
//			topLayout.addView(loginView);
//			topLayout.addView(securityQuizView);
		}
		
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

		if (loginView == null) {
			Log.v(MusesUtils.LOGIN_TAG, "login view is null, creating new view");
			loginView = new LoginView(context);
		}
		if (informationSecurityBehaviourView == null) {
			Log.v(MusesUtils.LOGIN_TAG, "informationSecurityBehaviourView is null, creating new view");
			informationSecurityBehaviourView = new InformationSecurityBehaviourView(context);
		}

		if (securityQuizView == null){
			Log.v(MusesUtils.LOGIN_TAG, "security view is null, creating new view");
			securityQuizView = new SecurityQuizView(context);
		}
		if (statisticsView == null){
			Log.v(MusesUtils.LOGIN_TAG, "statistics view is null, creating new view");
			statisticsView = new StatisticsView(context);
		}

		isLoggedIn = checkIfLoggedInPrefs();
		Log.d(MusesUtils.LOGIN_TAG, "isloggedin: "+isLoggedIn);
		Log.d(MusesUtils.LOGIN_TAG, "isloggedin in UserContextEventHandler: "+ UserContextEventHandler.getInstance().isUserAuthenticated());

		updateViews();
		  
	}

	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(MusesUtils.LOGIN_TAG, "onSaveInstanceState called in MainActivity");
		if (loginListBtn.isSelected()){
			outState.putString(SELECTED_LAYOUT, "login_view");
		}
		if (informationSecurityBehaviourListbtn.isSelected()){
			outState.putString(SELECTED_LAYOUT, "info_sec_view");
		}
		if (securityQuizListbtn.isSelected()){
			outState.putString(SELECTED_LAYOUT, "sec_quiz_view");
		}
		if (statisticsListButton.isSelected()){
			outState.putString(SELECTED_LAYOUT, "stats_view");
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d(MusesUtils.LOGIN_TAG, "onRestoreInstanceState called in MainActivity");
		if (savedInstanceState != null) {
			String selectedLayout = savedInstanceState.getString(SELECTED_LAYOUT,"login_view");
			if (selectedLayout.equals("login_view")){
				loginListBtn.setSelected(true);
				informationSecurityBehaviourListbtn.setSelected(false);
				securityQuizListbtn.setSelected(false);
				statisticsListButton.setSelected(false);
				
				topLayout.removeAllViews();
				topLayout.addView(loginView);
			}else if (selectedLayout.equals("info_sec_view")){
				loginListBtn.setSelected(false);
				informationSecurityBehaviourListbtn.setSelected(true);
				securityQuizListbtn.setSelected(false);
				statisticsListButton.setSelected(false);
				
				topLayout.removeAllViews();
				topLayout.addView(informationSecurityBehaviourView);
			} else if (selectedLayout.equals("sec_quiz_view")){
				loginListBtn.setSelected(false);
				informationSecurityBehaviourListbtn.setSelected(false);
				securityQuizListbtn.setSelected(true);
				statisticsListButton.setSelected(false);
				
				topLayout.removeAllViews();
				topLayout.addView(securityQuizView);
			}else if (selectedLayout.equals("stats_view")){
				loginListBtn.setSelected(false);
				informationSecurityBehaviourListbtn.setSelected(false);
				securityQuizListbtn.setSelected(false);
				statisticsListButton.setSelected(true);
				
				topLayout.removeAllViews();
				topLayout.addView(statisticsView);
			}
		}
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		autoUpdate.cancel();
		Log.d(MusesUtils.LOGIN_TAG, "onPause called in MainActivity");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(MusesUtils.LOGIN_TAG, "onDestroy called in MainActivity");
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_list_button:
			loginListBtn.setSelected(true);
			informationSecurityBehaviourListbtn.setSelected(false);
			securityQuizListbtn.setSelected(false);
			statisticsListButton.setSelected(false);
			
			topLayout.removeAllViews();
			topLayout.addView(loginView);
			break;
		case R.id.info_security_behaviour_list_button:
			informationSecurityBehaviourListbtn.setSelected(true);
			loginListBtn.setSelected(false);
			securityQuizListbtn.setSelected(false);
			statisticsListButton.setSelected(false);
			
			topLayout.removeAllViews();
			topLayout.addView(informationSecurityBehaviourView);
			break;
		case R.id.security_quiz_list_button:
			securityQuizListbtn.setSelected(true);
			loginListBtn.setSelected(false);
			informationSecurityBehaviourListbtn.setSelected(false);
			statisticsListButton.setSelected(false);
			
			topLayout.removeAllViews();
			topLayout.addView(securityQuizView);
			break;
		case R.id.statistics_list_button:
			statisticsListButton.setSelected(true);
			loginListBtn.setSelected(false);
			informationSecurityBehaviourListbtn.setSelected(false);
			securityQuizListbtn.setSelected(false);

			topLayout.removeAllViews();
			topLayout.addView(statisticsView);
			break;
		}
	}


	/**
	 * Update layouts with user actions
	 */
	
	private void updateViews() {
		loginView.updateLoginView();
		informationSecurityBehaviourView.updateInformationSecurityBehaviourView();
		securityQuizView.updateSecurityQuizView();
		statisticsView.updateStatisticsView();
		
	}
	
	private Handler callbackHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MusesUICallbacksHandler.LOGIN_SUCCESSFUL:
                Log.e(TAG, msg.getData().get(JSONIdentifiers.AUTH_MESSAGE).toString());
				stopProgress();
				isLoggedIn = true;
				updateLoginInPrefs(true);

				updateViews();
				toastMessage(msg.getData().get(JSONIdentifiers.AUTH_MESSAGE).toString());
				break;
			case MusesUICallbacksHandler.LOGIN_UNSUCCESSFUL:
                Log.e(TAG, msg.getData().get(JSONIdentifiers.AUTH_MESSAGE).toString());
				stopProgress();
				isLoggedIn = false;
				updateLoginInPrefs(false);
				
				updateViews();
				toastMessage(msg.getData().get(JSONIdentifiers.AUTH_MESSAGE).toString());
				break;
			default:  // No need to handle all error code right now, as we will a fixed message, but can be used in future
				Log.v(MusesUtils.LOGIN_TAG, "Unknown Error!, updating prefs..");
				stopProgress();
				isLoggedIn = false;
				updateLoginInPrefs(false);
				
				updateViews();
				toastMessage(getResources().getString(R.string.unknown_error_toast_text));
				break;
			}
		}

	};
	
	/**
	 * Starts the progress bar when user try to login
	 */
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
	
	/**
	 * Stops the progress bar when a reply is received from server
	 */
	
	private void stopProgress(){
		if (progressDialog != null){
			progressDialog.dismiss();
		}
	}

	/**
	 * Registers for callbacks using MusesUICallbacksHandler in
	 * UserContextMonitoringImplementation.
	 */
	private void registerCallbacks() {
		Log.v(MusesUtils.LOGIN_TAG, "Registring callbacks from MainActivity!");
		MusesUICallbacksHandler musesUICallbacksHandler = new MusesUICallbacksHandler(
				context, callbackHandler);
		ActuatorController.getInstance(this).registerCallback(
				musesUICallbacksHandler);
	}


	public void setActionBarTitle(final String title) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActionBar().setTitle(title);
			}
		});
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
			toastMessage(getResources().getString(R.string.empty_login_fields_msg));
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

	private void setMUSESServiceInitializedInPrefs(){
		SharedPreferences.Editor prefEditor = prefs.edit();	
		prefEditor.putBoolean(IS_MUSES_SERVICE_INITIALIZED,true);
		prefEditor.commit();
		Log.d(MusesUtils.LOGIN_TAG, "MUSES_SERVICE_INIT_FLAG set in preferences");
	}
	
	private boolean isMUSESServiceInitializedInPrefs(){
		if (prefs.contains(IS_MUSES_SERVICE_INITIALIZED)) {
			return prefs.getBoolean(IS_MUSES_SERVICE_INITIALIZED,false);
		} else {
			Log.d(MusesUtils.LOGIN_TAG, "No MUSES_SERVICE_INIT_FLAG found in preferences");
		}
		return false;
	}
	
	private void updateLoginInPrefs(boolean value) {
		SharedPreferences.Editor prefEditor = prefs.edit();	
		prefEditor.putBoolean(IS_LOGGED_IN,value);
		prefEditor.commit();
		Log.d(MusesUtils.LOGIN_TAG, "IS_LOGGED_IN set in preferences with value: "+value);
	}
	
	private boolean checkIfLoggedInPrefs(){
		if (prefs.contains(IS_LOGGED_IN)) {
			return prefs.getBoolean(IS_LOGGED_IN,false);
		} else {
			Log.d(MusesUtils.LOGIN_TAG, "No IS_LOGGED_IN found in preferences");
		}
		return false;
		
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
		private LinearLayout loginLayout;
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
			loginLayout = (LinearLayout) findViewById(R.id.login_layout_2);
			loginBtn = (Button) findViewById(R.id.login_button);
			loginBtn.setOnClickListener(this);
			logoutBtn = (Button) findViewById(R.id.logout_button);
			logoutBtn.setOnClickListener(this);
			setUsernamePasswordIfSaved();
			updateLoginView();
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
				} else {
					isPrivacyPolicyAgreementChecked = false;
				}
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
					hideKeyboard();
					doLogin(userName, password);
					saveUserPasswordInPrefs();
				} else {
					toastMessage(getResources().getString(R.string.make_sure_privacy_policy_read_txt));
				}
				break;
			case R.id.logout_button:
				UserContextEventHandler.getInstance().logout();
				break;
			}

		}

		public void updateLoginView() {
			
			if (isLoggedIn){
				Log.v(MusesUtils.LOGIN_TAG, "login success in, updating login");
				loginLayout.setVisibility(View.GONE);
				logoutBtn.setVisibility(View.VISIBLE);
				loginDetailTextView.setText(String.format("%s %s", getResources()
						.getString(R.string.logged_in_info_txt), userNameTxt.getText().toString()));
				setServerStatus();
				loginLabelTextView.setFocusable(true);
				loginLabelTextView.requestFocus();
			} else { // FIXME check below logs, why remote auth is checked here?	
				Log.d(TAG, "isLoggedIn status mismatch, GUI: "+(isLoggedIn?"true":"false")+" Service: "+(UserContextEventHandler.getInstance().isUserAuthenticated()?"true":"false"));
				Log.d(MusesUtils.LOGIN_TAG, "isLoggedIn status mismatch, GUI: "+(isLoggedIn?"true":"false")+" Service: "+(UserContextEventHandler.getInstance().isUserAuthenticated()?"true":"false"));
				//isLoggedIn = UserContextEventHandler.getInstance().isUserAuthenticated();
				logoutBtn.setVisibility(View.GONE);
				loginDetailTextView.setText(getResources().getString(
						R.string.login_detail_view_txt));
				loginLayout.setVisibility(View.VISIBLE);
				setUsernamePasswordIfSaved();
			}
			
		}
		
		private void setServerStatus() {
			serverStatus = Statuses.CURRENT_STATUS;
			String detailedText = String.format("%s %s", getResources()
					.getString(R.string.logged_in_info_txt), userNameTxt.getText().toString());
			detailedText += "\n" + getResources().getString(R.string.current_com_status_pre);
			detailedText += serverStatus == Statuses.ONLINE ? getResources().getString(R.string.current_com_status_2):
				getResources().getString(R.string.current_com_status_3);
			loginDetailTextView.setText(detailedText);
		}

		private void updateLoginWithNewServerStatus(){
			if (serverStatus != Statuses.CURRENT_STATUS ) {
				serverStatus = Statuses.CURRENT_STATUS;
				/* Not showing status in login screen */
				if (isLoggedIn) {
					Log.v(MusesUtils.LOGIN_TAG, "logged in, updating server status");
					setServerStatus();
				}
			}
		}

		private void hideKeyboard(){
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(logoutBtn.getWindowToken(), 
                                      InputMethodManager.RESULT_UNCHANGED_SHOWN);
            if (mainScrollView != null) {
            	mainScrollView.scrollTo(0, mainScrollView.getBaseline());
            }
		}

		private void saveUserPasswordInPrefs(){
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
		
		public void setUsernamePasswordIfSaved(){
			if (prefs.contains(USERNAME)) {
				userName = prefs.getString(USERNAME, "");
				password = prefs.getString(PASSWORD, "");
				userNameTxt.setText(userName);
				passwordTxt.setText(password);
				
			} else {
				userNameTxt.setText("");
				passwordTxt.setText("");
				Log.d(MusesUtils.LOGIN_TAG, "No username-pass found in preferences");
			}
			
			// Set rememberCheckBox, if no choice done default to true
			isSaveCredentialsChecked = prefs.getBoolean(SAVE_CREDENTIALS, false);
			rememberCheckBox.setChecked(isSaveCredentialsChecked);
			
		}
		
		
	}
	
	/**
	 * Information on Security Behaviour class shows information about user behaviour
	 * 
	 * @author Yasir Ali
	 * @version Jan 27, 2014
	 */

	private class InformationSecurityBehaviourView extends LinearLayout {

		private WebView infoSecurityBehaviourWebView;

		public InformationSecurityBehaviourView(Context context) {
			super(context);
			inflate(context, R.layout.info_sec_view, this);
			infoSecurityBehaviourWebView = (WebView) findViewById(R.id.info_sec_webview);
			updateInformationSecurityBehaviourView();
		}
		
		public void updateInformationSecurityBehaviourView() {
			Log.d(TAG, "Nothing to update for time being in webview.");
	        final String mimeType = "text/html";
	        final String encoding = "UTF-8";
	        String html =getResources().getString(R.string.info_sec_txt_webview);
	        infoSecurityBehaviourWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
	        infoSecurityBehaviourWebView.loadDataWithBaseURL("", html, mimeType, encoding, "");
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
		private Button securityQuizButton;
		
		public SecurityQuizView(Context context) {
			super(context);
			inflate(context, R.layout.security_quiz, this);
			securityQuizTextView = (TextView) findViewById(R.id.security_quiz_txtView);
			securityQuizButton = (Button) findViewById(R.id.sec_quiz_button);
			securityQuizButton.setOnClickListener(this);
		}
		
		public void updateSecurityQuizView() {
			
			if (isLoggedIn) {
				securityQuizTextView.setVisibility(View.GONE);
				securityQuizButton.setVisibility(View.VISIBLE);
			} else {
				securityQuizTextView.setVisibility(View.VISIBLE);
				securityQuizButton.setVisibility(View.GONE);
			}
			
		}
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.sec_quiz_button:
				startSecurityQuiz();
				break;
			}
		}

	}

	
	/**
	 * Statistics Information class shows statistics information about user actions
	 * 
	 * @author Yasir Ali
	 * @version Jan 27, 2014
	 */

	private class StatisticsView extends LinearLayout implements 
					View.OnClickListener {

		private TextView statisticsInfoTextView;

		public StatisticsView(Context context) {
			super(context);
			inflate(context, R.layout.statistics_view, this);
			statisticsInfoTextView = (TextView) findViewById(R.id.statistics_info_txtView);
			statisticsInfoTextView.setOnClickListener(this);
		}
		
		public void updateStatisticsView() {
			
			if (isLoggedIn) {
				statisticsInfoTextView.setText(getResources().getString(R.string.no_statistics_available_txt));
			}
			else {	
				statisticsInfoTextView.setText(getResources().getString(R.string.login_first_for_statictics_txt));
			}
			
		}
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.statistics_info_txtView:
				showStatistics();
				break;
			}
		}


	}
	/**
	 * Show user activity statistics in terms of graph
	 */
	private void showStatistics() {
		// TBD
		Log.d(TAG, "No Statistics available, TBD");
	}
	
	/**
	 * Allow the user to participate in security quiz
	 */
	
	private void startSecurityQuiz() {
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

        WebView webview = new WebView(this);
        webview.getSettings().setJavaScriptEnabled(true);
        setContentView(webview);
        byte[] post = EncodingUtils.getBytes("j_username="+userName+"&j_password="+password, "BASE64");
        webview.postUrl("https://muses-securityquizz.rhcloud.com/LoginServlet", post);

	}

}


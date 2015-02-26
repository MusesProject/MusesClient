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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import eu.musesproject.client.connectionmanager.DetailedStatuses;
import eu.musesproject.client.connectionmanager.Statuses;
import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;
import eu.musesproject.client.db.handler.DBManager;
import eu.musesproject.client.db.handler.MockUpHandler;
import eu.musesproject.client.model.contextmonitoring.UISource;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.client.usercontexteventhandler.UserContextEventHandler;

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
	private Button loginListBtn, securityInformationListbtn;
	private Context context;
	private LoginView loginView;
	private UserContextMonitoringController userContextMonitoringController;
	public static boolean isLoggedIn = false;
	private SharedPreferences prefs;
	private ProgressDialog progressDialog;
	private Timer autoUpdate;
	private Timer oneTimeUpdate;
	private int serverStatus = -1;
		
//	private static final int NOTIFICATION_EX = 1;
//	private NotificationManager notificationManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.muses_main);
		context = getApplicationContext();
		getActionBar().setDisplayShowTitleEnabled(false);
		
		topLayout = (LinearLayout) findViewById(R.id.top_layout);
		loginListBtn = (Button) findViewById(R.id.login_list_button);
		securityInformationListbtn = (Button) findViewById(R.id.security_info_list_button);
		loginListBtn.setOnClickListener(this);
		securityInformationListbtn.setOnClickListener(this);

		userContextMonitoringController = UserContextMonitoringController
				.getInstance(context);
		
		registerCallbacks();
		
		prefs = context.getSharedPreferences(MainActivity.PREFERENCES_KEY,
				Context.MODE_PRIVATE);
		
		if (!sendDecisionIfComingFromShowFeedbackDialog(super.getIntent().getExtras())) {
			// starts the background service of MUSES
			startService(new Intent(this, MUSESBackgroundService.class));
			Log.v(TAG, "muses service started ...");
		}
		
		loginView = new LoginView(context);
		topLayout.removeAllViews();
		topLayout.addView(loginView);
		//setAppIconOnStatusBar();
		
		// create mock up sensor config in the database
		new MockUpHandler(this).createMockUpSensorConfiguration();
	}

	

	@Override
	protected void onPause() {
		super.onPause();
		autoUpdate.cancel();
		unregisterReceiver(rcReceiver);
		
	}

	private BroadcastReceiver rcReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			registerCallbacks();
		}
	};

	private boolean sendDecisionIfComingFromShowFeedbackDialog(Bundle bundle) {
		if(bundle!= null){
			moveTaskToBack(true); // Forcing activity to go in background
			String userDecision = bundle.getString(DECISION_KEY);
			if (userDecision != null) {
				if (userDecision.equals(DECISION_OK)){
					Action okAction = new Action();
					okAction.setActionType(ActionType.OK);
					okAction.setTimestamp(System.currentTimeMillis());
					Log.e(TAG, "user pressed ok..");
					sendUserDecisionToMusDM(okAction);
					
				}
				if (userDecision.equals(DECISION_CANCEL)){
					Action cancelAction = new Action();
					cancelAction.setActionType(ActionType.CANCEL);
					cancelAction.setTimestamp(System.currentTimeMillis());
					Log.e(TAG, "user pressed cancel..");
					sendUserDecisionToMusDM(cancelAction);
				}
				return true;
			} else return false;
			
		} else return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_list_button:
			topLayout.removeAllViews();
			topLayout.addView(loginView);
			break;
		case R.id.security_info_list_button:
			if (isLoggedIn) {
				//topLayout.removeAllViews();
			}
			break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter rcFilter = new IntentFilter();
		rcFilter.addAction(REGISTER_UI_CALLBACK);
		registerReceiver(rcReceiver, rcFilter);
		
		DBManager dbManager = new DBManager(getApplicationContext());
		dbManager.openDB();
		boolean isActive = dbManager.isSilentModeActive();
		dbManager.closeDB();
		if (!isActive) {
			if (loginView == null) {
				loginView = new LoginView(context);
			}
			 
			topLayout.removeAllViews();
			topLayout.addView(loginView);
			
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

		private String decisionName;
		private String riskTextualDecp;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MusesUICallbacksHandler.LOGIN_SUCCESSFUL:
				stopProgress();

				isLoggedIn = true;
				loginView.updateLoginView(true);
				
				toastMessage(getResources().getString(
						R.string.login_success_msg));
				break;
			case MusesUICallbacksHandler.LOGIN_UNSUCCESSFUL:
				stopProgress();
				if (!UserContextEventHandler.getInstance().isUserAuthenticated())
				{
					Log.e(TAG, "Login failed, service is not authenticated.");
					toastMessage(getResources().getString(R.string.login_fail_msg));
				}
				else
				{
					Log.e(TAG, "EXTRA Login failed, but service IS authenticated. ");
				}
				break;
			case MusesUICallbacksHandler.ACTION_RESPONSE_ACCEPTED:
				Log.d(TAG, "Action response accepted ..");
				// FIXME This action should not be sent here, if action is
				// granted then it should be sent directly from MusDM
//				Action action = new Action();
//				action.setActionType(ActionType.OK);
//				action.setDescription("OK");
//				action.setTimestamp(System.currentTimeMillis());
//				Log.e(TAG, "user pressed ok..");
//				sendUserDecisionToMusDM(action);
				// No Pop-up necessary FIXME
				break;
			case MusesUICallbacksHandler.ACTION_RESPONSE_DENIED:
				Log.d(TAG, "Action response denied ..");
				decisionName = msg.getData().getString("name");
				riskTextualDecp = msg.getData().getString("risk_textual_decp");
				showResultDialog(riskTextualDecp,
						MusesUICallbacksHandler.ACTION_RESPONSE_DENIED);
				break;
			case MusesUICallbacksHandler.ACTION_RESPONSE_MAY_BE:
				Log.d(TAG, "Action response maybe ..");
				decisionName = msg.getData().getString("name");
				riskTextualDecp = msg.getData().getString("risk_textual_decp");
				showResultDialog(riskTextualDecp,
						MusesUICallbacksHandler.ACTION_RESPONSE_MAY_BE);
				break;
			case MusesUICallbacksHandler.ACTION_RESPONSE_UP_TO_USER:
				Log.d(TAG, "Action response upToUser ..");
				decisionName = msg.getData().getString("name");
				riskTextualDecp = msg.getData().getString("risk_textual_decp");
				showResultDialog(riskTextualDecp,
						MusesUICallbacksHandler.ACTION_RESPONSE_UP_TO_USER);
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
	 * Shows the result dialog to the user
	 * 
	 * @param message
	 */


	private void showResultDialog(String message, int type) {
		Intent showFeedbackIntent = new Intent(
				getApplicationContext(), FeedbackActivity.class);
		showFeedbackIntent.putExtra("message", message);
		showFeedbackIntent.putExtra("type", type);
		showFeedbackIntent
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_NEW_TASK);
		
		Bundle extras = new Bundle();
		extras.putString(MainActivity.DECISION_KEY, MainActivity.DECISION_OK);
		showFeedbackIntent.putExtras(extras);
		startActivity(showFeedbackIntent);
	}

	/**
	 * Send user's decision back to MusDM which will either allow MusesAwareApp
	 * or not
	 * 
	 * @param action
	 */

	private void sendUserDecisionToMusDM(Action action) {
		userContextMonitoringController.sendUserAction(UISource.MUSES_UI,
				action, null);
	}


	/**
	 * Registers for callbacks using MusesUICallbacksHandler in
	 * UserContextMonitoringImplementation.
	 */
	private void registerCallbacks() {
		MusesUICallbacksHandler musesUICallbacksHandler = new MusesUICallbacksHandler(
				context, callbackHandler);
		ActuatorController.getInstance().registerCallback(
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

	private void openApp(String packageName){   
		Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
	    if (intent != null) {
	        /* We found the activity now start the activity */
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        context.startActivity(intent);
	    } else {
	        /* Bring user to the market or let them choose an app? */
	        intent = new Intent(Intent.ACTION_VIEW);
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        intent.setData(Uri.parse("market://details?id=" + packageName));
	        context.startActivity(intent);
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
			if (isLoggedIn != UserContextEventHandler.getInstance().isUserAuthenticated())
			{
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
			
			if (loginSuccess)
			{
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
			else
			{
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
		
		private void setServerStatus()
		{
			if (isLoggedIn)
			{
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
			
			if (serverStatus != Statuses.CURRENT_STATUS )
			{
				serverStatus = Statuses.CURRENT_STATUS;
			
				/* Not showing status in login screen */
				if (isLoggedIn)
				{
					setServerStatus();
				}
			}
			/* Set label */
//					String serverStatus = String.format("%s %s %s %s", 
//							   getResources().getString(R.string.login_button_txt), 
//							   "(",
//							   Statuses.CURRENT_STATUS == Statuses.ONLINE ? getResources().getString(R.string.current_com_status_2):
//			  						   getResources().getString(R.string.current_com_status_3),
//			  				   ")");
					//loginLabelTextView.setText(serverStatus );
			
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
			isSaveCredentialsChecked = prefs.getBoolean(SAVE_CREDENTIALS, true);
			rememberCheckBox.setChecked(isSaveCredentialsChecked);
			
		}
		
		
		
	}

	/**
	 * PrivacyPolicyView class shows privacy details on the main GUI
	 * 
	 * @author Yasir Ali
	 * @version Jan 27, 2014
	 */

	private class PrivacyPolicyView extends LinearLayout {

		public PrivacyPolicyView(Context context) {
			super(context);
			//inflate(context, R.layout.privacy_policy_view, this);
		}

	}

	/**
	 * SecurityInformationView class shows security information on the main GUI
	 * 
	 * @author Yasir Ali
	 * @version Jan 27, 2014
	 */

	public class SecurityInformationView extends LinearLayout {

		public SecurityInformationView(Context context) {
			super(context);
			//inflate(context, R.layout.security_information_view, this);
			setSecurityInformationViewAttiributesHere();
		}

		private void setSecurityInformationViewAttiributesHere() {
			// TBD
		}

	}
//	private void setAppIconOnStatusBar() {
//		Notification.Builder mBuilder =
//		        new Notification.Builder(this)
//		        .setSmallIcon(R.drawable.muses_main)
//		        .setContentTitle("")
//		        .setContentText("");
//		// Creates an explicit intent for an Activity in your app
//		Intent resultIntent = new Intent(this, MainActivity.class);
//
//		// The stack builder object will contain an artificial back stack for the
//		// started Activity.
//		// This ensures that navigating backward from the Activity leads out of
//		// your application to the Home screen.
//		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//		// Adds the back stack for the Intent (but not the Intent itself)
//		stackBuilder.addParentStack(MainActivity.class);
//		// Adds the Intent that starts the Activity to the top of the stack
//		stackBuilder.addNextIntent(resultIntent);
//		PendingIntent resultPendingIntent =
//		        stackBuilder.getPendingIntent(
//		            0,
//		            PendingIntent.FLAG_UPDATE_CURRENT
//		        );
//		mBuilder.setContentIntent(resultPendingIntent);
//		NotificationManager mNotificationManager =
//		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//		// mId allows you to update the notification later on.
//		mNotificationManager.notify(1, mBuilder.build());
//	}

}
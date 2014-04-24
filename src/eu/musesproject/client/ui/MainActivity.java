/*
 * version 1.0 - MUSES prototype software
 * Copyright MUSES project (European Commission FP7) - 2013 
 * 
 */

package eu.musesproject.client.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
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
import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;
import eu.musesproject.client.model.contextmonitoring.UISource;
import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;

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
	private Context context;
	private LoginView loginView;
	private SecurityInformationView securityInformationView;
	private PrivacyPolicyView privacyPolicyView;
	private UserContextMonitoringController userContextMonitoringController;
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
        Log.v(TAG, "muses service started ...");
        
		userContextMonitoringController = UserContextMonitoringController.getInstance(context);
		try {
			Runtime.getRuntime().exec("logcat -c");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		registerCallbacks();
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
			loginView = new LoginView(context);
			topLayout.addView(loginView);
			break;
		case R.id.security_info_list_button:
			if (isLoggedIn) {
				topLayout.removeAllViews();
				securityInformationView = new SecurityInformationView(context);
				topLayout.addView(securityInformationView);
			}
			break;
		case R.id.policy_info_list_button:
			if (isLoggedIn) {
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

    
	@SuppressLint("HandlerLeak")
	private Handler callbackHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MusesUICallbacksHandler.LOGIN_SUCCESSFUL:
				LoginView view = new LoginView(context);
				view.updateLoginView();
				isLoggedIn=true;
				toastMessage(getResources().getString(R.string.login_success_msg));
				break;
			case MusesUICallbacksHandler.LOGIN_UNSUCCESSFUL:
				toastMessage(getResources().getString(R.string.login_fail_msg));
				break;
			case MusesUICallbacksHandler.ACTION_RESPONSE_ACCEPTED:
				Log.d(TAG, "Action response accepted ..");
				// FIXME This action should not be sent here, if action is granted then it should be sent directly from MusDM
				Action action = new Action();
				action.setActionType(ActionType.OK);
				action.setTimestamp(System.currentTimeMillis());
				Log.e(TAG, "user pressed ok..");
				sendUserDecisionToMusDM(action); 
				break;
			case MusesUICallbacksHandler.ACTION_RESPONSE_DENIED:
				Log.d(TAG, "Action response denied ..");
				showResultDialog(msg.getData().getString("message"), MusesUICallbacksHandler.ACTION_RESPONSE_DENIED);
				break;
			case MusesUICallbacksHandler.ACTION_RESPONSE_MAY_BE:
				Log.d(TAG, "Action response maybe ..");
				showResultDialog(msg.getData().getString("message"), MusesUICallbacksHandler.ACTION_RESPONSE_MAY_BE);
				break;
			case MusesUICallbacksHandler.ACTION_RESPONSE_UP_TO_USER:
				Log.d(TAG, "Action response upToUser ..");
				showResultDialog(msg.getData().getString("message"), MusesUICallbacksHandler.ACTION_RESPONSE_UP_TO_USER);
				break;

			}
		}

	};

	
	/**
	 * Send user's decision back to MusDM which will either allow MusesAwareApp or not
	 * @param action
	 */
	
	private void sendUserDecisionToMusDM(Action action){
		userContextMonitoringController.sendUserAction(UISource.MUSES_UI, action, null);;
	}
	
	/**
	 * Shows the result dialog to the user
	 * @param message
	 */
	
	public void showResultDialog(String message, int type ){
		Log.d(TAG, "Showing result dialog");
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle("MUSES Policy Warning")
		.setMessage(message)
		.setCancelable(true)
		.setPositiveButton("Details", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Action action = new Action();
				action.setActionType(ActionType.OK);
				action.setTimestamp(System.currentTimeMillis());
				Log.e(TAG, "user pressed ok..");
				sendUserDecisionToMusDM(action); 
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Action action = new Action();
				action.setActionType(ActionType.CANCEL);
				action.setTimestamp(System.currentTimeMillis());
				Log.e(TAG, "user pressed cancel..");
				sendUserDecisionToMusDM(action); 
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();								
		
		if(type == MusesUICallbacksHandler.ACTION_RESPONSE_DENIED) {
			alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		}
	}	

	/**
	 * Registers for callbacks using MusesUICallbacksHandler
	 * in UserContextMonitoringImplementation. 
	 */
	private void registerCallbacks(){
		MusesUICallbacksHandler musesUICallbacksHandler = new MusesUICallbacksHandler(context, callbackHandler);
		ActuatorController.getInstance().registerCallback(musesUICallbacksHandler);
	}
	
	/**
	 * Toast messages to UI
	 * @param message
	 */
	
	private void toastMessage(String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
		
	/**
	 * Check the login fields and Then tries login to the server
	 */
	
	public void doLogin(String userName, String password) {
		if (checkLoginInputFields(userName, password)){
			userContextMonitoringController.login(userName, password);
		}else {
			toastMessage(getResources().getString(R.string.empty_login_fields_msg));
		}
	}
	
	/**
	 * Check input fields are not empty before sending it for authentication
	 * @param userName
	 * @param password
	 * @return
	 */
	
	private boolean checkLoginInputFields(String userName, String password) {
		if (userName.equals("") || password.equals("")) { // FIXME need some new checking in future
			return false;
		}	
		return true;
	}

	
	
	/**
	 * LoginView class handles Login GUI (Username, passwords etc ) on the main GUI
	 * 
	 * @author Yasir Ali
	 * @version Jan 27, 2014
	 */

	private class LoginView extends LinearLayout implements View.OnClickListener, OnCheckedChangeListener {

		private EditText userNameTxt,passwordTxt;
		private LinearLayout loginLayout1, loginLayout2;
		private Button loginBtn, logoutBtn;
		private TextView loginDetailTextView;
		private CheckBox rememberCheckBox, agreeTermsCheckBox;
		private String userName, password;
		
		public LoginView(Context context) {
			super(context);
			inflate(context, R.layout.login_view, this);
			userNameTxt = (EditText)findViewById(R.id.username_text);
			passwordTxt = (EditText)findViewById(R.id.pass_text);
			loginDetailTextView = (TextView)findViewById(R.id.login_detail_text_view);
			rememberCheckBox = (CheckBox)findViewById(R.id.remember_checkbox);
			rememberCheckBox.setOnCheckedChangeListener(this);
			agreeTermsCheckBox = (CheckBox)findViewById(R.id.agree_terms_checkbox);
			agreeTermsCheckBox.setOnCheckedChangeListener(this);
			loginLayout1 = (LinearLayout) findViewById(R.id.login_layout_1);
			loginLayout2 = (LinearLayout) findViewById(R.id.login_layout_2);
			loginBtn = (Button)findViewById(R.id.login_button);
			loginBtn.setOnClickListener(this);
			logoutBtn = (Button)findViewById(R.id.logout_button);
			logoutBtn.setOnClickListener(this);
			populateLoggedInView();
		}
		
		/**
		 * Populate logged in view if user is successfully logged in.
		 * 
		 */
		
		private void populateLoggedInView() {
			if (isLoggedIn) {
				loginLayout2.setVisibility(View.GONE);
				logoutBtn.setVisibility(View.VISIBLE);
			}
		}


		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			
		}

		/**
		 * Handles all the button on the screen, overridden method for onClickLitsener
		 * @param View
		 * 
		 */
		
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.login_button:
				userName = userNameTxt.getText().toString();
				password = passwordTxt.getText().toString();
				doLogin(userName, password);
				break;
			case R.id.logout_button:
				logoutBtn.setVisibility(View.GONE);
				loginDetailTextView.setText(getResources().getString(R.string.login_detail_view_txt));
				loginLayout2.setVisibility(View.VISIBLE);
				toastMessage(getResources().getString(R.string.logout_successfully_msg));
				isLoggedIn=false;
				break;
			}
			
		}
		
	    public void updateLoginView(){
			loginLayout2.setVisibility(View.GONE);
			logoutBtn.setVisibility(View.VISIBLE);
			loginDetailTextView.setText(String.format("%s %s", getResources().getString(R.string.logged_in_info_txt), userName));
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
			inflate(context, R.layout.privacy_policy_view, this);
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
			inflate(context, R.layout.security_information_view, this);
			setSecurityInformationViewAttiributesHere();
		}

		private void setSecurityInformationViewAttiributesHere() {
			//TBD 
		}

	}
	
}
/*
 * version 1.0 - MUSES prototype software
 * Copyright MUSES project (European Commission FP7) - 2013 
 * 
 */
package eu.musesproject.client.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import eu.musesproject.client.R;
import eu.musesproject.client.contextmonitoring.UserContextMonitoringController;

/**
 * LoginView class handles Login GUI (Username, passwords etc ) on the main GUI
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */

public class LoginView extends LinearLayout implements View.OnClickListener, OnCheckedChangeListener {

	public static final String TAG = LoginView.class.getSimpleName();
	private EditText userNameTxt,passwordTxt;
	private LinearLayout loginLayout1, loginLayout2;
	private Button loginBtn, logoutBtn;
	private TextView loginDetailTextView;
	private CheckBox rememberCheckBox, agreeTermsCheckBox;
	public static boolean isLoggedIn = false;
	private Context context;
	private String userName, password;

	// CallBack messages
	public static final int LOGIN_SUCCESSFUL = 0;
	public static final int LOGIN_UNSUCCESSFUL = 1;
	public static final int ACTION_ACCEPTED = 2;
	public static final int ACTION_DENIED = 3;
	private Activity activity;
	
	public LoginView(Context context, Activity activity) {
		super(context);
		this.context = context;
		this.activity = activity;
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
			registerCallbacksAndLogin();
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
	
	/**
	 * Check the login fields and registers for callbacks using MusesUICallbacksHandler
	 * in UserContextMonitoringImplementation. Then tries login to the server
	 */
	
	private void registerCallbacksAndLogin() {
		userName = userNameTxt.getText().toString();
		password = passwordTxt.getText().toString();
		if (checkLoginInputFields(userName, password)){
			MusesUICallbacksHandler musesUICallbacksHandler = new MusesUICallbacksHandler(context, callbackHandler);
			UserContextMonitoringController userContextMonitoringController = UserContextMonitoringController.getInstance(context);
			userContextMonitoringController.registerCallback(musesUICallbacksHandler);
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
		if (userName.equals("") || password.equals("")) { // FIXME need some moew checking in future
			return false;
		}	
		return true;
	}

	
	@SuppressLint("HandlerLeak")
	private Handler callbackHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case LOGIN_SUCCESSFUL:
				toastMessage(getResources().getString(R.string.login_success_msg));
				loginLayout2.setVisibility(View.GONE);
				logoutBtn.setVisibility(View.VISIBLE);
				loginDetailTextView.setText(String.format("%s %s", getResources().getString(R.string.logged_in_info_txt), userName));
				isLoggedIn=true;
				break;
			case LOGIN_UNSUCCESSFUL:
				toastMessage(getResources().getString(R.string.login_fail_msg));
				break;
			case ACTION_ACCEPTED:
				Log.d(TAG, "ACTION_ACCEPTED in login view");
				((MainActivity)activity).showResultDialog(msg.getData().getString("message"));
				break;
			case ACTION_DENIED:
				Log.d(TAG, "ACTION_ACCEPTED in login view");
				((MainActivity)activity).showResultDialog(msg.getData().getString("message"));
				break;
			}
		}

	};


	/**
	 * Toast messages to UI
	 * @param message
	 */
	
	private void toastMessage(String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
		
	
	
}

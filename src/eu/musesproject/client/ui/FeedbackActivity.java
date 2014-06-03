package eu.musesproject.client.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import eu.musesproject.client.R;
import eu.musesproject.client.connectionmanager.Statuses;
import eu.musesproject.client.usercontexteventhandler.UserContextEventHandler;

public class FeedbackActivity extends Activity implements View.OnClickListener {
	private static final String TAG = FeedbackActivity.class.getSimpleName();
	private Bundle extras;
	private TextView feedbackView, feedbackTitleView, currentStatusView;
	private Button resolveConflictAutoBtn, okBtn, cancelBtn;
	private String feedback = "";
	private Dialog feedBackDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		String message = extras.getString("message");
		int type = extras.getInt("type");

		// Dialog
		feedBackDialog = new Dialog(this);
		feedBackDialog.setContentView(R.layout.feedback_dialog);
		
		// Views
		feedbackView = (TextView)feedBackDialog.findViewById(R.id.feedback_txt);
		feedbackTitleView = (TextView)feedBackDialog.findViewById(R.id.feedback_title_txt);
		currentStatusView = (TextView)feedBackDialog.findViewById(R.id.server_status);
		
		// Buttons
		resolveConflictAutoBtn = (Button)feedBackDialog.findViewById(R.id.resolve_conflict_auto_btn);
		okBtn = (Button)feedBackDialog.findViewById(R.id.details_btn);
		cancelBtn = (Button)feedBackDialog.findViewById(R.id.cancel_btn);
		resolveConflictAutoBtn.setOnClickListener(this);
		
		okBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
		
		switch (type) {
		case MusesUICallbacksHandler.ACTION_RESPONSE_DENIED:
			Log.v(TAG, "acion denied in feedback shown..");
			feedback = String.format("%s %s %s",getResources().getString(R.string.feedback_txt_1),
					getResources().getString(R.string.feedback_txt_2), 
					message); // FIXME suggestions should be added in future
			okBtn.setEnabled(false);
			okBtn.setText(getResources().getString(R.string.details_btn_txt));
			feedbackView.setText(feedback);
			feedbackTitleView.setText(getResources().getString(R.string.feedback_title_txt));
			resolveConflictAutoBtn.setVisibility(View.GONE);
			
			// show current status
			currentStatusView.setText(String.format("%s %s", getResources().getString(R.string.current_com_status_1),
															 (UserContextEventHandler.serverStatus == Statuses.ONLINE) ? 
															 getResources().getString(R.string.current_com_status_2): getResources().getString(R.string.current_com_status_3) ));
			break;
		case MusesUICallbacksHandler.ACTION_RESPONSE_MAY_BE:
			Log.v(TAG, "acion maybe in feedback shown..");
			feedback = String.format("%s %s %s",getResources().getString(R.string.feedback_txt_1),
					message,
					getResources().getString(R.string.feedback_txt_2)); // FIXME suggestions should be added in future
			feedbackView.setText(feedback);
			feedbackTitleView.setText(getResources().getString(R.string.feedback_title_txt));
			resolveConflictAutoBtn.setVisibility(View.GONE);
			okBtn.setText(getResources().getString(R.string.what_can_i_do_btn_txt));
			// show current status
			currentStatusView.setText(String.format("%s %s", getResources().getString(R.string.current_com_status_1),
															 (UserContextEventHandler.serverStatus == Statuses.ONLINE) ? 
															 getResources().getString(R.string.current_com_status_2): getResources().getString(R.string.current_com_status_3) ));
			
			break;
		case MusesUICallbacksHandler.ACTION_RESPONSE_UP_TO_USER:
			Log.v(TAG, "acion up to user in feedback shown..");
			feedback = String.format("%s %s %s",getResources().getString(R.string.feedback_txt_1),
					message,
					getResources().getString(R.string.feedback_txt_2)); // FIXME suggestions should be added in future
			feedbackView.setText(feedback);
			feedbackTitleView.setText(getResources().getString(R.string.feedback_title_txt));
			okBtn.setText(getResources().getString(R.string.continue_btn_txt));
			
			// show current status
			currentStatusView.setText(String.format("%s %s", getResources().getString(R.string.current_com_status_1),
															 (UserContextEventHandler.serverStatus == Statuses.ONLINE) ? 
															 getResources().getString(R.string.current_com_status_2): getResources().getString(R.string.current_com_status_3) ));
			
			break;

		}
		
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				feedBackDialog.show();
			}
		}, 100);

		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.resolve_conflict_auto_btn:
			System.out.println("resolve_conflict_auto_btn..");
			feedBackDialog.dismiss();
			finish();		
			// Not implemented
			break;
		case R.id.details_btn:
			Intent restartMainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
			extras.putString(MainActivity.DECISION_KEY, MainActivity.DECISION_OK);
			restartMainActivityIntent.putExtras(extras);
			startActivity(restartMainActivityIntent);
			feedBackDialog.dismiss();
			finish();
			break;
		case R.id.cancel_btn:
			Intent restartMainActivityIntent2 = new Intent(getApplicationContext(), MainActivity.class);
			extras.putString(MainActivity.DECISION_KEY, MainActivity.DECISION_CANCEL);
			restartMainActivityIntent2.putExtras(extras);
			startActivity(restartMainActivityIntent2);
			feedBackDialog.dismiss();
			finish();
			break;
		}
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (feedBackDialog != null){
			feedBackDialog.dismiss();
			feedBackDialog = null;
		}
	}
	
}

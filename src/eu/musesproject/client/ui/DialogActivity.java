package eu.musesproject.client.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import eu.musesproject.client.R;

public class DialogActivity extends Activity implements View.OnClickListener {
	private static final String TAG = DialogActivity.class.getSimpleName();
	private Bundle extras;
	TextView feedbackView;
	Button resolveConflictAutoBtn, detailsBtn, cancelBtn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		String message = extras.getString("message");
		int type = extras.getInt("type");

		
		
		Log.d(TAG, "Showing result dialog");
		
		Dialog feedBackDialog = new Dialog(this);
		feedBackDialog.setContentView(R.layout.feedback_dialog);
		feedBackDialog.setTitle(getResources().getString(R.string.feedback_title_txt));
		feedbackView = (TextView)feedBackDialog.findViewById(R.id.feedback_txt);
		resolveConflictAutoBtn = (Button)feedBackDialog.findViewById(R.id.resolve_conflict_auto_btn);
		detailsBtn = (Button)feedBackDialog.findViewById(R.id.details_btn);
		cancelBtn = (Button)feedBackDialog.findViewById(R.id.cancel_btn);
		resolveConflictAutoBtn.setOnClickListener(this);
		detailsBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
		
		String feedback = String.format("%s %s %s",getResources().getString(R.string.feedback_txt_1),
												   getResources().getString(R.string.feedback_txt_2), 
												   message); // FIXME suggestions should be added in future
		feedbackView.setText(feedback);

		if (type == MusesUICallbacksHandler.ACTION_RESPONSE_DENIED) {
			detailsBtn.setEnabled(false);
		}
		
		
		feedBackDialog.show();

		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.resolve_conflict_auto_btn:
			System.out.println("resolve_conflict_auto_btn..");
			// Not implemented
			break;
		case R.id.details_btn:
			Intent restartMainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
			extras.putString(MainActivity.DECISION_KEY, MainActivity.DECISION_OK);
			restartMainActivityIntent.putExtras(extras);
			startActivity(restartMainActivityIntent);
			finish();
			break;
		case R.id.cancel_btn:
			Intent restartMainActivityIntent2 = new Intent(getApplicationContext(), MainActivity.class);
			extras.putString(MainActivity.DECISION_KEY, MainActivity.DECISION_CANCEL);
			restartMainActivityIntent2.putExtras(extras);
			startActivity(restartMainActivityIntent2);
			finish();
			break;
		}
		
	}

}

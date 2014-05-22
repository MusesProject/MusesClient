package eu.musesproject.client.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class DialogActivity extends Activity {
	private static final String TAG = DialogActivity.class.getSimpleName();
	private Bundle extras;
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
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle("MUSES Policy Warning")
				.setMessage(message)
				.setCancelable(true)
				.setPositiveButton("Details",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								sendOK();
							}

						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent restartMainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
								extras.putString(MainActivity.DECISION_KEY, MainActivity.DECISION_CANCEL);
								restartMainActivityIntent.putExtras(extras);
								startActivity(restartMainActivityIntent);
								finish();
							}
						});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();

		if (type == MusesUICallbacksHandler.ACTION_RESPONSE_DENIED) {
			alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
					.setEnabled(false);
		}

		
	}

	
	private void sendOK() {
		Intent restartMainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
		extras.putString(MainActivity.DECISION_KEY, MainActivity.DECISION_OK);
		restartMainActivityIntent.putExtras(extras);
		startActivity(restartMainActivityIntent);
		finish();
		// TODO Auto-generated method stub
		
	}

}

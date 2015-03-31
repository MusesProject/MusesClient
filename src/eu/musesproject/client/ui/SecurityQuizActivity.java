package eu.musesproject.client.ui;

import eu.musesproject.client.R;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SecurityQuizActivity extends Activity{
	WebView securityQuizWebView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.security_quiz);
		securityQuizWebView = (WebView)findViewById(R.id.security_quiz_webview);
		securityQuizWebView.setWebViewClient(new QuizWebView());
	}
	
	private class QuizWebView extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}
	
}

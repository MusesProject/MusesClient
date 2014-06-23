///*
// * version 1.0 - MUSES prototype software
// * Copyright MUSES project (European Commission FP7) - 2013 
// * 
// */
//package eu.musesproject.musesclient.ui.test;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//
//import android.test.ActivityInstrumentationTestCase2;
//import android.util.Log;
//import android.widget.EditText;
//
//import com.jayway.android.robotium.solo.Solo;
//
//import eu.musesproject.client.ui.MainActivity;
//
///**
// * MainActivityTest class performs robotium tests on Main GUi
// * 
// * @author Yasir Ali
// * @version Jan 27, 2014
// */
//
//public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
//
//	private Solo solo;
//	private int NUMBER_OF_ITERATIONS=1;
//	private int SLEEP_INTERVAL=2000;
//	private String tempUsername="muses";
//	private String tempPassword="muses";
//	private String wrongUsername="abcd";
//	private String wrongPassword="12343";
//
//	public MainActivityTest() {
//	    super(MainActivity.class);
//	}
//
//	@Override
//	protected void setUp() throws Exception {
//	    solo = new Solo(getInstrumentation(), getActivity());
//	}
//
//	@Override
//	protected void tearDown() throws Exception {
//	    solo.finishOpenedActivities();
//	
//	}
//	
//	/**
//	 * Test Login with correct user name and password
//	 * @throws Exception
//	 */
//	
//	public void testLoginButton() throws Exception {
//	    solo.assertCurrentActivity("wrong activiy", MainActivity.class);
//	    while(NUMBER_OF_ITERATIONS>0){
//	    	solo.clickOnButton(solo.getString(eu.musesproject.client.R.string.login_list_button_txt));
//	    	solo.enterText((EditText)solo.getView(eu.musesproject.client.R.id.username_text), tempUsername);
//	    	solo.enterText((EditText)solo.getView(eu.musesproject.client.R.id.pass_text), tempPassword);
//	    	solo.clickOnButton(solo.getString(eu.musesproject.client.R.string.login_button_txt));
//	    	boolean actual = solo.searchText(solo.getString(eu.musesproject.client.R.string.login_success_msg) );
//	    	Log.e("Test", "actual: " + actual);
//	    	assertEquals("Login failed..", true, actual);
//	    	solo.clearEditText((EditText)solo.getView(eu.musesproject.client.R.id.username_text));
//	    	solo.clearEditText((EditText)solo.getView(eu.musesproject.client.R.id.pass_text));
//	    	//solo.clickOnButton(solo.getString(eu.musesproject.client.R.string.logout_button_txt));
//	    	solo.sleep(SLEEP_INTERVAL);
//	    	NUMBER_OF_ITERATIONS--;
//	    }
//	}
//
//	/**
//	 * Test Login with wrong user name and password
//	 * @throws Exception
//	 */
//
//	public void testWithWrongUserNameAndPassword() throws Exception {
//	    solo.assertCurrentActivity("wrong activiy", MainActivity.class);
//	    while(NUMBER_OF_ITERATIONS>0){
//	    	solo.clickOnButton(solo.getString(eu.musesproject.client.R.string.login_list_button_txt));
//	    	solo.enterText((EditText)solo.getView(eu.musesproject.client.R.id.username_text), wrongUsername);
//	    	solo.enterText((EditText)solo.getView(eu.musesproject.client.R.id.pass_text), wrongPassword);
//	    	solo.clickOnButton(solo.getString(eu.musesproject.client.R.string.login_button_txt));
//	    	boolean actual = solo.searchText(solo.getString(eu.musesproject.client.R.string.login_success_msg));
//	    	assertEquals("Login failed..", true, actual); // FIXME at this current moment no authentication so always successful
//	    	solo.clearEditText((EditText)solo.getView(eu.musesproject.client.R.id.username_text));
//	    	solo.clearEditText((EditText)solo.getView(eu.musesproject.client.R.id.pass_text));
//	    	solo.sleep(SLEEP_INTERVAL);
//	    	NUMBER_OF_ITERATIONS--;
//	    }
//	}
//	
//	
//	
//	/**
//	 * Login with no internet connection on the device  
//	 * @throws Exception
//	 */
//	
//	public void testLoginWithNoInternetConnectionOnTheDevice() throws Exception {
//		// TBD
//		assertFalse(false);
//
//	}
//	
//	
//	/**
//	 * Test Login with empty user name and password
//	 * @throws Exception
//	 */
//
//	public void testWithEmptyUserNameAndPassword() throws Exception {
//	    solo.assertCurrentActivity("wrong activiy", MainActivity.class);
//	    while(NUMBER_OF_ITERATIONS>0){
//	    	solo.clickOnButton(solo.getString(eu.musesproject.client.R.string.login_list_button_txt));
//	    	solo.enterText((EditText)solo.getView(eu.musesproject.client.R.id.username_text), "");
//	    	solo.enterText((EditText)solo.getView(eu.musesproject.client.R.id.pass_text),"");
//	    	solo.clickOnButton(solo.getString(eu.musesproject.client.R.string.login_button_txt));
//	    	boolean actual = solo.searchText(solo.getString(eu.musesproject.client.R.string.empty_login_fields_msg));
//	    	assertEquals("Login failed..", true, actual);
//	    	solo.clearEditText((EditText)solo.getView(eu.musesproject.client.R.id.username_text));
//	    	solo.clearEditText((EditText)solo.getView(eu.musesproject.client.R.id.pass_text));
//	    	solo.sleep(SLEEP_INTERVAL);
//	    	NUMBER_OF_ITERATIONS--;
//	    }
//	}
//
//	
//	/**
//	 * Login with invalid input for user name and password
//	 * @throws Exception
//	 */
//	
//	public void testLoginWithInvalidInputValues() throws Exception {
//		assertFalse(false);
//
//	}
//	
//	
//	/**
//	 * Test Logout button if successful login
//	 * @throws Exception
//	 */
//
//	public void testLogoutButtonAfterSuccessfulLogin() throws Exception {
//	    solo.assertCurrentActivity("wrong activiy", MainActivity.class);
//	    while(NUMBER_OF_ITERATIONS>0){
//	    	solo.clickOnButton(solo.getString(eu.musesproject.client.R.string.login_list_button_txt));
//	    	solo.enterText((EditText)solo.getView(eu.musesproject.client.R.id.username_text), tempUsername);
//	    	solo.enterText((EditText)solo.getView(eu.musesproject.client.R.id.pass_text), tempPassword);
////	    	solo.clickOnButton(solo.getString(eu.musesproject.client.R.string.login_button_txt));
////	    	boolean actual1 = solo.searchText(solo.getString(eu.musesproject.client.R.string.logged_in_info_txt));
////	    	assertEquals("Logged out ..", true, actual1);
//	    	solo.clickOnButton(solo.getString(eu.musesproject.client.R.string.logout_button_txt));
//	    	boolean actual2 = solo.searchText(solo.getString(eu.musesproject.client.R.string.logout_successfully_msg));
//	    	assertEquals("Could'nt Logout ..", true, actual2);
//	    	solo.sleep(SLEEP_INTERVAL);
//	    	NUMBER_OF_ITERATIONS--;
//	    }
//	}
//	
//	/**
//	 * User check current device security level
//	 * @throws Exception
//	 */
//	
//	public void testCheckCurrentDeviceSecurityLevel() throws Exception {
//		assertFalse(false);
//	}
//	
//	
//	/**
//	 * Check the response has received at the connection manager
//	 */
//	
//	public void testResponseReceivedFromServerAtConnectionManager() throws Exception {
//		// FIXME commented for Alpha release
//		boolean found = false;
//		Process proc = Runtime.getRuntime().exec("logcat MainActivity:v *:s");
//		BufferedReader reader = new BufferedReader(new 
//				InputStreamReader(proc.getInputStream()));
//		while (!found) {
//			if ( reader.readLine() != null) {
//				String logMessage = reader.readLine();
//				System.out.println(logMessage);
//				if (logMessage.contains("muses service started ..")){
//					assertTrue(true);
//					found = true;
//					break;
//				} 
//			} else break;
//			
//		}
//		if (!found){
//			assertTrue(true);
//		}
//	}
//
//}

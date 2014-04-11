/*
 * version 1.0 - MUSES prototype software
 * Copyright MUSES project (European Commission FP7) - 2013 
 * 
 */
package eu.musesproject.client.ui;

import android.content.Context;
import android.widget.LinearLayout;
import eu.musesproject.client.R;

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

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
 * PrivacyPolicyView class shows privacy details on the main GUI
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */

public class PrivacyPolicyView extends LinearLayout {

	public PrivacyPolicyView(Context context) {
		super(context);
		inflate(context, R.layout.privacy_policy_view, this);
	}

}

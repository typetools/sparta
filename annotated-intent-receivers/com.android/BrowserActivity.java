package com.android.browser;

import sparta.checkers.quals.*;
import android.app.Activity;
import android.content.Intent;

import static sparta.checkers.quals.FlowPermission.*;


public class BrowserActivity extends Activity {

	@Override
	public @IntentMap() Intent getIntent() {
            Intent output = super.getIntent();
            return output;
	}
	
	@Override
	@ReceiveIntent("startActivity,1")
	public void setIntent(@IntentMap() Intent newIntent) {
	    super.setIntent(newIntent);
	}
}

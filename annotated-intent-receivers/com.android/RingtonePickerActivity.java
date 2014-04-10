package com.android.internal.app;

import sparta.checkers.quals.Extra;
import sparta.checkers.quals.IntentMap;
import sparta.checkers.quals.ReceiveIntent;
import android.app.Activity;
import android.content.Intent;


public class RingtonePickerActivity extends Activity {
    
	@Override
	@ReceiveIntent("startActivity,1")
    public @IntentMap() Intent getIntent() {
        @SuppressWarnings("")
        @IntentMap() Intent output = super.getIntent();
        return output;
    }

}

package com.google.android.maps;

import sparta.checkers.quals.*;
import android.app.Activity;
import android.content.Intent;

import static sparta.checkers.quals.FlowPermission.*;


public class MapsActivity extends Activity {

	@Override
	@ReceiveIntent("startActivity,1")
    public @IntentMap() Intent getIntent() {
        Intent output = super.getIntent();
        return output;
    }
}

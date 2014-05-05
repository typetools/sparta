package com.android.camera;

import sparta.checkers.quals.*;
import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;

import static sparta.checkers.quals.FlowPermission.*;


public class Camera extends Activity {

	@Override
        public @IntentMap({
            @Extra(key = MediaStore.EXTRA_OUTPUT, source = { ANY }, sink = { INTERNET, FILESYSTEM }) }) Intent getIntent() {
    		//The sink is a content resolver URI which could map to anything. We assume DATABASE, INTERNET and FILESYSTEM.
            return super.getIntent();
        }
	
	@Override
	@ReceiveIntent("startActivity,1")
	public void setIntent(@IntentMap({
	        @Extra(key = MediaStore.EXTRA_OUTPUT, source = { ANY }, sink = { INTERNET, FILESYSTEM }) }) Intent newIntent) {
	    super.setIntent(newIntent);
	}
}

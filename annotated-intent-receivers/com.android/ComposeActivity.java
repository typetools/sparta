package com.android.mail.compose;

import sparta.checkers.quals.*;
import android.app.Activity;
import android.content.Intent;

import static sparta.checkers.quals.FlowPermission.*;


public class ComposeActivity extends Activity {

    @Override
        public @IntentMap({
            @Extra(key = Intent.EXTRA_SUBJECT, sink = { INTERNET, FILESYSTEM }),
            @Extra(key = Intent.EXTRA_TEXT, sink = { INTERNET, FILESYSTEM }) }) Intent getIntent() {
            return super.getIntent();
        }
    @Override
    @ReceiveIntent("startActivity,1")
    public void setIntent(@IntentMap({
            @Extra(key = Intent.EXTRA_SUBJECT, sink = { INTERNET, FILESYSTEM }),
            @Extra(key = Intent.EXTRA_TEXT, sink = { INTERNET, FILESYSTEM }) }) Intent newIntent) {
        super.setIntent(newIntent);
    }
}

package com.google.zxing.client.android;

import static sparta.checkers.quals.FlowPermission.*;
import sparta.checkers.quals.*;
import android.content.Intent;
import android.app.Activity;

// https://github.com/zxing/zxing/blob/master/android/src/com/google/zxing/client/android/CaptureActivity.java
public class CaptureActivity extends Activity {
    @Override
    public @IntentMap({@Extra(key = "SCAN_MODE", source = {INTERNET}, sink = {})})
            Intent getIntent() {
        return super.getIntent();
    }

    @Override
    @ReceiveIntent("startActivity,1")
    public void setIntent(
            @IntentMap({@Extra(key = "SCAN_MODE", source = {INTERNET}, sink = {})})
            Intent newIntent) {
        super.setIntent(newIntent);
    }
}
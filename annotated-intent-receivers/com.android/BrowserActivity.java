package com.android.browser;

import sparta.checkers.quals.*;
import android.app.Activity;
import android.content.Intent;

import static sparta.checkers.quals.FlowPermission.*;


public class BrowserActivity extends Activity {

    public @IntentMap() Intent getIntent() {
        Intent output = super.getIntent();
        return output;
    }
}

package com.android;
/**
 * Whenever an intent is sent without extras, this Component can be set as the
 * receiver component in the component map. Useful when there is no receiver for an
 * empty intent.
 * TODO: Rename it to MockActivityReceiver.
 */
import sparta.checkers.quals.*;
import android.app.Activity;
import android.content.Intent;

import static sparta.checkers.quals.FlowPermission.*;


public class MockReceiver extends Activity {

    @Override
    public @IntentMap() Intent getIntent() {
            return super.getIntent();
    }

    @Override
    @ReceiveIntent("startActivity,1")
    public void setIntent(@IntentMap() Intent newIntent) {
        super.setIntent(newIntent);
    }
}

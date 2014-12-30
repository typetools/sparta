package com.android;
/**
 * Whenever an intent is sent without extras, this Component can be set as the
 * receiver component in the component map. Useful when there is no receiver for an
 * empty intent.
 */
import sparta.checkers.quals.*;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static sparta.checkers.quals.FlowPermission.*;


public class MockBCReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, @IntentMap() Intent intent) {
    }
}

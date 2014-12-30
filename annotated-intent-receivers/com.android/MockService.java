package com.android;
/**
 * Whenever an intent is sent without extras, this Component can be set as the
 * receiver component in the component map. Useful when there is no receiver for an
 * empty intent.
 */
import sparta.checkers.quals.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.app.Service;

import static sparta.checkers.quals.FlowPermission.*;


public class MockService extends Service {

    @Override
    public IBinder onBind(@IntentMap() Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@IntentMap() Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}

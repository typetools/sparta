package tests;
import static sparta.checkers.quals.CoarseFlowPermission.*;

import sparta.checkers.quals.IExtra;
import sparta.checkers.quals.IntentExtras;
import android.app.Service;
import android.app.Activity;
import android.content.Intent;
import android.os.IBinder;


public class ServiceReceiverStub extends Service {

    @Override
    public IBinder onBind(@IntentExtras({ @IExtra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {}) })Intent intent) {
        return null;
    }
    
}

package tests;
import static sparta.checkers.quals.FlowPermission.*;

import sparta.checkers.quals.*;
import android.app.Service;
import android.app.Activity;
import android.content.Intent;
import android.os.IBinder;


public class ServiceReceiver extends Service {


    @Override
    @ReceiveIntent("startService,1")
    public int onStartCommand(@IntentMap({ @Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {}) })Intent intent, int flags, int startId) {
        return 0;
    }

    @Override
    @ReceiveIntent("bindService,3")
    public IBinder onBind(Intent intent) {
        return null;
    }
}

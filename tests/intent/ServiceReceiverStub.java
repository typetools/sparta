package tests;
import static sparta.checkers.quals.FlowPermission.*;

import sparta.checkers.quals.*;
import android.app.Service;
import android.app.Activity;
import android.content.Intent;
import android.os.IBinder;


public class ServiceReceiverStub extends Service {

    @Override
    public @ReceiveIntent IBinder onBind(@IntentMap({ @Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {}) })Intent intent) {
        return null;
    }
    
}

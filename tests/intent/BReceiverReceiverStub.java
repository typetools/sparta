package tests;

import static sparta.checkers.quals.FlowPermission.*;

import sparta.checkers.quals.*;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;

/**
 * Class used to test the intent analysis.
 * Temporary class to simulate the Receiver Activity
 * on the Intent analysis. Used in the Intent-checker tests from sparta-code.  
 * @author pbsf
 *
 */
public class BReceiverReceiverStub extends BroadcastReceiver {
    
    
    @Override
    @ReceiveIntent("sendBroadcast,1")
    public  void onReceive(Context context, @Source(LITERAL) @Sink(CONDITIONAL) 
            @IntentMap({ @Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {}) }) Intent intent) {
    }
    
    void methodNotAllowed() {
        @SuppressWarnings("")
        @Source(LITERAL) @Sink(CONDITIONAL)  
        @IntentMap({ @Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {}) }) Intent intent = new Intent();
      //:: error: (intent.invoking.receiveintent)
        onReceive(null, intent);
    }
    
}

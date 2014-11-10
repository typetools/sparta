package tests;

import static sparta.checkers.quals.FlowPermission.*;

import sparta.checkers.quals.*;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.app.Service;
import android.content.ContextWrapper;
import android.provider.CalendarContract;
import android.view.View;
import android.view.View.OnClickListener;

public class IntentMapBottomTest extends Activity {
     
    @IntentMap()
    Intent intentMapTop = new Intent();
    
    @IntentMap({
        @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
        @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY }) }) 
    Intent intentMap = new Intent();
    
    @Source("FILESYSTEM") @Sink("INTERNET") String getFile() {
        return null;
    }
    
    //:: error: (forbidden.flow)
    @Source("FILESYSTEM") @Sink("ANY") String getFile2() {
        return null;
    }
    
    @Source() @Sink("ANY") String getTop() {
        return null;
    }
    
    void sendToDisplay(@Sink("DISPLAY") String s) {

    }
    
    //IntentMapBottom.putExtra() and IntentMapBottom.getExtra() should always fail.
    void extrasFail() {
        Intent intentMapBottom1 = null;
        Intent intentMapBottom2 = null;
        //:: error: (argument.type.incompatible) :: error: (intent.not.initialized)
        intentMapBottom1.putExtra("RandomKey1", getFile());
        //:: error: (intent.not.initialized)
        intentMapBottom1.getStringExtra("RandomKey1");
    }

    void intentAssignmentSuccess() {
        Intent intentMapBottom1 = null;
        Intent intentMapBottom2 = null;
        Intent itemp = intentMapBottom2;
        intentMapBottom2 = intentMapBottom1;
        intentMapBottom1 = itemp;
        intentMapTop = itemp;
    }

    //intentMapBottom1 and intentMapBottom2 = Bottom.
    //intentMapTop = Top
    //Bottom <: intentMap <: Top
    
    //For the tests below pay attention to the component map.
    
    
  //Sending to [type of intentMap] Receiver
    void startActivitySuccess2(@Source("FILESYSTEM") @Sink("INTERNET") int test, String test2, Object test3, String[] test4) {
        startActivity(intentMap);
    }

    void startActivityFail2() {
        //:: error: (send.intent.missing.key)
        startActivity(intentMapTop);
        Intent bottom = null;
        //:: error: (intent.not.initialized)
        startActivity(bottom);
    }
   
  //Sending to @IntentMap() Receiver - Top 
    void startActivitySuccess3(@Source("FILESYSTEM") @Sink("INTERNET") int test, String test2, Object test3, String[] test4) {
        startActivity(intentMap);
        startActivity(intentMapTop);
    }
    
}

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
     
    @IntentMapBottom Intent intentMapBottom1 = new Intent(); //@IntentMapBottom
 
    @IntentMapBottom Intent intentMapBottom2 = new Intent(); //@IntentMapBottom
    
    @IntentMap()
    Intent intentMapTop = new Intent();
    
    @IntentMap({
        @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
        @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY }) }) 
    Intent intentMap = new Intent();
    
    @Source(FILESYSTEM) @Sink(INTERNET) String getFile() {
        return null;
    }
    
    //:: error: (forbidden.flow)
    @Source(FILESYSTEM) @Sink(ANY) String getFile2() {
        return null;
    }
    
    @Source() @Sink(ANY) String getTop() {
        return null;
    }
    
    void sendToDisplay(@Sink(DISPLAY) String s) {

    }
    
    void putExtraSuccess() {
        //Forbidden flow occurs here because the flow policy
        //of this test does not allow {} -> ANY.
        //But there is no argument.type.incompatible error.
        
        intentMapBottom1.putExtra("RandomKey", getTop());
        //:: error: (forbidden.flow)
        intentMapBottom1.putExtra("RandomKey2", getFile2());
    }
    
    void putExtraFail() {
      //:: error: (argument.type.incompatible)
        intentMapBottom1.putExtra("RandomKey1", getFile());
      //:: error: (argument.type.incompatible)
        intentMapBottom1.putExtra("RandomKey2", getFile());
      //:: error: (argument.type.incompatible)
        intentMapBottom1.putExtra("RandomKey3", getFile());
    }

    //every getExtra operation returns @Souce and @Sink top types.
    void getExtraSuccess() {
        String s = intentMapBottom1.getStringExtra("k2");
        //SHOULD NOT FAIL
        sendToDisplay(s);
    }


    void intentAssignmentSuccess() {
        Intent i4 = intentMapBottom2;
        intentMapBottom2 = intentMapBottom1;
        intentMapBottom1 = i4;
        intentMapTop = i4;
    }

    void intentAssignmentFail() {
      //:: error: (assignment.type.incompatible)
        intentMapBottom1 = intentMapTop;
      //:: error: (assignment.type.incompatible)
        intentMapBottom2 = intentMapTop;
    }
    
    //intentMapBottom1 and intentMapBottom2 = Bottom.
    //intentMapTop = Top
    //Bottom <: intentMap <: Top
    
    //For the tests below pay attention to the component map.
    
    //Sending to @IntentMapBottom Receiver - Bottom.
    void startActivitySuccess(@Source(FILESYSTEM) @Sink(INTERNET) int test, String test2, Object test3, String[] test4) {
        startActivity(intentMapBottom1);
    }
        
    void startActivityFail() {
      //:: error: (send.intent.missing.key)
        startActivity(intentMapTop);
      //:: error: (send.intent.missing.key)
        startActivity(intentMap);
    }
    
  //Sending to [type of intentMap] Receiver
    void startActivitySuccess2(@Source(FILESYSTEM) @Sink(INTERNET) int test, String test2, Object test3, String[] test4) {
        startActivity(intentMapBottom1);
        startActivity(intentMap);
    }

    void startActivityFail2() {
        //:: error: (send.intent.missing.key)
        startActivity(intentMapTop);
    }
   
  //Sending to @IntentMap() Receiver - Top 
    void startActivitySuccess3(@Source(FILESYSTEM) @Sink(INTERNET) int test, String test2, Object test3, String[] test4) {
        startActivity(intentMapBottom1);
        startActivity(intentMap);
        startActivity(intentMapTop);
    }
    
}
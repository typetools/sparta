import static sparta.checkers.quals.FlowPermission.*;

import sparta.checkers.quals.IExtra;
import sparta.checkers.quals.IntentExtras;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import android.content.Context;
import android.content.Intent;
import sparta.checkers.quals.*;
import android.app.Activity;
import android.provider.CalendarContract;

public class IntentTest extends Activity {
    
    

    @SuppressWarnings("")
    @IntentExtras({
        @IExtra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
        @IExtra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY }) }) 
    Intent i1 = new Intent();
    
    @SuppressWarnings("")
    @IntentExtras({
        @IExtra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
        @IExtra(key = "k3", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY }) })
    Intent i2 = new Intent();
    
    @SuppressWarnings("")
    @IntentExtras({ @IExtra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }) })
    Intent i3 = new Intent();
    
    @SuppressWarnings("")
    @IntentExtras({
            @IExtra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
            @IExtra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY }),
            @IExtra(key = "k3", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY }) })
    Intent i4 = new Intent();
    
    @Source(FILESYSTEM) @Sink(INTERNET) String getVal() {
      //:: error: (return.type.incompatible)
        return "";
    }
    
    void sendToDisplay(@Sink(DISPLAY) String s) {

    }
    
    void putExtraSuccess() {
        i1.putExtra("k1", getVal());
    }

    void putExtraFail() {
        //:: error: (intent.type.incompatible)
        i1.putExtra("k2", getVal());
        //:: error: (intent.key.notfound)
        i1.putExtra("k3", getVal());
    }
    
    void getExtraSuccess() {
        String s = i1.getStringExtra("k2");
        //SHOULD NOT FAIL
        sendToDisplay(s);
    }

    void getExtraFail() {
        i1.putExtra("k1", getVal());
        String s1 = i1.getStringExtra("k1");
        //:: error: (intent.key.notfound)
        String s4 = i1.getStringExtra("k4");
        //:: error: (argument.type.incompatible)
        sendToDisplay(s1);
    }

    void intentAssignmentSuccess() {
        i3 = i1;
    }

    void intentAssignmentFail() {
     //:: error: (assignment.type.incompatible)
        i2 = i1;
     //:: error: (assignment.type.incompatible)
        i4 = i1;
    }
    
    void sendIntentSuccess() {
        @SuppressWarnings("")
        @IntentExtras(value={@IExtra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {  }) }, action="action1",categories={"cat1","cat2"}, data="data1")
        Intent senderIntent1 = new Intent();
        senderIntent1.setAction("action1");
        senderIntent1.addCategory("cat1");
        senderIntent1.addCategory("cat2");
        senderIntent1.setData(CalendarContract.Events.CONTENT_URI);
        @SuppressWarnings("")
        @IntentExtras({@IExtra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {DISPLAY}) })
        Intent senderIntent2 = new Intent();
        startActivity(senderIntent1);
        startActivity(senderIntent2);
    }
    
    void sendIntentFail() {
        @SuppressWarnings("")
        @IntentExtras({@IExtra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = {  }) })
        Intent senderIntent1 = new Intent();
        @SuppressWarnings("")
        @IntentExtras({@IExtra(key = "k5", source = { ANY }, sink = { }) })
        Intent senderIntent2 = new Intent();
     //:: error: (send.intent)
        startActivity(senderIntent1);
     //:: error: (send.intent)
        startActivity(senderIntent2);
    }
    
    
    
}
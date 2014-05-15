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

public class ActivityTest extends Activity {
    
    @IntentMap({
        @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
        @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY }) }) 
    Intent i1 = new Intent();
    
    @IntentMap({
        @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
        @Extra(key = "k3", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY }) })
    Intent i2 = new Intent();
    
    @IntentMap({ @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }) })
    Intent i3 = new Intent();
    
    @IntentMap({
            @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
            @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY }),
            @Extra(key = "k3", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY }) })
    Intent i4 = new Intent();
    
    @Source(FILESYSTEM) @Sink(INTERNET) String getFile() {
      //:: error: (return.type.incompatible)
        return "";
    }
    
    void sendToDisplay(@Sink(DISPLAY) String s) {

    }
    
    void putExtraSuccess() {
        i1.putExtra("k1", getFile());
    }

    void putExtraFail() {
        //:: error: (argument.type.incompatible)
        i1.putExtra("k2", getFile());
        //:: error: (argument.type.incompatible) ::error: (intent.key.notfound)
        i1.putExtra("k3", getFile());
    }
    
    void getExtraSuccess() {
        String s = i1.getStringExtra("k2");
        //SHOULD NOT FAIL
        sendToDisplay(s);
    }

    void getExtraFail() {
        i1.putExtra("k1", getFile());
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
    
    void startActivitySuccess(@Source(FILESYSTEM) @Sink(INTERNET) int test, String test2, Object test3, String[] test4) {
        @IntentMap(value={@Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {  }) })
        Intent senderIntent1 = (@IntentMap(value={@Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {  }) })
        Intent) new Intent();
        senderIntent1.setAction("action1");
        senderIntent1.addCategory("cat1");
        senderIntent1.addCategory("cat2");
        @IntentMap({@Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {DISPLAY}) })
        Intent senderIntent2 = (@IntentMap({@Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {DISPLAY}) })
        Intent) new Intent();
        startActivity(senderIntent1);
        startActivity(senderIntent2);
    }
    
    void startActivityFail() {
        Intent senderIntent1 = (@IntentMap({@Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = {  }) })
        Intent) new Intent();
        Intent senderIntent2 = (@IntentMap({@Extra(key = "k5", source = { ANY }, sink = { }) })
        Intent) new Intent();
     //:: error: (send.intent)
        startActivity(senderIntent1);
     //:: error: (send.intent)
        startActivity(senderIntent2);
    }
    
    private OnClickListener clickListenerOK = new OnClickListener() {
        public void onClick(View v)	{
            Intent senderIntent1 = (@IntentMap(value={@Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {  }) })
            Intent) new Intent();
            Intent senderIntent2 = (@IntentMap({@Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {DISPLAY}) })
            Intent) new Intent();
            startActivity(senderIntent1);
            startActivity(senderIntent2);
        }
    };

    private OnClickListener clickListenerFail = new OnClickListener() {
        public void onClick(View v)	{
            Intent senderIntent1 = (@IntentMap({@Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = {  }) })
            Intent) new Intent();
            Intent senderIntent2 = (@IntentMap({@Extra(key = "k5", source = { ANY }, sink = { }) })
            Intent) new Intent();
            //:: error: (send.intent)
            startActivity(senderIntent1);
            //:: error: (send.intent)
            startActivity(senderIntent2);
            }
    };
    
}
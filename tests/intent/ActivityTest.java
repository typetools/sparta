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

    @Source("FILESYSTEM") @Sink("INTERNET") String getFile() {
        return null;
    }

    void sendToDisplay(@Sink("DISPLAY") String s) {

    }

    @Source("ANY") @Sink() String getTop() {
        return null;
    }

    @Source() @Sink("ANY") String getBottom() {
        return null;
    }

    void putExtraSuccess() {
        i1.putExtra("k1", getFile());
    }

    void putExtraFail() {
        //:: error: (argument.type.incompatible)
        i1.putExtra("k2", getFile());
    }

    void getExtraSuccess() {
        String s = i1.getStringExtra("k2");
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

    //Parameters of the method below are used to test the component map.
    void startActivitySuccess(@Source("FILESYSTEM") @Sink("INTERNET") int test,
            @Source("FILESYSTEM") @Sink("INTERNET") String test2,
            @Source("FILESYSTEM") @Sink("INTERNET") Object test3,
            @Source("FILESYSTEM") @Sink("INTERNET") String[] test4) {
        @IntentMap(value={@Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {  }) })
        Intent senderIntent1 = new Intent();
        senderIntent1.setAction("action1");
        senderIntent1.addCategory("cat1");
        senderIntent1.addCategory("cat2");
        @IntentMap({@Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {DISPLAY}) })
        Intent senderIntent2 = new Intent();
        startActivity(senderIntent1);
        startActivity(senderIntent2);
    }

    void startActivityFail() {
        @IntentMap({@Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = {  }) })
        Intent senderIntent1 = new Intent();
        @IntentMap({@Extra(key = "k5", source = { ANY }, sink = { }) })
        Intent senderIntent2 = new Intent();
     //:: error: (send.intent.missing.key)
        startActivity(senderIntent1);
     //:: error: (send.intent.incompatible.types)
        startActivity(senderIntent2);
    }

    void unknownReceiver() {
        Intent i = new Intent();
        startActivity(i);
    }

    //Not in the component map
    void receiverNotFound() {
        Intent i = new Intent();
      //:: error: (intent.receiver.notfound)
        startActivity(i);
    }

    private OnClickListener clickListenerOK = new OnClickListener() {
        public void onClick(@Source("ANY") @Sink({})  View v)	{
            @IntentMap(value={@Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {  }) })
            Intent senderIntent1 = new Intent();
            @IntentMap({@Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {DISPLAY}) })
            Intent senderIntent2 = new Intent();
            startActivity(senderIntent1);
            startActivity(senderIntent2);
        }
    };

    private OnClickListener clickListenerFail = new OnClickListener() {
        public void onClick(@Source("ANY") @Sink({}) View v)	{
            @IntentMap({@Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = {  }) })
            Intent senderIntent1 = new Intent();
            @IntentMap({@Extra(key = "k5", source = { ANY }, sink = { }) })
            Intent senderIntent2 = new Intent();
            //:: error: (send.intent.missing.key)
            startActivity(senderIntent1);
            //:: error: (send.intent.incompatible.types)
            startActivity(senderIntent2);
            }
    };

}

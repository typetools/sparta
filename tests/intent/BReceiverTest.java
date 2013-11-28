import static sparta.checkers.quals.FlowPermission.*;

import sparta.checkers.quals.IExtra;
import sparta.checkers.quals.IntentExtras;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import android.content.Context;
import android.content.Intent;
import sparta.checkers.quals.*;
import android.app.Activity;
import android.app.Service;
import android.content.ContextWrapper;
import android.provider.CalendarContract;

public class BReceiverTest extends Activity {
    
     
    void startActivitySuccess() {
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
        sendBroadcast(senderIntent1);
        sendBroadcast(senderIntent2);
    }
    
    void startActivityFail() {
        @SuppressWarnings("")
        @IntentExtras({@IExtra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = {  }) })
        Intent senderIntent1 = new Intent();
        @SuppressWarnings("")
        @IntentExtras({@IExtra(key = "k5", source = { ANY }, sink = { }) })
        Intent senderIntent2 = new Intent();
     //:: error: (send.intent)
        sendBroadcast(senderIntent1);
     //:: error: (send.intent)
        sendBroadcast(senderIntent2);
    }
    
}
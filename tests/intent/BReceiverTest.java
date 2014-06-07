import static sparta.checkers.quals.FlowPermission.*;

import sparta.checkers.quals.*;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.app.Service;
import android.content.ContextWrapper;
import android.provider.CalendarContract;

public class BReceiverTest extends Activity {
    
     
    void startActivitySuccess() {
        Intent senderIntent1 = (@IntentMap(value={@Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {  }) })
        Intent) new Intent();
        Intent senderIntent2 = (@IntentMap({@Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {DISPLAY}) })
        Intent) new Intent();
        sendBroadcast(senderIntent1);
        sendBroadcast(senderIntent2);
    }
    
    void startActivityFail() {
        Intent senderIntent1 = (@IntentMap({@Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = {  }) })
        Intent) new Intent();
        Intent senderIntent2 = (@IntentMap({@Extra(key = "k5", source = { ANY }, sink = { }) })
        Intent) new Intent();
     //:: error: (send.intent.missing.key)
        sendBroadcast(senderIntent1);
     //:: error: (send.intent.incompatible.types)
        sendBroadcast(senderIntent2);
    }
    
}
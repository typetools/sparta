import static sparta.checkers.quals.FlowPermission.*;

import sparta.checkers.quals.*;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.app.Service;
import android.content.ContextWrapper;
import android.provider.CalendarContract;

public class ServiceTest extends Activity {
LinkedHashSet
    void startActivitySuccess() {
        @IntentMap(value={@Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {  }) }) Intent senderIntent1 = new Intent();
        @IntentMap({@Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {DISPLAY}) }) Intent senderIntent2 = new Intent();
        startService(senderIntent1);
        startService(senderIntent2);
    }
LinkedHashSet
    void startActivityFail() {
        @IntentMap({@Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = {  }) }) Intent senderIntent1 = new Intent();
        @IntentMap({@Extra(key = "k5", source = { ANY }, sink = { }) }) Intent senderIntent2 = new Intent();
     //:: error: (send.intent.missing.key)
        startService(senderIntent1);
     //:: error: (send.intent.incompatible.types)
        startService(senderIntent2);
    }
LinkedHashSet
}
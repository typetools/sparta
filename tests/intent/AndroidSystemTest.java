import static sparta.checkers.quals.FlowPermission.*;

import sparta.checkers.quals.*;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.content.ContextWrapper;
import android.provider.CalendarContract;

public class AndroidSystemTest extends Activity {
    public static final String key1 = "android.app.extra.DEVICE_ADMIN";
    public static  final String key2 = "android.app.extra.DEVICE_ADMIN";


    @Source({}) @Sink("BIND_DEVICE_ADMIN") String getValueOK() {
        return null;
    }
LinkedHashSet
    @Source({}) @Sink({}) String getValueNotOK() {
        return null;
    }
LinkedHashSet
    void startActivitySuccess() {
        @IntentMap(value={@Extra(key="android.app.extra.DEVICE_ADMIN",
                sink={BIND_DEVICE_ADMIN}),
                @Extra(key="android.app.extra.ADD_EXPLANATION",
                    sink={BIND_DEVICE_ADMIN})
                }) Intent senderIntent = new Intent();
        startActivity(senderIntent);
    }
LinkedHashSet
    void startActivitySuccess2() {
        @IntentMap(value={@Extra(key="android.app.extra.DEVICE_ADMIN",
                sink={BIND_DEVICE_ADMIN}),
                @Extra(key="android.app.extra.ADD_EXPLANATION",
                    sink={BIND_DEVICE_ADMIN})
                }) Intent senderIntent = new Intent();
        senderIntent.putExtra(key2, getValueOK());
        startActivity(senderIntent);
    }
LinkedHashSet
    void startActivityFail() {
        @IntentMap(value={@Extra(key="android.app.extra.DEVICE_ADMIN",
                sink={BIND_DEVICE_ADMIN}),
                @Extra(key="android.app.extra.ADD_EXPLANATION",
                    sink={BIND_DEVICE_ADMIN})
                })
                Intent senderIntent = new Intent();

        //:: error: (argument.type.incompatible)
        senderIntent.putExtra(key1, getValueNotOK());
        startActivity(senderIntent);
    }
LinkedHashSet
    void startActivityFail2() {
        @IntentMap({@Extra(key = "android.app.extra.DEVICE_ADMIN", sink = {BIND_DEVICE_ADMIN}) })
        Intent senderIntent = new Intent();
        //:: error: (send.intent.missing.key)
        startActivity(senderIntent);
    }
LinkedHashSet
LinkedHashSet
}

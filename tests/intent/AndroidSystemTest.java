import static sparta.checkers.quals.FlowPermission.*;

import sparta.checkers.quals.*;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.content.ContextWrapper;
import android.provider.CalendarContract;

public class AndroidSystemTest extends Activity {
    
    @Source(LITERAL) @Sink(BIND_DEVICE_ADMIN) String getValueOK() {
        @SuppressWarnings("")
        @Source(LITERAL) @Sink(BIND_DEVICE_ADMIN) String output = "";
        return output;
    }
    
    @Source(LITERAL) @Sink(CONDITIONAL) String getValueNotOK() {
        @SuppressWarnings("")
        @Sink() String output = "";
        return output;
    }
    
    void startActivitySuccess() {
        @SuppressWarnings("")
        @IntentMap(value={@Extra(key="android.app.extra.DEVICE_ADMIN",source={ANY},
            sink={BIND_DEVICE_ADMIN}),
            @Extra(key="android.app.extra.ADD_EXPLANATION", source={ANY},
                sink={BIND_DEVICE_ADMIN})
            })
        Intent senderIntent = new Intent();
        startActivity(senderIntent);
    }
    
    void startActivitySuccess2() {
        @SuppressWarnings("")
        @IntentMap(value={@Extra(key="android.app.extra.DEVICE_ADMIN",source={ANY},
            sink={BIND_DEVICE_ADMIN}),
            @Extra(key="android.app.extra.ADD_EXPLANATION", source={ANY},
                sink={BIND_DEVICE_ADMIN})
            })
        Intent senderIntent = new Intent();
        String key = "android.app.extra.DEVICE_ADMIN";
        senderIntent.putExtra(key, getValueOK());
        startActivity(senderIntent);
    }
    
    void startActivityFail() {
        @SuppressWarnings("")
        @IntentMap(value={@Extra(key="android.app.extra.DEVICE_ADMIN",source={ANY},
        sink={BIND_DEVICE_ADMIN}),
        @Extra(key="android.app.extra.ADD_EXPLANATION", source={ANY},
            sink={BIND_DEVICE_ADMIN})
        })
        Intent senderIntent = new Intent();
        String key = "android.app.extra.DEVICE_ADMIN";
        //:: error: (intent.type.incompatible)
        senderIntent.putExtra(key, getValueNotOK());
        startActivity(senderIntent);
    }
    
    void startActivityFail2() {
        @SuppressWarnings("")
        @IntentMap({@Extra(key = "android.app.extra.DEVICE_ADMIN", source = {ANY}, sink = {}) })
        Intent senderIntent = new Intent();
        //:: error: (send.intent)
        startActivity(senderIntent);
    }
    
    
}
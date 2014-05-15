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


    @Source(LITERAL) @Sink(BIND_DEVICE_ADMIN) String getValueOK() {
        return null;
    }
    
    @Source(LITERAL) @Sink(CONDITIONAL) String getValueNotOK() {
        return null;
    }
    
    void startActivitySuccess() {
        Intent senderIntent = (@IntentMap(value={@Extra(key="android.app.extra.DEVICE_ADMIN",source={ANY},
                sink={BIND_DEVICE_ADMIN}),
                @Extra(key="android.app.extra.ADD_EXPLANATION", source={ANY},
                    sink={BIND_DEVICE_ADMIN})
                })
            Intent) new Intent();
        startActivity(senderIntent);
    }
    
    void startActivitySuccess2() {
        Intent senderIntent = (@IntentMap(value={@Extra(key="android.app.extra.DEVICE_ADMIN",source={ANY},
                sink={BIND_DEVICE_ADMIN}),
                @Extra(key="android.app.extra.ADD_EXPLANATION", source={ANY},
                    sink={BIND_DEVICE_ADMIN})
                })
            Intent) new Intent();
        senderIntent.putExtra(key2, getValueOK());
        startActivity(senderIntent);
    }
    
    void startActivityFail() {
        Intent senderIntent = (@IntentMap(value={@Extra(key="android.app.extra.DEVICE_ADMIN",source={ANY},
                sink={BIND_DEVICE_ADMIN}),
                @Extra(key="android.app.extra.ADD_EXPLANATION", source={ANY},
                    sink={BIND_DEVICE_ADMIN})
                })
                Intent) new Intent();

        //:: error: (argument.type.incompatible)
        senderIntent.putExtra(key1, getValueNotOK());
        startActivity(senderIntent);
    }
    
    void startActivityFail2() {
        Intent senderIntent = (@IntentMap({@Extra(key = "android.app.extra.DEVICE_ADMIN", source = {ANY}, sink = {}) })
        Intent) new Intent();
        //:: error: (send.intent)
        startActivity(senderIntent);
    }
    
    
}
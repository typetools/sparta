package tests;

import static sparta.checkers.quals.FlowPermission.*;

import sparta.checkers.quals.*;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.app.Service;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.provider.CalendarContract;
import org.checkerframework.common.aliasing.qual.*;
public class DataflowInferenceTest extends Activity {

    @Source("ACCESS_FINE_LOCATION") @Sink() String getLocation() {
        return null;
    }

    @Source("ANY") @Sink() String getTop() {
        return null;
    }

    @Source() @Sink("ANY") String getBottom() {
        return null;
    }

    // The receiver component of the methods below is ../intentreceivers/ActivityReceiverStub.java
    // It expects the type @IntentMap({ @Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {}) })

    // Rule 1
    void m1() {
        @IntentMap({@Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
            @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { }) })
        Intent i2 = new Intent();
        i2.putExtra("k2", getLocation());
        i2.putExtra("k2", getBottom());
        i2.putExtra("k2", getLocation());
        //:: error: (argument.type.incompatible)
        i2.putExtra("k2", getTop());

        //:: error: (argument.type.incompatible)
        i2.putExtra("k1", getTop());

        i2.putExtra("k5", getBottom());
        startActivity(i2);

        Intent i3 = new Intent();
        i3.putExtra("key", getBottom());
        i3.putExtra("key", getTop());
        i3.putExtra("key", getTop());
        i3.putExtra("key", getBottom());
    }

    // Rule 2
    void m2() {
        Intent i1 = new Intent();
        i1.putExtra("k5", getLocation());
        startActivity(i1);

        i1.putExtra("k1", getLocation());
        startActivity(i1);

    }

    // TODO: Improved LUB needs to be implemented
    void m3() {
        Intent i3 = new Intent();
        //:: error: (send.intent.missing.key)
        startActivity(i3);
        boolean bool = true;
        if (bool) {
           i3.putExtra("k5", getBottom());
           startActivity(i3);
        } else {
           i3.putExtra("k5", getBottom());
           i3.putExtra("k2", getTop());
           startActivity(i3);
        }
        startActivity(i3);
    }

    // Testing "new Intent()" as a receiver of putExtra.
    void m4() {
        startActivity(new Intent());
        startActivity(new Intent().putExtra("k5", getBottom()));
        //:: error: (send.intent.incompatible.types)
        startActivity(new Intent().putExtra("k5", getTop()));
    }

    // Testing if the return type of putExtra is also being refined.
    void m5() {
        Intent i1 = new Intent();
        //:: error: (send.intent.missing.key)
        startActivity(i1);

        i1 = i1.putExtra("k5", getBottom());
        //:: error: (send.intent.missing.key)
        startActivity(i1);

        i1 = i1.putExtra("k5", getTop());

        i1 = i1.putExtra("k5", getLocation());
    }

    // Testing aliasing
    void m6() {
        Intent a = new Intent();
        a.putExtra("k", getTop());
        //::error: (unique.leaked)
        Intent b = a;
        // a loses its refined type.

        //::error: (intent.key.notfound) ::error: (argument.type.incompatible)
        b.putExtra("k", getLocation());
        //::error: (intent.key.notfound)
        a.putExtra("k", getBottom());
    }

    // There is no refinement for getExtra* methods.

    void m7() {
        //:: error: (intent.key.notfound)
        new Intent().getStringExtra("k1");
    }

    // Testing refinement for Bundles
    void m8() {
        Bundle b = new Bundle();
        b.putString("k", getTop());
        b.putString("k", getTop());
        //::error: (unique.leaked)
        Bundle aliased = b;
        //::error: (intent.key.notfound) ::error: (argument.type.incompatible)
        b.putString("k", getTop());
        //::error: (intent.key.notfound) ::error: (argument.type.incompatible)
        aliased.putString("k2", getTop());
    }
}

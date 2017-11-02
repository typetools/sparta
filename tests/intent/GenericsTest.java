import static sparta.checkers.quals.FlowPermission.*;

import sparta.checkers.quals.*;
import android.content.Intent;
import android.app.Activity;
import android.os.Parcelable;
import android.os.Parcel;

import java.util.*;
public class GenericsTest extends Activity {

    void putExtraTests() {
        @IntentMap({
            @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
            @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY })})
        Intent i1 = new Intent();

        ArrayList<@Source("FILESYSTEM") @Sink("INTERNET")Integer> a1 = new ArrayList<@Source("FILESYSTEM") @Sink("INTERNET") Integer>();
        i1.putIntegerArrayListExtra("k1", a1);
        ArrayList<@Source("ACCESS_FINE_LOCATION") @Sink("DISPLAY")Integer> a2 = new ArrayList<@Source("ACCESS_FINE_LOCATION") @Sink("DISPLAY") Integer>();
        i1.putIntegerArrayListExtra("k2", a2);

        // ::error: (argument.type.incompatible)
        i1.putIntegerArrayListExtra("k1", a2);
        // ::error: (argument.type.incompatible)
        i1.putIntegerArrayListExtra("k2", a1);

        ArrayList<@Source("FILESYSTEM") @Sink("INTERNET")Parcelable> a3 = new ArrayList<@Source("FILESYSTEM") @Sink("INTERNET") Parcelable>();
        i1.putParcelableArrayListExtra("k1", a3);
        // ::error: (argument.type.incompatible)
        i1.putParcelableArrayListExtra("k2", a3);
    }

    void getExtraTests() {
        @IntentMap({
            @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
            @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY })})
        Intent i1 = new Intent();

        ArrayList<@Source("FILESYSTEM") @Sink("INTERNET")Integer> a1 = i1.getIntegerArrayListExtra("k1");
        ArrayList<@Source("ACCESS_FINE_LOCATION") @Sink("DISPLAY")Integer> a2 = i1.getIntegerArrayListExtra("k2");

        ArrayList<@Source("FILESYSTEM") @Sink("INTERNET")Parcelable> a3 = i1.getParcelableArrayListExtra("k1");
        ArrayList<@Source("ACCESS_FINE_LOCATION") @Sink("DISPLAY")Parcelable> a4 = i1.getParcelableArrayListExtra("k2");

        @Source("FILESYSTEM") @Sink("INTERNET") Parcelable p = i1.getParcelableExtra("k1");
        @Source("ACCESS_FINE_LOCATION") @Sink("DISPLAY") Parcelable p2 = i1.getParcelableExtra("k2");

        // ::error: (assignment.type.incompatible)
        ArrayList<@Source("FILESYSTEM") @Sink("INTERNET")Integer> a5 = i1.getIntegerArrayListExtra("k2");
        // ::error: (assignment.type.incompatible)
        ArrayList<@Source("ACCESS_FINE_LOCATION") @Sink("DISPLAY")Integer> a6 = i1.getIntegerArrayListExtra("k1");

        // ::error: (assignment.type.incompatible)
        ArrayList<@Source("FILESYSTEM") @Sink("INTERNET")Parcelable> a7 = i1.getParcelableArrayListExtra("k2");
        // ::error: (assignment.type.incompatible)
        ArrayList<@Source("ACCESS_FINE_LOCATION") @Sink("DISPLAY")Parcelable> a8 = i1.getParcelableArrayListExtra("k1");

        // ::error: (assignment.type.incompatible)
        @Source("ACCESS_FINE_LOCATION") @Sink("DISPLAY") Parcelable p4 = i1.getParcelableExtra("k1");
        // ::error: (assignment.type.incompatible)
        @Source("FILESYSTEM") @Sink("INTERNET") Parcelable p3 = i1.getParcelableExtra("k2");
    }
}

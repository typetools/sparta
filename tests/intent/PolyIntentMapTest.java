import static sparta.checkers.quals.FlowPermission.*;

import sparta.checkers.quals.*;
import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;

public class PolyIntentMapTest extends Activity {

   public static final String k1 = "k1";
   public static final String k2 = "k2";

    @PolyFlow
    @PolyIntentMap Intent polyMethod(@PolyIntentMap Intent n) {
        return n;
    }

    @Source(FILESYSTEM) @Sink(INTERNET) String getFile() {
        return "";
    }

    @Source(ACCESS_FINE_LOCATION) @Sink(DISPLAY) String getLocation() {
        return "";
    }

    void putExtraSuccess() {
        @IntentMap({
            @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
            @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY })})
        Intent i1 = new Intent();

        @IntentMap({
            @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
            @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY })})
        Intent i2 = polyMethod(i1);
        i2.putExtra(k1, getFile());
        i2.putExtra(k2, getLocation());
    }

    void putExtraFail() {
        @IntentMap({
            @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
            @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY })})
        Intent i1 = new Intent();

        Intent i2 = polyMethod(i1);
        //::error: (argument.type.incompatible) ::error: (intent.key.notfound)
        i2.putExtra("SomeInexistentKey", getFile());
        //::error: (argument.type.incompatible)
        i2.putExtra(k1, getLocation());

        //Same keys but different types -> Assignment fail.
        @IntentMap({
            @Extra(key = "k1", source = {  }, sink = { INTERNET }),
            @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { })})
        //::error: (assignment.type.incompatible)
        Intent i3 = polyMethod(i1);
    }

    void intentConstructorTest() {
        @IntentMap({
            @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
            @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY })})
        Intent i1 = new Intent();

        @IntentMap({
            @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
            @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY })})
        Intent i2 = new Intent(i1);

        i2.putExtra(k1, getFile());
        i2.putExtra(k2, getLocation());

        //::error: (argument.type.incompatible) ::error: (intent.key.notfound)
        i2.putExtra("SomeInexistentKey", getFile());
        //::error: (argument.type.incompatible)
        i2.putExtra(k1, getLocation());

        @IntentMap({
            @Extra(key = "k2", source = { FILESYSTEM }, sink = { INTERNET }),
            @Extra(key = "k4", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY })})
        //::error: (assignment.type.incompatible)
        Intent i3 = new Intent(i1);
    }

    void bundleTest() {
        @IntentMap({
            @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
            @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY })})
        Intent i1 = new Intent();

        @IntentMap({
            @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
            @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY })})
        Bundle b = i1.getExtras();
        b.putString(k1, getFile());
        b.putString(k2, getLocation());

        //::error: (argument.type.incompatible) ::error: (intent.key.notfound)
        b.putString("SomeInexistentKey", getFile());
        //::error: (argument.type.incompatible)
        b.putString(k1, getLocation());

        @IntentMap({
            @Extra(key = "k2", source = { FILESYSTEM }, sink = { INTERNET }),
            @Extra(key = "k4", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY })})
        //::error: (assignment.type.incompatible)
        Bundle b2 = i1.getExtras();
    }

}
import static sparta.checkers.quals.FlowPermission.*;

import org.checkerframework.common.value.qual.StringVal;

import sparta.checkers.quals.*;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.app.Service;
import android.content.ContextWrapper;
import android.provider.CalendarContract;

public class ConstCheckerTest extends Activity {
   public static final String k1 = "k1";
   public static final String k2 = "k2";

    @IntentMap({
        @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
        @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY }),
        @Extra(key = "k3", source = { FILESYSTEM }, sink = { DISPLAY }),
        @Extra(key = "k4", source = {  }, sink = { INTERNET }),
        @Extra(key = Intent.EXTRA_PHONE_NUMBER, source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY }) }) 
    Intent i1 = new Intent();
    boolean flag = false;
    void getExtraFail() {
        @StringVal({ "k2", "k3" })
        String k2Ork3 = flag ? "k2" : "k3";

        //:: error: (forbidden.flow)
        String test1 = i1.getStringExtra(k2Ork3);
    }
}
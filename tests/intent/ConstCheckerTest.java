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
    
    @Source(FILESYSTEM) @Sink(INTERNET) String getVal() {
        return "";
    }

    @Source(ANY) @Sink() String getTop() {
        return "";
    }

    String getK1() {
        return "k1";
    }
    
    void putExtraSuccess() {
        i1.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        i1.putExtra(k1, getVal());

    }
    
    void getExtraSuccess() {
        @StringVal({ "k2", Intent.EXTRA_PHONE_NUMBER }) String k2OrPhoneNumber = null;
        //Error below should be removed if constant propagation is active.
      //:: error: (intent.key.variable)
        String test1 = i1.getStringExtra(k2OrPhoneNumber);
        
        @StringVal({ "k1", "k4" }) String k1Ork4 = null;
        //Error below should be removed if constant propagation is active.
      //:: error: (intent.key.variable)
        String test2 = i1.getStringExtra(k1Ork4);
    }
    
    void getExtraFail() {
        @StringVal({ "k2", "k3" }) String k2Ork3 = null;
        //Error below should be removed if constant propagation is active. forbidden.flow error should be added instead.
        //:: error: (intent.key.variable)
        String test1 = i1.getStringExtra(k2Ork3);
    }

    void putExtraFail() {
        //Error intent.key.variable below should be removed if constant propagation is active
        //:: error: (intent.key.variable) :: error: (argument.type.incompatible)
        i1.putExtra(getK1(), getTop());
    }
    
   
}
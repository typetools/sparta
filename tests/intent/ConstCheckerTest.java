import static sparta.checkers.quals.FlowPermission.*;

import sparta.checkers.quals.*;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.app.Service;
import android.content.ContextWrapper;
import android.provider.CalendarContract;

public class ConstCheckerTest extends Activity {
    
    @SuppressWarnings("")
    @IntentMap({
        @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
        @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY }) }) 
    Intent i1 = new Intent();
    
    @Source(FILESYSTEM) @Sink(INTERNET) String getVal() {
        //:: error: (return.type.incompatible)
        return "";
    }
    
    String getK1() {
        return "k1";
    }
    
    void putExtraSuccess() {
        String k1 = "k1";
        i1.putExtra(k1, getVal());
    }

    void putExtraFail() {
        //:: error: (intent.key.variable)
        i1.putExtra(getK1(), getVal());
    }
    
   
}
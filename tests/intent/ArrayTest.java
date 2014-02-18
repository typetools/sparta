import static sparta.checkers.quals.FlowPermission.*;

import sparta.checkers.quals.*;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.app.Service;
import android.content.ContextWrapper;
import android.provider.CalendarContract;

public class ArrayTest extends Activity {
    
    @SuppressWarnings("")
    @IntentMap({
        @Extra(key = "k1", source = { FILESYSTEM }, sink = { INTERNET }),
        @Extra(key = "k2", source = { ACCESS_FINE_LOCATION }, sink = { DISPLAY }) }) 
    Intent i1 = new Intent();
    
    @Source(FILESYSTEM) @Sink(INTERNET) String getVal() {
      //:: error: (return.type.incompatible)
        return "";
    }
    
    @Source(FILESYSTEM) @Sink(INTERNET) String @Source(FILESYSTEM) @Sink(INTERNET) [] getValArray() {
      //:: error: (return.type.incompatible)
          return new String[]{};
    }
    
    void sendToDisplay(@Sink(DISPLAY) String s) {

    }
    
    void putExtraArraySuccess() {
        i1.putExtra("k1", getValArray());
    }
    
    void putExtraArrayFail() {
      //:: error: (intent.type.incompatible)
        i1.putExtra("k2", getValArray());
        //:: error: (intent.key.notfound)
        i1.putExtra("k3", getValArray());
    }
    
    void getExtraArraySuccess() {
        @Source(FILESYSTEM) @Sink(INTERNET) String @Source(FILESYSTEM) @Sink(INTERNET) [] stringArray = i1.getStringArrayExtra("k1");
    }
    
    void getExtraArrayFail() {
      //:: error: (assignment.type.incompatible)
        @Source(FILESYSTEM) @Sink(INTERNET) String @Source(FILESYSTEM) @Sink(INTERNET) [] stringArray = i1.getStringArrayExtra("k2");
      //:: error: (assignment.type.incompatible)
        @Source(FILESYSTEM) @Sink(INTERNET) String @Source(FILESYSTEM) @Sink(INTERNET) [] stringArray2 = i1.getStringArrayExtra("k3");
    }
    
}
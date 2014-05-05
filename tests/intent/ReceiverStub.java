package tests;

import static sparta.checkers.quals.FlowPermission.*;
import sparta.checkers.quals.*;
import android.app.Activity;
import android.content.Intent;

/**
 * Class used to test the intent analysis.
 * Temporary class to simulate the Receiver Activity
 * on the Intent analysis. Used in the Intent-checker tests from sparta-code.  
 * @author pbsf
 *
 */
public class ReceiverStub extends Activity {
    
    void m() {
        @SuppressWarnings("")
        @IntentMap({ @Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {}) }) 
        Intent i = getIntent();
    }
    
    @Override
    @ReceiveIntent("startActivity,1")
    //:: error: (intent.getintent.notfound)
    public void setIntent(@IntentMap({ @Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {}) }) 
        Intent newIntent) {
        super.setIntent(newIntent);
    }
    
}

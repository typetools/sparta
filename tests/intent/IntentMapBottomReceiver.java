package tests;

import static sparta.checkers.quals.FlowPermission.*;
import sparta.checkers.quals.*;
import android.app.Activity;
import android.content.Intent;

/**
 * Class used to test the intent analysis.
 * Class that simulates a Receiver Activity
 * on the Intent analysis. Used in the Intent-checker tests from sparta-code.  
 * @author pbsf
 *
 */
public class IntentMapBottomReceiver extends Activity {
    @Override
    public  @IntentMapBottom 
    Intent getIntent() {
        return (@IntentMapBottom Intent) super.getIntent();
    }
    
    void m() {
        @IntentMapBottom
        Intent i = getIntent();
    }
    
    @Override
    @ReceiveIntent("startActivity,1")
    public void setIntent(@IntentMapBottom Intent newIntent) {
        super.setIntent(newIntent);
    }
    
}

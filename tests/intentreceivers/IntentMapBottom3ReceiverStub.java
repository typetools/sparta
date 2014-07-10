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
public class IntentMapBottom3ReceiverStub extends Activity {
    @Override
    public  @IntentMap() Intent getIntent() {
        return super.getIntent();
    }
    
    @Override
    @ReceiveIntent("startActivity,1")
    public void setIntent(@IntentMap() Intent newIntent) {
        super.setIntent(newIntent);
    }
    
}

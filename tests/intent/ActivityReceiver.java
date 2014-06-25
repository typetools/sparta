package tests;

import static sparta.checkers.quals.FlowPermission.*;
import sparta.checkers.quals.*;
import android.app.Activity;
import android.content.Intent;

/**
 * Class used to test the intent analysis.ss
 * Temporary class to simulate the Receiver Activity
 * on the Intent analysis. Used in the Intent-checker tests from sparta-code.  
 * @author pbsf
 *
 */
public class ActivityReceiver extends Activity {
    @Override
    public  @IntentMap({ @Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {}) }) 
    Intent getIntent() {
        return super.getIntent();
    }
    
    void m() {
    	@IntentMap({ @Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {}) }) 
    	Intent i = (@IntentMap({ @Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {}) }) 
        Intent) getIntent();
    }
    
    @Override
    @ReceiveIntent("startActivity,1")
    public void setIntent(@IntentMap({ @Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {}) }) 
        Intent newIntent) {
        super.setIntent(newIntent);
    }
    
}

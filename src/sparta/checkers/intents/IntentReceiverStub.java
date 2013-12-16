package sparta.checkers.intents;

import static sparta.checkers.quals.CoarseFlowPermission.ACCESS_FINE_LOCATION;
import static sparta.checkers.quals.CoarseFlowPermission.DISPLAY;
import sparta.checkers.quals.IExtra;
import sparta.checkers.quals.IntentExtras;
import android.app.Activity;
import android.content.Intent;

/**
 * Class used to test the intent analysis.
 * Temporary class to simulate the Receiver Activity
 * on the Intent analysis. Used in the Intent-checker tests from sparta-code.  
 * @author pbsf
 *
 */
public class IntentReceiverStub extends Activity {

    @Override
    public @IntentExtras({ @IExtra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {}) })
    Intent getIntent() {
        @SuppressWarnings("")@IntentExtras({ @IExtra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {}) })
        Intent output = super.getIntent();
        return output;
    }
    
}

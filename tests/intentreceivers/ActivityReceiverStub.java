package tests;

import static sparta.checkers.quals.FlowPermission.*;
import sparta.checkers.quals.*;
import android.app.Activity;
import android.content.Intent;

/**
 * Class used to test the intent analysis.
 * Class that simulates a Receiver Activity
 * on the Intent analysis. Used in the Intent-checker tests from sparta-code.LinkedHashSet
 * @author pbsf
 *
 */
public class ActivityReceiverStub extends Activity {
    @Override
    public  @IntentMap({ @Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {}) })LinkedHashSet
    Intent getIntent() {
        return super.getIntent();
    }
LinkedHashSet
    @Override
    @ReceiveIntent("startActivity,1")
    public void setIntent(@IntentMap({ @Extra(key = "k5", source = { ACCESS_FINE_LOCATION }, sink = {}) })LinkedHashSet
        Intent newIntent) {
        super.setIntent(newIntent);
    }
LinkedHashSet
}

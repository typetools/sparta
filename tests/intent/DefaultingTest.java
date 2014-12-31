package tests;

import static sparta.checkers.quals.FlowPermission.*;
import sparta.checkers.quals.*;
import android.content.Intent;
import android.app.Activity;

public class DefaultingTest extends Activity {

    @Source("FILESYSTEM") @Sink("INTERNET") String getFile() {
        return null;
    }

    void bottom(@Source() @Sink("ANY") String s) {}

    void sendToDisplay(@Source("ACCESS_FINE_LOCATION") @Sink("DISPLAY") String s) {}

    void defaultingTest() {
        @IntentMap({
            @Extra(key = "k1", source = { FILESYSTEM }),
            @Extra(key = "k2", sink = {DISPLAY})})
        Intent i = new Intent();
        i.putExtra("k1", getFile());
        sendToDisplay(i.getStringExtra("k2"));

        @IntentMap({
            @Extra(key = "k1", source = { FILESYSTEM }, sink = {INTERNET}),
            @Extra(key = "k2", source = {ACCESS_FINE_LOCATION}, sink = {DISPLAY})})
        Intent i2 = i;

        @IntentMap({
            @Extra(key = "k1", sink = {INTERNET}),
            @Extra(key = "k2", source = {ACCESS_FINE_LOCATION})})
        Intent i3 = i;
    }

    void defaultingTest2() {
        @IntentMap({
            @Extra(key = "k1"),
            @Extra(key = "k2")})
        Intent i = new Intent();
        bottom(i.getStringExtra("k1"));
        bottom(i.getStringExtra("k2"));

        @IntentMap({
            @Extra(key = "k1", source = {}, sink = {ANY}),
            @Extra(key = "k2", source = {}, sink = {ANY})})
        Intent i2 = i;

        @IntentMap({
            @Extra(key = "k1", source = {ANY}, sink = {}),
            @Extra(key = "k2", source = {ANY}, sink = {})})
        //::error: (assignment.type.incompatible)
        Intent i3 = i;

    }

    @Override
    //::error: (intent.defaulting.receiveintent.source) ::error: (intent.getintent.notfound)
    public void setIntent(@IntentMap({ @Extra(key = "k5", sink = {}) }) Intent newIntent) {
        super.setIntent(newIntent);
    }
    
    void testMethodParameter(){
        @IntentMap({ @Extra(key = "k1", source = ANY, sink={}),
            @Extra(key = "k3", source = {}), sink= ANY }) Intent i = new Intent();  
        methodParameter(i);
    }
    void methodParameter(@IntentMap({ @Extra(key = "k1", source = ANY),
            @Extra(key = "k3", source = {}) }) Intent i) {
    }

}

// @skip-test
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermissionString.*;

class LocationController implements LocationListener {
    public void onLocationChanged(@Source({ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}) Location location) {}

    public void onStatusChanged( @Source(ANY) String arg0, @Source(ANY) int arg1, @Source(ANY) Bundle arg2) {}
    public void onProviderEnabled(@Source(ANY) String arg0) {}
    public void onProviderDisabled(@Source(ANY) String arg0) {}
}

class LocationControllerBad implements LocationListener {
    //:: error: (override.param.invalid)
    public void onLocationChanged(@Sink(INTERNET) Location location) {}

    public void onStatusChanged( @Source(ANY) String arg0, @Source(ANY) int arg1, @Source(ANY) Bundle arg2) {}
    public void onProviderEnabled(@Source(ANY) String arg0) {}
    public void onProviderDisabled(@Source(ANY) String arg0) {}
}
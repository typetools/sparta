import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;

class LocationController implements LocationListener {
    public void onLocationChanged(@Source({ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}) Location location) {}

    public void onStatusChanged(String arg0, int arg1, @Source(ANY) Bundle arg2) {}
    public void onProviderEnabled(String arg0) {}
    public void onProviderDisabled(String arg0) {}
}

class LocationControllerBad implements LocationListener {
    //:: error: (override.param.invalid)
    public void onLocationChanged(Location location) {}

    public void onStatusChanged(String arg0, int arg1, @Source(ANY) Bundle arg2) {}
    public void onProviderEnabled(String arg0) {}
    public void onProviderDisabled(String arg0) {}
}
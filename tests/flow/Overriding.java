import android.location.Location;
import android.location.LocationListener;

import sparta.checkers.quals.*;

class LocationController implements LocationListener {
    public void onLocationChanged(@Sources(SPARTA_Permission.LOCATION) Location location) {}

    public void onStatusChanged(String arg0, int arg1, android.os.Bundle arg2) {}
    public void onProviderEnabled(String arg0) {}
    public void onProviderDisabled(String arg0) {}
}

class LocationControllerBad implements LocationListener {
    //:: error: (override.param.invalid)
    public void onLocationChanged(Location location) {}

    public void onStatusChanged(String arg0, int arg1, android.os.Bundle arg2) {}
    public void onProviderEnabled(String arg0) {}
    public void onProviderDisabled(String arg0) {}
}
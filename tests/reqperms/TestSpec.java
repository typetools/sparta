import android.Manifest.permission;
import android.location.LocationManager;

import sparta.checkers.quals.*;

// Tests permissions contained in permission.astub
class TestSpec {
    @RequiredPermissions(android.Manifest.permission.SET_WALLPAPER)
    Object local() { return null; }

    void bad(android.accessibilityservice.AccessibilityService as) throws java.io.IOException {
        //:: error: (all.unsatisfied.permissions)
        as.clearWallpaper();
        //:: error: (all.unsatisfied.permissions)
        local();
    }

    @RequiredPermissions(android.Manifest.permission.SET_WALLPAPER)
    void good(android.accessibilityservice.AccessibilityService as) throws java.io.IOException {
        as.clearWallpaper();
        local();
    }

    @RequiredPermissions(permission.INTERNET)
    void foo() {
        bar();
    }

    @RequiredPermissions({permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION})
    void bar() {}

    @RequiredPermissions(permission.INTERNET)
    void baz(LocationManager locationManager) {
        //:: error: (all.unsatisfied.permissions)
        String provider = locationManager.getBestProvider(null, true);
    }
}
import android.Manifest.permission;

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
}
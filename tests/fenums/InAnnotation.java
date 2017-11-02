import sparta.checkers.permission.qual.RequiredPermissions;
import android.Manifest.permission;

class InAnnotation {
    // :: error: (annotation.type.incompatible)
    @RequiredPermissions("Hi!")
    void bad1() {}

    @RequiredPermissions(permission.NFC)
    void good1() {}
}
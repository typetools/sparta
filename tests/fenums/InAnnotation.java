import android.Manifest.permission;

import sparta.checkers.quals.*;

class InAnnotation {
    //:: error: (annotation.type.incompatible)
    @RequiredPermissions("Hi!")
    void bad1() {}

    @RequiredPermissions(permission.NFC)
    void good1() {}
}
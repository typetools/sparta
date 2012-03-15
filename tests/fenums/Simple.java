import android.Manifest.permission;

import sparta.checkers.quals.*;

class Simple {
    void foo() {
        //:: error: (assignment.type.incompatible)
        String s = permission.NFC;
        s = "ha!";
        //:: error: (assignment.type.incompatible)
        @Permission String ps = s;
        ps = permission.NFC;

        Object o = null;
        //:: error: (assignment.type.incompatible)
        o = permission.NFC;
    }
}

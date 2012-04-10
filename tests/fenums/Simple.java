import android.Manifest.permission;

import sparta.checkers.quals.*;

class Simple {
    void foo(String s, Object o) {
        //:: error: (assignment.type.incompatible)
        s = permission.NFC;
        s = "ha!";
        //:: error: (assignment.type.incompatible)
        @Permission String ps = s;
        ps = permission.NFC;

        o = null;
        //:: error: (assignment.type.incompatible)
        o = permission.NFC;
    }
}

// @skip-test
import sparta.checkers.permission.qual.Permission;
import static android.Manifest.permission.NFC;


class Simple {
    void foo(String s, Object o) {
        //:: error: (assignment.type.incompatible)
        s = NFC;
        s = "ha!";
        //:: error: (assignment.type.incompatible)
        @Permission String ps = s;
        ps = NFC;

        o = null;
        //:: error: (assignment.type.incompatible)
        o = NFC;
    }
}

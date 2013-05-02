import sparta.checkers.quals.*;
import sparta.checkers.quals.SPARTA_Permission;
import java.util.*;

// TODO: I couldn't reproduce this issue in the PolyAll Checker.
//@skip-test
class MethodTypeArgumentResolution {
    void foo(List<@Sources(SPARTA_Permission.LOCATION) Object> l, Comparator<@Sources(SPARTA_Permission.LOCATION) Object> c) {
        // Unexpected incompatible type error, because method type argument is not inferred correctly.
        Collections.sort(l, c);
        // This call is/should be equivalent.
        Collections.<@Sources(SPARTA_Permission.LOCATION) Object>sort(l, c);
    }
}

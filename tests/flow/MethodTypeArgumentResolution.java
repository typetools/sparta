import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;
import java.util.*;

// TODO: I couldn't reproduce this issue in the PolyAll Checker.
//@skip-test
class MethodTypeArgumentResolution {
    void foo(List<@Sources(SpartaPermission.ACCESS_FINE_LOCATION) Object> l, Comparator<@Sources(SpartaPermission.ACCESS_FINE_LOCATION) Object> c) {
        // Unexpected incompatible type error, because method type argument is not inferred correctly.
        Collections.sort(l, c);
        // This call is/should be equivalent.
        Collections.<@Sources(SpartaPermission.ACCESS_FINE_LOCATION) Object>sort(l, c);
    }
}

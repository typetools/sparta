import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.FlowSource;
import java.util.*;

// TODO: I couldn't reproduce this issue in the PolyAll Checker.
//@skip-test
class MethodTypeArgumentResolution {
    void foo(List<@Sources(FlowSource.LOCATION) Object> l, Comparator<@Sources(FlowSource.LOCATION) Object> c) {
        // Unexpected incompatible type error, because method type argument is not inferred correctly.
        Collections.sort(l, c);
        // This call is/should be equivalent.
        Collections.<@Sources(FlowSource.LOCATION) Object>sort(l, c);
    }
}

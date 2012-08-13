import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import java.util.*;

// TODO: I couldn't reproduce this issue in the PolyAll Checker.
//@skip-test
class MethodTypeArgumentResolution {
    void foo(List<@FlowSources(FlowSource.LOCATION) Object> l, Comparator<@FlowSources(FlowSource.LOCATION) Object> c) {
        // Unexpected incompatible type error, because method type argument is not inferred correctly.
        Collections.sort(l, c);
        // This call is/should be equivalent.
        Collections.<@FlowSources(FlowSource.LOCATION) Object>sort(l, c);
    }
}

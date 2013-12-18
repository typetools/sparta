import sparta.checkers.quals.*;
import sparta.checkers.quals.ParameterizedFlowPermission;
import java.util.*;

// TODO: I couldn't reproduce this issue in the PolyAll Checker.
//@skip-test
class MethodTypeArgumentResolution {
    void foo(List<@Source(FlowPermission.ACCESS_FINE_LOCATION) Object> l, Comparator<@Source(FlowPermission.ACCESS_FINE_LOCATION) Object> c) {
        // Unexpected incompatible type error, because method type argument is not inferred correctly.
        Collections.sort(l, c);
        // This call is/should be equivalent.
        Collections.<@Source(FlowPermission.ACCESS_FINE_LOCATION) Object>sort(l, c);
    }
}

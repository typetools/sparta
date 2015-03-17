import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermissionString.*;
import java.util.*;

class MethodTypeArgumentResolution {
    void foo(List<@Source(ACCESS_FINE_LOCATION) Object> l, Comparator<@Source(ACCESS_FINE_LOCATION) Object> c) {
        Collections.sort(l, c);
        Collections.<@Source(ACCESS_FINE_LOCATION) Object>sort(l, c);
    }
}

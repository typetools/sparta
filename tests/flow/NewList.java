import java.util.*;

import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import static sparta.checkers.quals.FlowPermission.*;


class MyList<T extends @Source(sparta.checkers.quals.FlowPermission.ANY) Object> {
}

class NewList {
    List<@Source(ACCESS_FINE_LOCATION) Object> good1  = new ArrayList<@Source(ACCESS_FINE_LOCATION) Object>();
    //:: error: (assignment.type.incompatible)
    List<@Source(ACCESS_FINE_LOCATION) Object> bad1 = new ArrayList<@Source(ANY) Object>();

    MyList</*@Source(FlowPermission.ACCESS_FINE_LOCATION)*/ Object> good2  = new MyList<@Source(ACCESS_FINE_LOCATION) Object>();
    //:: error: (assignment.type.incompatible)
    MyList<@Source(ACCESS_FINE_LOCATION) Object> bad2 = new MyList<@Source(FlowPermission.ANY) Object>();
}

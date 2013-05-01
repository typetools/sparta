import java.util.*;

import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.FlowSource;

class MyList<T extends @Sources(sparta.checkers.quals.Sources.FlowSource.ANY) Object> {
}

class NewList {
    List<@Sources(FlowSource.LOCATION) Object> good1  = new ArrayList<@Sources(FlowSource.LOCATION) Object>();
    //:: error: (assignment.type.incompatible)
    List<@Sources(FlowSource.LOCATION) Object> bad1 = new ArrayList<@Sources(FlowSource.ANY) Object>();

    MyList</*@Sources(FlowSource.LOCATION)*/ Object> good2  = new MyList<@Sources(FlowSource.LOCATION) Object>();
    //:: error: (assignment.type.incompatible)
    MyList<@Sources(FlowSource.LOCATION) Object> bad2 = new MyList<@Sources(FlowSource.ANY) Object>();
}
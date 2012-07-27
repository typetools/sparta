import java.util.*;

import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;

class MyList<T extends @FlowSources(sparta.checkers.quals.FlowSources.FlowSource.ANY) Object> {
}

class NewList {
    List<@FlowSources(FlowSource.LOCATION) Object> good1  = new ArrayList<@FlowSources(FlowSource.LOCATION) Object>();
    //:: error: (assignment.type.incompatible)
    List<@FlowSources(FlowSource.LOCATION) Object> bad1 = new ArrayList<@FlowSources(FlowSource.ANY) Object>();

    MyList</*@FlowSources(FlowSource.LOCATION)*/ Object> good2  = new MyList<@FlowSources(FlowSource.LOCATION) Object>();
    //:: error: (assignment.type.incompatible)
    MyList<@FlowSources(FlowSource.LOCATION) Object> bad2 = new MyList<@FlowSources(FlowSource.ANY) Object>();
}
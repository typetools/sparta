import java.util.*;

import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;

class MyList<T extends @Sources(sparta.checkers.quals.SpartaPermission.ANY) Object> {
}

class NewList {
    List<@Sources(SpartaPermission.LOCATION) Object> good1  = new ArrayList<@Sources(SpartaPermission.LOCATION) Object>();
    //:: error: (assignment.type.incompatible)
    List<@Sources(SpartaPermission.LOCATION) Object> bad1 = new ArrayList<@Sources(SpartaPermission.ANY) Object>();

    MyList</*@Sources(SpartaPermission.LOCATION)*/ Object> good2  = new MyList<@Sources(SpartaPermission.LOCATION) Object>();
    //:: error: (assignment.type.incompatible)
    MyList<@Sources(SpartaPermission.LOCATION) Object> bad2 = new MyList<@Sources(SpartaPermission.ANY) Object>();
}
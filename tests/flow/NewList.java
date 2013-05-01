import java.util.*;

import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.SPARTA_Permission;

class MyList<T extends @Sources(sparta.checkers.quals.Sources.SPARTA_Permission.ANY) Object> {
}

class NewList {
    List<@Sources(SPARTA_Permission.LOCATION) Object> good1  = new ArrayList<@Sources(SPARTA_Permission.LOCATION) Object>();
    //:: error: (assignment.type.incompatible)
    List<@Sources(SPARTA_Permission.LOCATION) Object> bad1 = new ArrayList<@Sources(SPARTA_Permission.ANY) Object>();

    MyList</*@Sources(SPARTA_Permission.LOCATION)*/ Object> good2  = new MyList<@Sources(SPARTA_Permission.LOCATION) Object>();
    //:: error: (assignment.type.incompatible)
    MyList<@Sources(SPARTA_Permission.LOCATION) Object> bad2 = new MyList<@Sources(SPARTA_Permission.ANY) Object>();
}
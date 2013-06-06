import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.FlowPermission;

class ListTest {
    // Simple test to ensure that defaulting on java.util.List works correctly.
    java.util.List<String> s;
    String t = s.get(1);
}


class List<T extends @Sink(FlowPermission.CONDITIONAL) @Source(FlowPermission.ANY) Object> {
    T get( @Source(FlowPermission.ANY) List<T> this, int index) { return null; }
    void add(T p) {}
}

class Generics {
    List<Object> lo = new List<Object>();
    List<@Source(FlowPermission.INTERNET) Object> netok = new List<@Source(FlowPermission.INTERNET) Object>();
    List<@Source(FlowPermission.INTERNET) Object> netok2 = foo();
    
    //:: error: (assignment.type.incompatible)
    List<@Source(FlowPermission.INTERNET) Object> neterr = new List<Object>();

    void use(Object o, @Source(FlowPermission.INTERNET) Object neto) {
        netok.add(neto);
        neto = netok.get(4);
        //:: error: (assignment.type.incompatible)
        o = netok.get(4);
    }
    
    List<@Source(FlowPermission.INTERNET) Object> foo() {
    	return new List<@Source(FlowPermission.INTERNET) Object>();
    }
}
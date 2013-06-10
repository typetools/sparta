import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;

class ListTest {
    // Simple test to ensure that defaulting on java.util.List works correctly.
    java.util.List<String> s;
    String t = s.get(1);
}


class FlowList<T extends @Sink(CONDITIONAL) @Source(ANY) Object> {
    
    T getF( @Source(ANY) FlowList<T> this, int index) { return null; }
    void addF(T p) {}
}

class Generics {

    List<Object> lo = new List<Object>();
    List<@Source(FlowPermission.INTERNET) Object> netok = new List<@Source(FlowPermission.INTERNET) Object>();
    List<@Source(FlowPermission.INTERNET) Object> netok2 = foo();
    
    //:: error: (assignment.type.incompatible)
    FlowList<@Source(INTERNET) Object> neterr = new FlowList<Object>();
    void use(Object o, @Source(INTERNET) Object neto) {

        netok.addF(neto);
        neto = netok.getF(4);
        //:: error: (assignment.type.incompatible)
        o = netok.getF(4);
    }
    
    List<@Source(FlowPermission.INTERNET) Object> foo() {
    	return new List<@Source(FlowPermission.INTERNET) Object>();
    }

}
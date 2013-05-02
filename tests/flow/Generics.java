import sparta.checkers.quals.*;
import sparta.checkers.quals.SPARTA_Permission;
import sparta.checkers.quals.SPARTA_Permission;

class ListTest {
    // Simple test to ensure that defaulting on java.util.List works correctly.
    java.util.List<String> s;
    String t = s.get(1);
}


class List<T extends @Sinks(SPARTA_Permission.CONDITIONAL) @Sources(SPARTA_Permission.ANY) Object> {
    T get( @Sources(SPARTA_Permission.ANY) List<T> this, int index) { return null; }
    void add(T p) {}
}

class Generics {
    List<Object> lo = new List<Object>();
    List<@Sources(SPARTA_Permission.NETWORK) Object> netok = new List<@Sources(SPARTA_Permission.NETWORK) Object>();
    //:: error: (assignment.type.incompatible)
    List<@Sources(SPARTA_Permission.NETWORK) Object> neterr = new List<Object>();

    void use(Object o, @Sources(SPARTA_Permission.NETWORK) Object neto) {
        netok.add(neto);
        neto = netok.get(4);
        //:: error: (assignment.type.incompatible)
        o = netok.get(4);
    }
}
import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;
import sparta.checkers.quals.SpartaPermission;

class ListTest {
    // Simple test to ensure that defaulting on java.util.List works correctly.
    java.util.List<String> s;
    String t = s.get(1);
}


class List<T extends @Sinks(SpartaPermission.CONDITIONAL) @Sources(SpartaPermission.ANY) Object> {
    T get( @Sources(SpartaPermission.ANY) List<T> this, int index) { return null; }
    void add(T p) {}
}

class Generics {
    List<Object> lo = new List<Object>();
    List<@Sources(SpartaPermission.INTERNET) Object> netok = new List<@Sources(SpartaPermission.INTERNET) Object>();
    //:: error: (assignment.type.incompatible)
    List<@Sources(SpartaPermission.INTERNET) Object> neterr = new List<Object>();

    void use(Object o, @Sources(SpartaPermission.INTERNET) Object neto) {
        netok.add(neto);
        neto = netok.get(4);
        //:: error: (assignment.type.incompatible)
        o = netok.get(4);
    }
}
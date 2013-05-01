import sparta.checkers.quals.*;
import sparta.checkers.quals.Sinks.FlowSink;
import sparta.checkers.quals.Sources.FlowSource;

class ListTest {
    // Simple test to ensure that defaulting on java.util.List works correctly.
    java.util.List<String> s;
    String t = s.get(1);
}


class List<T extends @Sinks(FlowSink.CONDITIONAL) @Sources(FlowSource.ANY) Object> {
    T get( @Sources(FlowSource.ANY) List<T> this, int index) { return null; }
    void add(T p) {}
}

class Generics {
    List<Object> lo = new List<Object>();
    List<@Sources(FlowSource.NETWORK) Object> netok = new List<@Sources(FlowSource.NETWORK) Object>();
    //:: error: (assignment.type.incompatible)
    List<@Sources(FlowSource.NETWORK) Object> neterr = new List<Object>();

    void use(Object o, @Sources(FlowSource.NETWORK) Object neto) {
        netok.add(neto);
        neto = netok.get(4);
        //:: error: (assignment.type.incompatible)
        o = netok.get(4);
    }
}
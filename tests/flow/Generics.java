import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;

class ListTest {
    // Simple test to ensure that defaulting on java.util.List works correctly.
    java.util.List<String> s;
    String t = s.get(1);
}

//:: error: (forbidden.flow)
class List<T extends @FlowSinks({}) @FlowSources(sparta.checkers.quals.FlowSources.FlowSource.ANY) Object> {
    T get( @FlowSources(sparta.checkers.quals.FlowSources.FlowSource.ANY) List<T> this, int index) { return null; }
    void add(T p) {}
}

class Generics {
    List<Object> lo = new List<Object>();
    List<@FlowSources(FlowSource.NETWORK) Object> netok = new List<@FlowSources(FlowSource.NETWORK) Object>();
    //:: error: (assignment.type.incompatible)
    List<@FlowSources(FlowSource.NETWORK) Object> neterr = new List<Object>();

    void use(Object o, @FlowSources(FlowSource.NETWORK) Object neto) {
        netok.add(neto);
        neto = netok.get(4);
        //:: error: (assignment.type.incompatible)
        o = netok.get(4);
    }
}
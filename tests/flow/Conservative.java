import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.*;
import sparta.checkers.quals.FlowSinks.*;

@ConservativeFlow
class Cons {
    Object get() { return null; }
    void set(Object o) {}
}

class Use {
    @FlowSources({}) Object nosrc;
    @FlowSources(FlowSource.ANY) Object anysrc;
    @FlowSinks({}) Object nosink;
    @FlowSinks(FlowSink.ANY) Object anysink;

    void demo(Cons c) {
        //:: error: (assignment.type.incompatible)
        nosrc = c.get();
        anysrc = c.get();
        //:: error: (argument.type.incompatible)
        c.set(nosink);
        c.set(anysink);
    }
}
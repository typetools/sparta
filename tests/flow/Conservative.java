import sparta.checkers.quals.*;
import sparta.checkers.quals.Source.*;
import sparta.checkers.quals.Sink.*;

@ConservativeFlow
class Cons {
    Object get() { return null; }
    //:: error: (forbidden.flow)
    void set(Object o) {}
}

class Use {
    //:: error: (forbidden.flow)
    @Source({}) Object nosrc;
    @Source(FlowPermission.ANY) Object anysrc;
    //:: error: (forbidden.flow)
    @Sink({}) Object nosink;
    //:: error: (forbidden.flow)
    @Sink(FlowPermission.ANY) Object anysink;

    void demo(Cons c) {
        //:: error: (assignment.type.incompatible)
        nosrc = c.get();
        anysrc = c.get();
        //:: error: (argument.type.incompatible)
        c.set(nosink);
        c.set(anysink);
    }
}
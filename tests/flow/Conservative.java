import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.*;
import sparta.checkers.quals.Sinks.*;

@ConservativeFlow
class Cons {
    Object get() { return null; }
    //:: error: (forbidden.flow)
    void set(Object o) {}
}

class Use {
    //:: error: (forbidden.flow)
    @Sources({}) Object nosrc;
    @Sources(SPARTA_Permission.ANY) Object anysrc;
    //:: error: (forbidden.flow)
    @Sinks({}) Object nosink;
    //:: error: (forbidden.flow)
    @Sinks(SPARTA_Permission.ANY) Object anysink;

    void demo(Cons c) {
        //:: error: (assignment.type.incompatible)
        nosrc = c.get();
        anysrc = c.get();
        //:: error: (argument.type.incompatible)
        c.set(nosink);
        c.set(anysink);
    }
}
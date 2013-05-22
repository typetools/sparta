import sparta.checkers.quals.*;
import sparta.checkers.quals.Source.*;
import sparta.checkers.quals.Sink.*;
import static sparta.checkers.quals.FlowPermission.*;

@NotReviewed
class Cons {
    //:: error: (forbidden.flow)
    Object get() { return null; }
    //:: error: (forbidden.flow)
    void set(Object o) {}
}

class Use {
    //:: error: (forbidden.flow)
    @Source({}) Object nosrc;
    @Source(ANY) Object anysrc;
    //:: error: (forbidden.flow)
    @Sink({}) Object nosink;


    void demo(Cons c) {
        //:: error: (assignment.type.incompatible)
        nosrc = c.get();
        //:: error: (assignment.type.incompatible)
        anysrc = c.get();
        //:: error: (argument.type.incompatible)
        c.set(nosink);
     
    }
}
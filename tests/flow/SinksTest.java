import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;

class SinkTest {
    void sendData(@Sink(FlowPermission.INTERNET) Object p) {
        // Allowed: fewer sinks
        noComm(p);
    }

    //:: error: (forbidden.flow)
    void noComm(@Sink({}) Object p) {
        // Forbidden: more sinks
        //:: error: (argument.type.incompatible)
        sendData(p);
    }

    void two(@Sink({FlowPermission.INTERNET, FlowPermission.WRITE_SMS}) Object p) {
        // Allowed: fewer sinks
        sendData(p);
        // Forbidden: more sinks
        //:: error: (argument.type.incompatible)
        any(p);
    }

    //:: error: (forbidden.flow)
    void any(@Sink(FlowPermission.ANY) Object p) {
        // Allowed: fewer sinks
        two(p);
    }

    // Null is legal for all sinks.
    void testNull() {
        any(null);
        two(null);
    }
}
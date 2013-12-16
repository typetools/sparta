import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;

class SinkTest {
    void sendData(@Sink(CoarseFlowPermission.INTERNET) Object p) {
        // Allowed: fewer sinks
        noComm(p);
    }

    //:: error: (forbidden.flow)
    void noComm(@Sink({}) Object p) {
        // Forbidden: more sinks
       //MASKED //:: error: (argument.type.incompatible)
        //:: error: (forbidden.flow)
        sendData(p);
    }

    void two(@Sink({CoarseFlowPermission.INTERNET, CoarseFlowPermission.WRITE_SMS}) Object p) {
        // Allowed: fewer sinks
        sendData(p);
        // Forbidden: more sinks
        //:: error: (argument.type.incompatible)
        any(p);
    }

    //:: error: (forbidden.flow)
    void any(@Sink(CoarseFlowPermission.ANY) Object p) {
        // Allowed: fewer sinks
        //:: error: (forbidden.flow)
        two(p);
    }

    // Null is legal for all sinks.
    void testNull() {
        any(null);
        two(null);
    }
}
import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import static sparta.checkers.quals.FlowPermission.*;


class SinkTest {
    void sendData(@Sink({INTERNET, CONDITIONAL}) Object p) {
        // Allowed: fewer sinks
        noComm(p);
    }


    void noComm(@Sink(CONDITIONAL) Object p) {
        // Forbidden: more sinks
        //:: error: (argument.type.incompatible)
        sendData(p);
    }

    void two(@Sink({INTERNET, WRITE_SMS, CONDITIONAL}) Object p) {
        // Allowed: fewer sinks
        sendData(p);
        // Forbidden: more sinks
        //:: error: (argument.type.incompatible)
        any(p);
    }

    void any(@Sink(ANY) Object p) {
        // Allowed: fewer sinks
        two(p);
    }

    // Null is legal for all sinks.
    void testNull() {
        any(null);
        two(null);
    }
}
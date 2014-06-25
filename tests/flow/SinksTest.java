import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import static sparta.checkers.quals.FlowPermission.*;


class SinkTest {
    void sendData(@Source({}) @Sink({INTERNET}) Object p) {
        // Allowed: fewer sinks
        noComm(p);
    }


    void noComm(@Source({}) @Sink({}) Object p) {
        // Forbidden: more sinks
        //:: error: (argument.type.incompatible)
        sendData(p);
    }

    void two(@Source({}) @Sink({INTERNET, WRITE_SMS}) Object p) {
        // Allowed: fewer sinks
        sendData(p);
        // Forbidden: more sinks
        //:: error: (argument.type.incompatible)
        any(p);
    }

    void any(@Source({}) @Sink(ANY) Object p) {
        // Allowed: fewer sinks
        two(p);
    }

    // Null is legal for all sinks.
    void testNull() {
        any(null);
        two(null);
    }
}
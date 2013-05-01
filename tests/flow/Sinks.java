import sparta.checkers.quals.*;
import sparta.checkers.quals.Sinks.FlowSink;

class Sinks {
    void sendData(@Sinks(FlowSink.NETWORK) Object p) {
        // Allowed: fewer sinks
        noComm(p);
    }

    //:: error: (forbidden.flow)
    void noComm(@Sinks({}) Object p) {
        // Forbidden: more sinks
        //:: error: (argument.type.incompatible)
        sendData(p);
    }

    void two(@Sinks({FlowSink.NETWORK, FlowSink.SMS}) Object p) {
        // Allowed: fewer sinks
        sendData(p);
        // Forbidden: more sinks
        //:: error: (argument.type.incompatible)
        any(p);
    }

    //:: error: (forbidden.flow)
    void any(@Sinks(FlowSink.ANY) Object p) {
        // Allowed: fewer sinks
        two(p);
    }

    // Null is legal for all sinks.
    void testNull() {
        any(null);
        two(null);
    }
}
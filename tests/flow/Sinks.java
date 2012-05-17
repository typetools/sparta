import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSinks.FlowSink;

class Sinks {
    void sendData(@FlowSinks(FlowSink.NETWORK) Object p) {
        // Allowed: fewer sinks
        noComm(p);
    }

    void noComm(Object p) {
        // Forbidden: more sinks
        //:: error: (argument.type.incompatible)
        sendData(p);
    }

    void two(@FlowSinks({FlowSink.NETWORK, FlowSink.TEXTMESSAGE}) Object p) {
        // Allowed: fewer sinks
        sendData(p);
        // Forbidden: more sinks
        //:: error: (argument.type.incompatible)
        any(p);
    }

    void any(@FlowSinks(FlowSink.ANY) Object p) {
        // Allowed: fewer sinks
        two(p);
    }

    // Null is legal for all sinks.
    void testNull() {
        any(null);
        two(null);
    }
}
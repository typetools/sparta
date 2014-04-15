import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import static sparta.checkers.quals.FlowPermission.*;


class ParameterizedSinkTest {
    void sendData(@Sink(value={CONDITIONAL}, finesinks={@FineSink(value=INTERNET, params={"maps.google.com", "voice.google.com", "google.com"})}) Object p) {
        // Allowed: fewer sinks
        noComm(p);
        // Forbidden: less restrictive sink params
        //:: error: (argument.type.incompatible)
        two(p);
    }

    void noComm(@Sink(CONDITIONAL) Object p) {
        // Forbidden: more sinks
        //:: error: (argument.type.incompatible)
        sendData(p);
    }

    void two(@Sink(value={WRITE_SMS, CONDITIONAL}, finesinks={@FineSink(value=INTERNET, params={"*.google.com", "google.com"})}) Object p) {
        // Allowed: fewer sinks, more restrictive sink params
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
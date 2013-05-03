import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;

class SinksTest {
    void sendData(@Sinks(SpartaPermission.INTERNET) Object p) {
        // Allowed: fewer sinks
        noComm(p);
    }

    //:: error: (forbidden.flow)
    void noComm(@Sinks({}) Object p) {
        // Forbidden: more sinks
        //:: error: (argument.type.incompatible)
        sendData(p);
    }

    void two(@Sinks({SpartaPermission.INTERNET, SpartaPermission.SMS}) Object p) {
        // Allowed: fewer sinks
        sendData(p);
        // Forbidden: more sinks
        //:: error: (argument.type.incompatible)
        any(p);
    }

    //:: error: (forbidden.flow)
    void any(@Sinks(SpartaPermission.ANY) Object p) {
        // Allowed: fewer sinks
        two(p);
    }

    // Null is legal for all sinks.
    void testNull() {
        any(null);
        two(null);
    }
}
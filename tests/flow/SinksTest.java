import sparta.checkers.quals.*;
import sparta.checkers.quals.Sinks.SPARTA_Permission;

class SinksTest {
    void sendData(@Sinks(SPARTA_Permission.NETWORK) Object p) {
        // Allowed: fewer sinks
        noComm(p);
    }

    //:: error: (forbidden.flow)
    void noComm(@Sinks({}) Object p) {
        // Forbidden: more sinks
        //:: error: (argument.type.incompatible)
        sendData(p);
    }

    void two(@Sinks({SPARTA_Permission.NETWORK, SPARTA_Permission.SMS}) Object p) {
        // Allowed: fewer sinks
        sendData(p);
        // Forbidden: more sinks
        //:: error: (argument.type.incompatible)
        any(p);
    }

    //:: error: (forbidden.flow)
    void any(@Sinks(SPARTA_Permission.ANY) Object p) {
        // Allowed: fewer sinks
        two(p);
    }

    // Null is legal for all sinks.
    void testNull() {
        any(null);
        two(null);
    }
}
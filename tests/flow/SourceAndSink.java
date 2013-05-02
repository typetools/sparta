import sparta.checkers.quals.*;
import sparta.checkers.quals.SPARTA_Permission;
import sparta.checkers.quals.SPARTA_Permission;

class SourceAndSink {
    //:: error: (forbidden.flow)
    @Sources(SPARTA_Permission.MICROPHONE) @Sinks({}) Object getPrivateSound() { return null; }
    @Sources(SPARTA_Permission.MICROPHONE) @Sinks(SPARTA_Permission.ANY) Object getPublicSound() { return null; }


    //:: error: (forbidden.flow)
    void sendAnyData(@Sources(SPARTA_Permission.ANY) @Sinks(SPARTA_Permission.NETWORK) Object p) {}
    void sendData(@Sources(SPARTA_Permission.LITERAL) @Sinks(SPARTA_Permission.NETWORK) Object p) {}

    void test1() {
        //:: error: (forbidden.flow)
        Object x = getPrivateSound();
        //:: error: (argument.type.incompatible)
        sendData(x);
    }

    void test2() {
        // even with the sinks annotation, we cannot allow this
        Object x = getPublicSound();
        // legal
        sendAnyData(x);
        //:: error: (argument.type.incompatible)
        sendData(x);
    }

    void test3() {
        //:: error: (assignment.type.incompatible)
        @Sinks(SPARTA_Permission.NETWORK) Object x = getPrivateSound();
    }
}
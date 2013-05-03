import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;
import sparta.checkers.quals.SpartaPermission;

class SourceAndSink {
    //:: error: (forbidden.flow)
    @Sources(SpartaPermission.MICROPHONE) @Sinks({}) Object getPrivateSound() { return null; }
    @Sources(SpartaPermission.MICROPHONE) @Sinks(SpartaPermission.ANY) Object getPublicSound() { return null; }


    //:: error: (forbidden.flow)
    void sendAnyData(@Sources(SpartaPermission.ANY) @Sinks(SpartaPermission.INTERNET) Object p) {}
    void sendData(@Sources(SpartaPermission.LITERAL) @Sinks(SpartaPermission.INTERNET) Object p) {}

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
        @Sinks(SpartaPermission.INTERNET) Object x = getPrivateSound();
    }
}
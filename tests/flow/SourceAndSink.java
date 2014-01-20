import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;
import sparta.checkers.quals.FlowPermission;

class SourceAndSink {
    @Source(RECORD_AUDIO) @Sink(CONDITIONAL) Object getPrivateSound() { return null; }
    @Source(RECORD_AUDIO) @Sink(ANY) Object getPublicSound() { return null; }

    //:: error: (forbidden.flow)
    void sendAnyData(@Source(ANY) @Sink(INTERNET) Object p) {}
    void sendData(@Source(LITERAL) @Sink(INTERNET) Object p) {}

    void test1() {

        Object x = getPrivateSound();
        //:: error: (argument.type.incompatible)
        sendData(x);
    }

    void test2() {

        Object x = getPublicSound();
        // legal
        sendAnyData(x);
        //sendData expect @Source(LITERAL)
        //:: error: (argument.type.incompatible)
        sendData(x);
    }

    void test3() {
        //:: error: (assignment.type.incompatible)
        @Sink(INTERNET) Object x = getPrivateSound();
    }
}

class SourceAndSinkOld {
    //:: error: (forbidden.flow)
    @Source(RECORD_AUDIO) @Sink({}) Object getPrivateSound() { return null; }
    @Source(RECORD_AUDIO) @Sink(ANY) Object getPublicSound() { return null; }

    //:: error: (forbidden.flow)
    void sendAnyData(@Source(ANY) @Sink(INTERNET) Object p) {}
    void sendData(@Source(LITERAL) @Sink(INTERNET) Object p) {}

    void test1() {
        //:: error: (forbidden.flow)
        Object x = getPrivateSound();
        //:: error: (forbidden.flow)
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
        //:: error: (forbidden.flow)
        @Sink(INTERNET) Object x = getPrivateSound();
    }
}
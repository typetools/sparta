import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.FlowPermission;

class SourceAndSink {
    //:: error: (forbidden.flow)
    @Source(FlowPermission.RECORD_AUDIO) @Sink({}) Object getPrivateSound() { return null; }
    @Source(FlowPermission.RECORD_AUDIO) @Sink(FlowPermission.ANY) Object getPublicSound() { return null; }


    //:: error: (forbidden.flow)
    void sendAnyData(@Source(FlowPermission.ANY) @Sink(FlowPermission.INTERNET) Object p) {}
    void sendData(@Source(FlowPermission.LITERAL) @Sink(FlowPermission.INTERNET) Object p) {}

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
        @Sink(FlowPermission.INTERNET) Object x = getPrivateSound();
    }
}
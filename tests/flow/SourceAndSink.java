import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.FlowSource;
import sparta.checkers.quals.Sinks.FlowSink;

class SourceAndSink {
    //:: error: (forbidden.flow)
    @Sources(FlowSource.MICROPHONE) @Sinks({}) Object getPrivateSound() { return null; }
    @Sources(FlowSource.MICROPHONE) @Sinks(FlowSink.ANY) Object getPublicSound() { return null; }


    //:: error: (forbidden.flow)
    void sendAnyData(@Sources(FlowSource.ANY) @Sinks(FlowSink.NETWORK) Object p) {}
    void sendData(@Sources(FlowSource.LITERAL) @Sinks(FlowSink.NETWORK) Object p) {}

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
        @Sinks(FlowSink.NETWORK) Object x = getPrivateSound();
    }
}
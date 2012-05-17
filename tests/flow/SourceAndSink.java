import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;

class SourceAndSink {
    @FlowSources(FlowSource.MICROPHONE) Object getPrivateSound() { return null; }
    @FlowSources(FlowSource.MICROPHONE) @FlowSinks(FlowSink.ANY) Object getPublicSound() { return null; }

    void sendAnyData(@FlowSources(FlowSource.ANY) @FlowSinks(FlowSink.NETWORK) Object p) {}
    void sendData(@FlowSinks(FlowSink.NETWORK) Object p) {}

    void test1() {
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
        @FlowSinks(FlowSink.NETWORK) Object x = getPrivateSound();
    }
}
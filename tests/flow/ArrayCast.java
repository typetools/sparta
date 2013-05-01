import sparta.checkers.quals.*;
import sparta.checkers.quals.Sinks.FlowSink;
import sparta.checkers.quals.Sources.FlowSource;
import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.PolySinks;
import sparta.checkers.quals.PolySources;

class ArrayCast {

    void foo() {
        //:: error: (assignment.type.incompatible)
        @Sinks(FlowSink.NETWORK) Object @Sources(FlowSource.ACCELEROMETER) [] params = new /*@Sinks(FlowSink.NETWORK)*/ Object[1];
        // Error only occurs when -Alint=cast:strict is used.

        //strict:: warning: (cast.unsafe)
        //:: error: (argument.type.incompatible)
        Object[] result = (Object[]) call("method", params);

        // The annotations are on the array type, not on the array component type.
        //:: error: (argument.type.incompatible)
        callStart(result);
        callFinished(result);

        @Sinks(FlowSink.NETWORK) Object [] otherOne = getObjs();
        callStart(getObjs());
    }

    @Sources(FlowSource.NETWORK) @Sinks(FlowSink.NETWORK) Object call(
            @Sources(FlowSource.LITERAL) String method, @Sinks(FlowSink.NETWORK) Object[] params) {
        @Sources(FlowSource.NETWORK) @Sinks(FlowSink.NETWORK) Object a = params[0];
        return a;
    }

    void callStart(@Sources(FlowSource.NETWORK) @Sinks(FlowSink.NETWORK) Object []  result) {}
    void callFinished(Object @Sources(FlowSource.NETWORK) @Sinks(FlowSink.NETWORK) [] result) {}

    @Sinks(FlowSink.NETWORK) Object [] getObjs() {
        return null;
    }
}

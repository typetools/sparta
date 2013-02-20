import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSinks.FlowSink;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks;
import sparta.checkers.quals.PolyFlowSinks;
import sparta.checkers.quals.PolyFlowSources;

class ArrayCast {

    void foo() {
        //:: error: (assignment.type.incompatible)
        @FlowSinks(FlowSink.NETWORK) Object @FlowSources(FlowSource.ACCELEROMETER) [] params = new /*@FlowSinks(FlowSink.NETWORK)*/ Object[1];
        // Error only occurs when -Alint=cast:strict is used.

        //strict:: warning: (cast.unsafe)
        //:: error: (argument.type.incompatible)
        Object[] result = (Object[]) call("method", params);

        // The annotations are on the array type, not on the array component type.
        //:: error: (argument.type.incompatible)
        callStart(result);
        callFinished(result);

        @FlowSinks(FlowSink.NETWORK) Object [] other = getObjs();
        callStart(getObjs());
    }

    @FlowSources(FlowSource.NETWORK) @FlowSinks(FlowSink.NETWORK) Object call(
            @FlowSources(FlowSource.LITERAL) String method, @FlowSinks(FlowSink.NETWORK) Object[] params) {
        @FlowSources(FlowSource.NETWORK) @FlowSinks(FlowSink.NETWORK) Object a = params[0];
        return a;
    }

    void callStart(@FlowSources(FlowSource.NETWORK) @FlowSinks(FlowSink.NETWORK) Object []  result) {}
    void callFinished(Object @FlowSources(FlowSource.NETWORK) @FlowSinks(FlowSink.NETWORK) [] result) {}

    @FlowSinks(FlowSink.NETWORK) Object [] getObjs() {
        return null;
    }
}

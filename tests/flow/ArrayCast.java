import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;

class ArrayCast {

    void foo() {
        //:: error: (assignment.type.incompatible)
        @Sink(FlowPermission.INTERNET) Object @Source(FlowPermission.ACCELEROMETER) [] params = new /*@Sink(FlowPermission.INTERNET)*/ Object[1];
        // Error only occurs when -Alint=cast:strict is used.

        //strict:: warning: (cast.unsafe)
        //:: error: (argument.type.incompatible)
        Object[] result = (Object[]) call("method", params);

        // The annotations are on the array type, not on the array component type.
        //:: error: (argument.type.incompatible)
        callStart(result);
        callFinished(result);

        @Sink(FlowPermission.INTERNET) Object [] otherOne = getObjs();
        callStart(getObjs());
    }

    @Source(FlowPermission.INTERNET) @Sink(FlowPermission.INTERNET) Object call(
            @Source(FlowPermission.LITERAL) String method, @Sink(FlowPermission.INTERNET) Object[] params) {
        @Source(FlowPermission.INTERNET) @Sink(FlowPermission.INTERNET) Object a = params[0];
        return a;
    }

    void callStart(@Source(FlowPermission.INTERNET) @Sink(FlowPermission.INTERNET) Object []  result) {}
    void callFinished(Object @Source(FlowPermission.INTERNET) @Sink(FlowPermission.INTERNET) [] result) {}

    @Sink(FlowPermission.INTERNET) Object [] getObjs() {
        return null;
    }
}

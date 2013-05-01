import sparta.checkers.quals.*;
import sparta.checkers.quals.Sinks.SPARTA_Permission;
import sparta.checkers.quals.Sources.SPARTA_Permission;
import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.PolySinks;
import sparta.checkers.quals.PolySources;

class ArrayCast {

    void foo() {
        //:: error: (assignment.type.incompatible)
        @Sinks(SPARTA_Permission.NETWORK) Object @Sources(SPARTA_Permission.ACCELEROMETER) [] params = new /*@Sinks(SPARTA_Permission.NETWORK)*/ Object[1];
        // Error only occurs when -Alint=cast:strict is used.

        //strict:: warning: (cast.unsafe)
        //:: error: (argument.type.incompatible)
        Object[] result = (Object[]) call("method", params);

        // The annotations are on the array type, not on the array component type.
        //:: error: (argument.type.incompatible)
        callStart(result);
        callFinished(result);

        @Sinks(SPARTA_Permission.NETWORK) Object [] otherOne = getObjs();
        callStart(getObjs());
    }

    @Sources(SPARTA_Permission.NETWORK) @Sinks(SPARTA_Permission.NETWORK) Object call(
            @Sources(SPARTA_Permission.LITERAL) String method, @Sinks(SPARTA_Permission.NETWORK) Object[] params) {
        @Sources(SPARTA_Permission.NETWORK) @Sinks(SPARTA_Permission.NETWORK) Object a = params[0];
        return a;
    }

    void callStart(@Sources(SPARTA_Permission.NETWORK) @Sinks(SPARTA_Permission.NETWORK) Object []  result) {}
    void callFinished(Object @Sources(SPARTA_Permission.NETWORK) @Sinks(SPARTA_Permission.NETWORK) [] result) {}

    @Sinks(SPARTA_Permission.NETWORK) Object [] getObjs() {
        return null;
    }
}

import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;
import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.PolySinks;
import sparta.checkers.quals.PolySources;

class ArrayCast {

    void foo() {
        //:: error: (assignment.type.incompatible)
        @Sinks(SpartaPermission.INTERNET) Object @Sources(SpartaPermission.ACCELEROMETER) [] params = new /*@Sinks(SpartaPermission.INTERNET)*/ Object[1];
        // Error only occurs when -Alint=cast:strict is used.

        //strict:: warning: (cast.unsafe)
        //:: error: (argument.type.incompatible)
        Object[] result = (Object[]) call("method", params);

        // The annotations are on the array type, not on the array component type.
        //:: error: (argument.type.incompatible)
        callStart(result);
        callFinished(result);

        @Sinks(SpartaPermission.INTERNET) Object [] otherOne = getObjs();
        callStart(getObjs());
    }

    @Sources(SpartaPermission.INTERNET) @Sinks(SpartaPermission.INTERNET) Object call(
            @Sources(SpartaPermission.LITERAL) String method, @Sinks(SpartaPermission.INTERNET) Object[] params) {
        @Sources(SpartaPermission.INTERNET) @Sinks(SpartaPermission.INTERNET) Object a = params[0];
        return a;
    }

    void callStart(@Sources(SpartaPermission.INTERNET) @Sinks(SpartaPermission.INTERNET) Object []  result) {}
    void callFinished(Object @Sources(SpartaPermission.INTERNET) @Sinks(SpartaPermission.INTERNET) [] result) {}

    @Sinks(SpartaPermission.INTERNET) Object [] getObjs() {
        return null;
    }
}

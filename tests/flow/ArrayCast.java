import static sparta.checkers.quals.FlowPermission.*;
import sparta.checkers.quals.*;

class ArrayCast {
void bar(){
    @Source(INTERNET) byte /*@Source(CAMERA)*/ [] ba;
}
    
    void foo() {
        //:: error: (assignment.type.incompatible)
        @Sink(INTERNET) Object @Source(FlowPermission.ACCELEROMETER) [] params = new /*@Sink(INTERNET)*/ Object[1];
        // Error only occurs when -Alint=cast:strict is used.

        //strict:: warning: (cast.unsafe)
        //:: error: (argument.type.incompatible)
        Object[] result = (Object[]) call("method", params);

        // The annotations are on the array type, not on the array component type.
        //:: error: (argument.type.incompatible)
        callStart(result);
        callFinished(result);

        @Sink(INTERNET) Object [] otherOne = getObjs();
        callStart(getObjs());
    }

    @Source(INTERNET) @Sink(INTERNET) Object call(
            @Source(FlowPermission.LITERAL) String method, @Sink(INTERNET) Object[] params) {
        @Source(INTERNET) @Sink(INTERNET) Object a = params[0];
        return a;
    }

    void callStart(@Source(INTERNET) @Sink(INTERNET) Object []  result) {}
    void callFinished(Object @Source(INTERNET) @Sink(INTERNET) [] result) {}

    @Sink(INTERNET) Object [] getObjs() {
        return null;
    }
}

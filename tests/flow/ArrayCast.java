import static sparta.checkers.quals.FlowPermission.*;

import java.io.ByteArrayOutputStream;

import sparta.checkers.quals.*;

class ArrayCast {
void bar(){
    @Source(INTERNET) byte /*@Source(CAMERA)*/ [] ba;
    @Source(CAMERA) byte /*@Source(CAMERA)*/ [] ba2;

    @Source(CAMERA) ByteArrayOutputStream bytes = getBAOS();
    @Source(CAMERA) byte /*@Source(CAMERA)*/ [] bas = bytes.toByteArray();
    
    //Make sure the current work around for the constructor bug is working:
    @SuppressWarnings("flow")
    ByteArrayOutputStream bytes2 = (/*@Source(CAMERA)@Sink({ DISPLAY})*/ ByteArrayOutputStream) new ByteArrayOutputStream();
    @Source(CAMERA) byte /*@Source(CAMERA)*/ [] bas2 = bytes2.toByteArray();


}

@Source(CAMERA) ByteArrayOutputStream getBAOS(){    
    @SuppressWarnings("flow")
    @Source(CAMERA) ByteArrayOutputStream baos = ( @Source(CAMERA) ByteArrayOutputStream)  new ByteArrayOutputStream();
    return baos;
}
    
    void foo() {
        
        @Sink(INTERNET) Object @Source(FlowPermission.ACCELEROMETER) [] params = new /*@Sink(INTERNET)*/ Object[1];
        // Error only occurs when -Alint=cast:strict is used.
      //strict:: warning: (cast.unsafe)
        Object[] result = (Object[]) call("method", params);

        // The annotations are on the array type, not on the array component type.
        //:: error: (argument.type.incompatible)
        callStart(result);
        callFinished(result);

        @Sink(INTERNET) Object [] otherOne = getObjs();
        callStart(getObjs());
    }

    @Source(INTERNET) @Sink(INTERNET) Object call(
            @Source({}) String method, @Sink(INTERNET) Object[] params) {
        @Source(INTERNET) @Sink(INTERNET) Object a = params[0];
        return a;
    }

    void callStart(@Source(INTERNET) @Sink(INTERNET) Object []  result) {}
    void callFinished(Object @Source(INTERNET) @Sink(INTERNET) [] result) {}

    @Sink(INTERNET) Object [] getObjs() {
        return null;
    }
}

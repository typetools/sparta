//import android.util.FloatMath;
import sparta.checkers.quals.*;
import sparta.checkers.quals.ParameterizedFlowPermission;
import sparta.checkers.quals.ParameterizedFlowPermission;

public class Poly {
    @PolyFlow
    Object testSink(Object in) { return in; }

    void callSink(@Sink(FlowPermission.WRITE_EMAIL) Object s) { 
	@Sink(FlowPermission.WRITE_EMAIL)  Object l = 
		testSink(s); 
    }

    @PolySink @Source(FlowPermission.CAMERA) Object
    testComb(@PolySink @Source(FlowPermission.CAMERA) Object in) { return in; }

    void callComb(@Sink(FlowPermission.WRITE_EMAIL) Object s) {
        @Sink(FlowPermission.WRITE_EMAIL) @Source(FlowPermission.CAMERA) Object l = testComb(s);
        //:: error: (assignment.type.incompatible)
        @Sink(FlowPermission.INTERNET) @Source(FlowPermission.CAMERA) Object l2 = testComb(s); 
    }

    @Source(FlowPermission.ACCESS_FINE_LOCATION) float y;
    void test_floatmath() {
//        y = FloatMath.sin(y);
    }
    //Rename NOFLOW to default flow

}
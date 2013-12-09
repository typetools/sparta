//import android.util.FloatMath;
import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.FlowPermission;

public class Poly {
    @PolyFlow
    Object testSink(Object in) { return in; }

    void callSink(@Sink(CoarseFlowPermission.WRITE_EMAIL) Object s) { 
	@Sink(CoarseFlowPermission.WRITE_EMAIL)  Object l = 
		testSink(s); 
    }

    @PolySink @Source(CoarseFlowPermission.CAMERA) Object
    testComb(@PolySink @Source(CoarseFlowPermission.CAMERA) Object in) { return in; }

    void callComb(@Sink(CoarseFlowPermission.WRITE_EMAIL) Object s) {
        @Sink(CoarseFlowPermission.WRITE_EMAIL) @Source(CoarseFlowPermission.CAMERA) Object l = testComb(s);
        //:: error: (assignment.type.incompatible)
        @Sink(CoarseFlowPermission.INTERNET) @Source(CoarseFlowPermission.CAMERA) Object l2 = testComb(s); 
    }

    @Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) float y;
    void test_floatmath() {
//        y = FloatMath.sin(y);
    }
    //Rename NOFLOW to default flow

}
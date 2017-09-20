//import android.util.FloatMath;
import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.FlowPermission;

public class Poly {
    @PolyFlow
    Object testSink(Object in) { return in; }

    void callSink(@Sink(FlowPermissionString.WRITE_EMAIL) Object s) {
	@Sink(FlowPermissionString.WRITE_EMAIL)  Object l =
		testSink(s);
    }

    @PolySink @Source(FlowPermissionString.CAMERA) Object
    testComb(@PolySink @Source(FlowPermissionString.CAMERA) Object in) { return in; }

    void callComb(@Sink(FlowPermissionString.WRITE_EMAIL) Object s) {
        @Sink(FlowPermissionString.WRITE_EMAIL) @Source(FlowPermissionString.CAMERA) Object l = testComb(s);
        //:: error: (assignment.type.incompatible)
        @Sink(FlowPermissionString.INTERNET) @Source(FlowPermissionString.CAMERA) Object l2 = testComb(s);
    }

    @Source(FlowPermissionString.ACCESS_FINE_LOCATION) float y;
    void test_floatmath() {
//        y = FloatMath.sin(y);
    }
    //Rename NOFLOW to default flow

}
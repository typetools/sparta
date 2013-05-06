//import android.util.FloatMath;
import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;
import sparta.checkers.quals.SpartaPermission;

public class Poly {
    @PolyFlow
    Object testSinks(Object in) { return in; }

    void callSinks(@Sinks(SpartaPermission.EMAIL) Object s) { 
	@Sinks(SpartaPermission.EMAIL)  Object l = 
		testSinks(s); 
    }

    @PolySinks @Sources(SpartaPermission.CAMERA) Object
    testComb(@PolySinks @Sources(SpartaPermission.CAMERA) Object in) { return in; }

    void callComb(@Sinks(SpartaPermission.EMAIL) Object s) {
        @Sinks(SpartaPermission.EMAIL) @Sources(SpartaPermission.CAMERA) Object l = testComb(s);
        //:: error: (assignment.type.incompatible)
        @Sinks(SpartaPermission.INTERNET) @Sources(SpartaPermission.CAMERA) Object l2 = testComb(s); 
    }

    @Sources(SpartaPermission.ACCESS_FINE_LOCATION) float y;
    void test_floatmath() {
//        y = FloatMath.sin(y);
    }
    //Rename NOFLOW to default flow

}
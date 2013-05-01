//import android.util.FloatMath;
import sparta.checkers.quals.*;
import sparta.checkers.quals.Sinks.SPARTA_Permission;
import sparta.checkers.quals.Sources.SPARTA_Permission;

public class Poly {
    @PolyFlow
    Object testSinks(Object in) { return in; }

    void callSinks(@Sinks(SPARTA_Permission.EMAIL) Object s) { 
	@Sinks(SPARTA_Permission.EMAIL)  Object l = 
		testSinks(s); 
    }

    @PolySinks @Sources(SPARTA_Permission.CAMERA) Object
    testComb(@PolySinks @Sources(SPARTA_Permission.CAMERA) Object in) { return in; }

    void callComb(@Sinks(Sinks.SPARTA_Permission.EMAIL) Object s) {
        @Sinks(Sinks.SPARTA_Permission.EMAIL) @Sources(SPARTA_Permission.CAMERA) Object l = testComb(s);
        //:: error: (assignment.type.incompatible)
        @Sinks(Sinks.SPARTA_Permission.NETWORK) @Sources(SPARTA_Permission.CAMERA) Object l2 = testComb(s); 
    }

    @Sources(SPARTA_Permission.LOCATION) float y;
    void test_floatmath() {
//        y = FloatMath.sin(y);
    }
    //Rename NOFLOW to default flow

}
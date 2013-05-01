//import android.util.FloatMath;
import sparta.checkers.quals.*;
import sparta.checkers.quals.Sinks.FlowSink;
import sparta.checkers.quals.Sources.FlowSource;

public class Poly {
    @PolyFlow
    Object testSinks(Object in) { return in; }

    void callSinks(@Sinks(FlowSink.EMAIL) Object s) { 
	@Sinks(FlowSink.EMAIL)  Object l = 
		testSinks(s); 
    }

    @PolySinks @Sources(FlowSource.CAMERA) Object
    testComb(@PolySinks @Sources(FlowSource.CAMERA) Object in) { return in; }

    void callComb(@Sinks(Sinks.FlowSink.EMAIL) Object s) {
        @Sinks(Sinks.FlowSink.EMAIL) @Sources(FlowSource.CAMERA) Object l = testComb(s);
        //:: error: (assignment.type.incompatible)
        @Sinks(Sinks.FlowSink.NETWORK) @Sources(FlowSource.CAMERA) Object l2 = testComb(s); 
    }

    @Sources(FlowSource.LOCATION) float y;
    void test_floatmath() {
//        y = FloatMath.sin(y);
    }
    //Rename NOFLOW to default flow

}
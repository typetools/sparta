//import android.util.FloatMath;
import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSinks.FlowSink;
import sparta.checkers.quals.FlowSources.FlowSource;

public class Poly {
    @PolyFlow
    Object testSinks(Object in) { return in; }

    void callSinks(@FlowSinks(FlowSink.EMAIL) Object s) { 
	@FlowSinks(FlowSink.EMAIL)  Object l = 
		testSinks(s); 
    }

    @PolyFlowSinks @FlowSources(FlowSource.CAMERA) Object
    testComb(@PolyFlowSinks @FlowSources(FlowSource.CAMERA) Object in) { return in; }

    void callComb(@FlowSinks(FlowSinks.FlowSink.EMAIL) Object s) {
        @FlowSinks(FlowSinks.FlowSink.EMAIL) @FlowSources(FlowSource.CAMERA) Object l = testComb(s);
        //:: error: (assignment.type.incompatible)
        @FlowSinks(FlowSinks.FlowSink.NETWORK) @FlowSources(FlowSource.CAMERA) Object l2 = testComb(s); 
    }

    @FlowSources(FlowSource.LOCATION) float y;
    void test_floatmath() {
//        y = FloatMath.sin(y);
    }
    //Rename NOFLOW to default flow

}
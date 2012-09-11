import checkers.quals.PolyAll;
import android.app.Activity;
import android.view.*;
import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;

class NoFlowTest {
	@FlowSources(FlowSource.NETWORK) int source = 1;
	@SuppressWarnings("flow") //Variable initialization
	@FlowSources({}) @FlowSinks(FlowSink.NETWORK) int sink = 2;
	@SuppressWarnings("flow") //Variable initialization
	@FlowSources(FlowSource.NETWORK) @FlowSinks(FlowSink.NETWORK) int sourceSink = 3;
	@FlowSources({}) @FlowSinks({}) int none = 4;
	
	@FlowSources(FlowSource.NETWORK) TestClass classSource;
	@SuppressWarnings("flow") //Variable initialization
	@FlowSinks(FlowSink.NETWORK) TestClass classSink;
	@SuppressWarnings("flow") //Variable initialization
	@FlowSources(FlowSource.NETWORK) @FlowSinks(FlowSink.NETWORK) TestClass classSourceSink;
	@FlowSources({}) @FlowSinks({}) TestClass classNone;
	
	//test on return, receiver, and whole class
	void testParameter() {
		 //:: error: (argument.type.incompatible)
		testMethod(source);
		testMethod(sink);
		 //:: error: (argument.type.incompatible)
		testMethod(sourceSink);
		testMethod(none);
	}
	
	void testReturn() {
		source = testMethod(0);
		//:: error: (assignment.type.incompatible)
		sink = testMethod(0);
		//:: error: (assignment.type.incompatible)
		sourceSink = testMethod(0);
		none = testMethod(0);
	}
	
	void testReceiver() {
		none = classSource.testClassMethod(none);
		none = classSink.testClassMethod(none);
		none = classSourceSink.testClassMethod(none);
		none = classNone.testClassMethod(none);
	}
	
	
	void testClass() {
		//:: error: (argument.type.incompatible)
		source = classNone.testClassMethod(source);
		none = classNone.testClassMethod(sink);
		//:: error: (assignment.type.incompatible)
		sink = classNone.testClassMethod(sink);
		//:: error: (argument.type.incompatible)
		none = classNone.testClassMethod(sourceSink);
		//:: error: (argument.type.incompatible) :: error: (assignment.type.incompatible)
		sourceSink = classNone.testClassMethod(sourceSink);
		none = classNone.testClassMethod(none);
	}

	// This test assumes that SurfaceView.getHolder is annotated
	// with @NoFlow. If this test fails, checks whether that specification changed.
	// If so, change it to some other API that uses @NoFlow.
	SurfaceHolder sHolder;
	void testAndroid(SurfaceView view) {
		sHolder = view.getHolder(); 
	}
	
	@NoFlow
	int testMethod(int input) {
		return 0;
	}
}

@NoFlow
class TestClass {
	int testClassMethod(@PolyAll TestClass this, int input) {
		return 0;
	}
}
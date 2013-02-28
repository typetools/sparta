import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;

class NoFlowTest {
	@FlowSources({FlowSource.NETWORK, FlowSource.LITERAL}) int source = 1;
    @FlowSinks(FlowSink.NETWORK) int sink = 2;
	@FlowSources({FlowSource.NETWORK, FlowSource.LITERAL}) @FlowSinks(FlowSink.NETWORK) int sourceSink = 3;
	@SuppressWarnings("flow")
	@FlowSources({}) @FlowSinks({}) int none = 4;
	
//	@FlowSources(FlowSource.NETWORK) TestClass classSource;
//	@FlowSinks(FlowSink.NETWORK) TestClass classSink;
//	@FlowSources(FlowSource.NETWORK) @FlowSinks(FlowSink.NETWORK) TestClass classSourceSink;
//	@FlowSources({}) @FlowSinks({}) TestClass classNone;
//	
//	//test on return, receiver, and whole class
//	void testParameter() {
//		 //:: error: (argument.type.incompatible)
//		testMethod(source);
//		testMethod(sink);
//		 //:: error: (argument.type.incompatible)
//		testMethod(sourceSink);
//		testMethod(none);
//	}
//	
//	void testReturn() {
//		source = testMethod(0);
//		//:: error: (assignment.type.incompatible)
//		sink = testMethod(0);
//		//:: error: (assignment.type.incompatible)
//		sourceSink = testMethod(0);
//		none = testMethod(0);
//	}
//	
//	void testReceiver() {
//		none = classSource.testClassMethod(none);
//		none = classSink.testClassMethod(none);
//		none = classSourceSink.testClassMethod(none);
//		none = classNone.testClassMethod(none);
//	}
//	
//	
//	void testClass() {
//		//:: error: (argument.type.incompatible)
//		source = classNone.testClassMethod(source);
//		none = classNone.testClassMethod(sink);
//		//:: error: (assignment.type.incompatible)
//		sink = classNone.testClassMethod(sink);
//		//:: error: (argument.type.incompatible)
//		none = classNone.testClassMethod(sourceSink);
//		//:: error: (argument.type.incompatible) :: error: (assignment.type.incompatible)
//		sourceSink = classNone.testClassMethod(sourceSink);
//		none = classNone.testClassMethod(none);
//	}
//
//	
//	@NoFlow
//	int testMethod(int input) {
//		return 0;
//	}
//}
//
//@NoFlow
//class TestClass {
//	int testClassMethod( int input) {
//		return 0;
//	}
}
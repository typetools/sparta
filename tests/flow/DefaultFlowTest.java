
import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import static sparta.checkers.quals.FlowPermission.*;

class DefaultFlowTest {
	@Source({INTERNET,LITERAL}) int source = 1;
        @Sink(INTERNET) int sink = 2;
	@Source({INTERNET,LITERAL}) @Sink(INTERNET) int sourceSink = 3;
	int none = 4;
	
	@Source(INTERNET) TestClass classSource;
	@Sink(INTERNET) TestClass classSink;
	@Source(INTERNET) @Sink(INTERNET) TestClass classSourceSink;
	TestClass classNone;
	
	
	int testMethod(int input) {
		return 0;
	}
	//test on return, receiver, and whole class
	void testParameter() {

	        testMethod(source);
		 //:: error: (argument.type.incompatible)
		testMethod(sink);
		 //:: error: (argument.type.incompatible)
		testMethod(sourceSink);
		testMethod(none);
	}
	
	void testReturn() {
		source = testMethod(0);
		sink = testMethod(0);
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
		source = classNone.testClassMethod(source);
		//:: error: (argument.type.incompatible)
		none = classNone.testClassMethod(sink);
		//:: error: (argument.type.incompatible)
		sink = classNone.testClassMethod(sink);
		//:: error: (argument.type.incompatible)
		none = classNone.testClassMethod(sourceSink);
		//:: error: (argument.type.incompatible)
		sourceSink = classNone.testClassMethod(sourceSink);
		none = classNone.testClassMethod(none);
	}

	// This test assumes that SurfaceView.getHolder is annotated
	// with @DefaultFlow. If this test fails, checks whether that specification changed.
	// If so, change it to some other API that uses @DefaultFlow.
//	SurfaceHolder sHolder;
//	void testAndroidMethod(SurfaceView view) {
//		sHolder = view.getHolder(); 
//	}
	
	// This test assumes that class ProgressDialog is annotated with @DefaultFlow.
	// An annotation on the class should make all methods default to @DefaultFlow.
//	ProgressDialog pd;
//	void testAndroidClass(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable) {
//		pd = ProgressDialog.show(context, title, message, indeterminate, cancelable);
//	}
	

}


class TestClass {
	int testClassMethod(@PolySource @PolySink TestClass this, int input) {
		return 0;
	}
}
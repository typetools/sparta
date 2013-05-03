import checkers.quals.PolyAll;

import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;
import sparta.checkers.quals.SpartaPermission;

class DefaultFlowTest {
	@Sources({SpartaPermission.INTERNET,SpartaPermission.LITERAL}) int source = 1;
        @Sinks(SpartaPermission.INTERNET) int sink = 2;
	@Sources({SpartaPermission.INTERNET,SpartaPermission.LITERAL}) @Sinks(SpartaPermission.INTERNET) int sourceSink = 3;
	int none = 4;
	
	@Sources(SpartaPermission.INTERNET) TestClass classSource;
	@Sinks(SpartaPermission.INTERNET) TestClass classSink;
	@Sources(SpartaPermission.INTERNET) @Sinks(SpartaPermission.INTERNET) TestClass classSourceSink;
	TestClass classNone;
	
	@DefaultFlow
	int testMethod(int input) {
		return 0;
	}
	//test on return, receiver, and whole class
	void testParameter() {
              //:: error: (argument.type.incompatible)
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
		//:: error: (argument.type.incompatible)
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

@DefaultFlow
class TestClass {
	int testClassMethod(@PolySources @PolySinks TestClass this, int input) {
		return 0;
	}
}
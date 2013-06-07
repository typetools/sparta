import sparta.checkers.quals.ConservativeFlow;
import sparta.checkers.quals.Source;
import sparta.checkers.quals.Sink;

import static sparta.checkers.quals.FlowPermission.*;

@ConservativeFlow
class TestImplicitConstructor { }

class TestNoParamConstructor {
	@ConservativeFlow
	TestNoParamConstructor() { }
}
@ConservativeFlow
class TestParamConstructor {

	//:: error: (forbidden.flow)   
	TestParamConstructor(String name) { }
	
	//:: error: (forbidden.flow)
	static void test(String test) { }
}

class TestExplicitConstructorType {
	 @Source(INTERNET) @Sink(CAMERA)
	 TestExplicitConstructorType()  { }
}

@Source(INTERNET) @Sink(CAMERA)
class TestClassAnnotationType {
	TestClassAnnotationType()  { }
}


class ConstructorAnnotation {

	void testConstructor() {
		
		// Sanity check -- OK
		//:: error: (argument.type.incompatible)
		TestParamConstructor.test("test");

		// OK -- Conservative flow on parameters
		//:: error: (argument.type.incompatible)
		new TestParamConstructor("hello");
		
		// BUG? Class annotations dont seem to do anything.
		//:: error: (assignment.type.incompatible)
		@Source(INTERNET) @Sink(CAMERA) TestClassAnnotationType classAnnotation = new TestClassAnnotationType();
		
		// BUG? Constructor annotations
		// We get invocation.invalid here based on the reciever type.
		// Is TestExplicitConstructor constructor annotation doing the correct thing?
		// 
		// error: creation of @Sink(FlowPermission.CAMERA) @Source(FlowPermission.INTERNET) void 
		// <init>(@Sink(CONDITIONAL) @Source(LITERAL) TestExplicitConstructorType this) 
		// not allowed with given receiver;
		// 
		// found   : @Sink(FlowPermission.CAMERA) @Source(FlowPermission.INTERNET) TestExplicitConstructorType
		// required: @Sink(CONDITIONAL) @Source(LITERAL) TestExplicitConstructorType
		//:: error: (constructor.invocation.invalid)
		new TestExplicitConstructorType();
		
		// Conservative flow return types.
		
		// BUG? This should be thrown //:: error: (assignment.type.incompatible) 
		@Source(LITERAL) TestImplicitConstructor imp = new TestImplicitConstructor();
		// BUG? This should be thrown //:: error: (assignment.type.incompatible)
		@Source(LITERAL) TestNoParamConstructor noParam = new TestNoParamConstructor();

	}
}

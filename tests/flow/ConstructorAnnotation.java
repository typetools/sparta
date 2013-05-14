import sparta.checkers.quals.ConservativeFlow;
import sparta.checkers.quals.Source;
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
	 /*@Source(INTERNET)*/
	 TestExplicitConstructorType()  { }
}


class ConstructorAnnotation {

	void testConstructor() {
		
		// BUG? This should be thrown //:: error: (assignment.type.incompatible) 
		@Source(LITERAL) TestImplicitConstructor imp = new TestImplicitConstructor();
		// BUG? This should be thrown //:: error: (assignment.type.incompatible)
		@Source(LITERAL) TestNoParamConstructor noParam = new TestNoParamConstructor();

		//:: error: (argument.type.incompatible)
		@Source(LITERAL) TestParamConstructor param = new TestParamConstructor("hello");
		
		//:: error: (argument.type.incompatible)
		new TestParamConstructor("hello");
		
		// Sanity check -- OK
		//:: error: (argument.type.incompatible)
		TestParamConstructor.test("test");
		
		// BUG? This error should be thrown 
		//:: error: (assignment.type.incompatible)
		@Source(CAMERA) TestExplicitConstructorType explicit = new TestExplicitConstructorType();

		// Until recently this threw an error about constructor invocation...
		//:: error: (constructor.invocation.invalid)
		@Source(INTERNET) TestExplicitConstructorType explicit2 = new TestExplicitConstructorType();
	}
}

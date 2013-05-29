import sparta.checkers.quals.Source;
import static sparta.checkers.quals.FlowPermission.*;



class TestStubImplicitConstructor { 
	
	//:: error: (forbidden.flow)
	static void stubSanity(String fail) { }
}

class TestStubNoParamConstructor {
	TestStubNoParamConstructor() { }
	
	//:: error: (forbidden.flow)
	static void stubSanity(String fail) { }
}

class TestStubParamConstructor {
	
	//:: error: (forbidden.flow)   
	TestStubParamConstructor(String name) { }
	
	//:: error: (forbidden.flow)
	static void stubSanity(String fail) { }
}

class TestStubExplicitConstructorType {

	TestStubExplicitConstructorType()  { }
	
	//:: error: (forbidden.flow)
    static void stubSanity(String fail) { }
}


class ConstructorStubAnnotation {

	void testConstructor() {
		// Make sure that the stub files are actually working (pointing to the right class, etc).
		//:: error: (argument.type.incompatible)
		TestStubImplicitConstructor.stubSanity("test");
		//:: error: (argument.type.incompatible)
		TestStubNoParamConstructor.stubSanity("test");
		//:: error: (argument.type.incompatible)
		TestStubParamConstructor.stubSanity("test");
		//:: error: (argument.type.incompatible)
		TestStubExplicitConstructorType.stubSanity("test");
		
		// BUG? This should be thrown //:: error: (assignment.type.incompatible) 
		@Source(LITERAL) TestStubImplicitConstructor imp = new TestStubImplicitConstructor();
		// BUG? This should be thrown //:: error: (assignment.type.incompatible)
		@Source(LITERAL) TestStubNoParamConstructor noParam = new TestStubNoParamConstructor();
		
		//:: error: (argument.type.incompatible)
		@Source(LITERAL) TestStubParamConstructor param = new TestStubParamConstructor("hello");
		//:: error: (argument.type.incompatible)
		new TestStubParamConstructor("hello");
		
		// BUG? An error should be thrown (Constructor is explicity @Source(INTERNET)) 
		// //:: error: (assignment.type.incompatible)
		@Source(LITERAL) TestStubExplicitConstructorType explicit = new TestStubExplicitConstructorType();

		// This has different behaviour than the ConstructorAnnotation test...
		// //:: error: (constructor.invocation.invalid)
		//:: error: (assignment.type.incompatible)
		@Source(INTERNET) TestStubExplicitConstructorType explicit2 = new TestStubExplicitConstructorType();
	}
}
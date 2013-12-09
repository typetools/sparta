import checkers.quals.FromByteCode;
import sparta.checkers.quals.Source;
import sparta.checkers.quals.Sink;

import static sparta.checkers.quals.CoarseFlowPermission.*;

@FromByteCode
class TestImplicitConstructor { }

class TestNoParamConstructor {
    @FromByteCode
    TestNoParamConstructor() { }
}
@FromByteCode
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
        // OK -- Conservative flow on parameter
        //:: error: (argument.type.incompatible)  :: error: (forbidden.flow)
        new TestParamConstructor("hello");
        
        @Source(INTERNET) @Sink(CAMERA) TestClassAnnotationType classAnnotation = new TestClassAnnotationType();
        
        @Source(INTERNET) @Sink(CAMERA) TestExplicitConstructorType constructorAnnotation = new TestExplicitConstructorType();
        
        // Conservative flow return types.
        
        // BUG? This should be thrown //:: error: (assignment.type.incompatible) 

        //:: error: (forbidden.flow) :: error: (forbidden.flow)
        TestImplicitConstructor imp = new TestImplicitConstructor();
        // BUG? This should be thrown //:: error: (assignment.type.incompatible)
        //:: error: (forbidden.flow) :: error: (forbidden.flow)
        TestNoParamConstructor noParam = new TestNoParamConstructor();

    }
}

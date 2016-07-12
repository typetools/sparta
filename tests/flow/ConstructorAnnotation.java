import org.checkerframework.framework.qual.FromByteCode;

import sparta.checkers.quals.Source;
import sparta.checkers.quals.Sink;

import static sparta.checkers.quals.FlowPermissionString.*;
//warning: FlowPolicy: Found transitive flows:
@SuppressWarnings("flow")
@FromByteCode
class TestImplicitConstructor { }

@SuppressWarnings("flow")
class TestNoParamConstructor {
    @FromByteCode
    TestNoParamConstructor() { }
}
@SuppressWarnings("flow")
@FromByteCode
class TestParamConstructor {

    @FromByteCode
    TestParamConstructor(String name) { }
    @FromByteCode
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
@Source(ANY) @Sink({}) String top;
    void testConstructor() {

        // Sanity check -- OK
        //:: error: (argument.type.incompatible)
        TestParamConstructor.test(top);
        // OK -- Conservative flow on parameter
        //:: error: (argument.type.incompatible)  :: error: (forbidden.flow)
        new TestParamConstructor(top);

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

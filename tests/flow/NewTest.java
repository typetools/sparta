import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.FlowPermission;

import java.io.*;
import java.net.*;

// Test for annotations on constructor returns and polymorphism
// with constructors.
class NewTest {
    @Sink(FlowPermission.INTERNET) TestClass1 sink;

    @Sink(FlowPermission.INTERNET) String param = "jkl";

    void method1() {
        sink = new TestClass1(param);
        sink = new @Sink({FlowPermission.INTERNET, FlowPermission.CONDITIONAL}) TestClass1(param);
        //:: error: (assignment.type.incompatible)
        sink = new @Sink(FlowPermission.FILESYSTEM) TestClass1(param);
    }

    @Sink(FlowPermission.INTERNET) TestClass3 sink3;

    void method3() {
        // Subset of sinks can be specified at instantiation.
        sink3 = new @Sink(FlowPermission.INTERNET) TestClass3(param);
    }

    TestClass2 unqual_field;
    @Sink({FlowPermission.CONDITIONAL,FlowPermission.FILESYSTEM}) TestClass2 fs_field;

   	@Sink({FlowPermission.CONDITIONAL, FlowPermission.FILESYSTEM})  String fs;

    void foo() {
 
        TestClass2 local= new TestClass2(fs);
        fs_field = local;
        // Mismatch between explicitly given type and result of poly-resolution.
        //:: error: (constructor.invocation.invalid)
        local = new @Source(FlowPermission.CAMERA) TestClass2(fs);

        // Specific sink is a subtype of empty sinks.
        unqual_field = new @Sink({FlowPermission.FILESYSTEM, FlowPermission.CONDITIONAL, FlowPermission.INTERNET}) @Source(FlowPermission.LITERAL) TestClass2(fs);
        //:: error: (assignment.type.incompatible)
        unqual_field = new @Source(FlowPermission.CAMERA) TestClass2(fs);

        fs_field = new @Sink({FlowPermission.FILESYSTEM,FlowPermission.CONDITIONAL}) TestClass2(fs);

        fs_field = new @Source({FlowPermission.CAMERA, FlowPermission.LITERAL}) TestClass2(fs); //Allowed via Flow-policy

        //:: error: (assignment.type.incompatible)
        fs_field = new @Source(FlowPermission.INTERNET) TestClass2(fs);

    }
}

// Test specific constructor return type.
class TestClass1 {
    @Sink(FlowPermission.INTERNET) TestClass1(@Sink(FlowPermission.INTERNET) String tc1Params) {}
}

// Test polymorphic constructor return type.
class TestClass2 {
    @PolySource @PolySink TestClass2(@PolySource @PolySink String spec) {}
}

// Test that concrete instantiation can restrict return type.
class TestClass3 {
    @Sink({FlowPermission.INTERNET, FlowPermission.FILESYSTEM}) TestClass3(@Sink(FlowPermission.INTERNET) String param) {}
}

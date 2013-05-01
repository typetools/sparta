import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.FlowSource;
import sparta.checkers.quals.Sinks.FlowSink;

import java.io.*;
import java.net.*;

// Test for annotations on constructor returns and polymorphism
// with constructors.
class NewTest {
    @Sinks(FlowSink.NETWORK) TestClass1 sink;

    @Sinks(FlowSink.NETWORK) String param = "jkl";

    void method1() {
        sink = new TestClass1(param);
        sink = new @Sinks({FlowSink.NETWORK, FlowSink.CONDITIONAL}) TestClass1(param);
        //:: error: (assignment.type.incompatible)
        sink = new @Sinks(FlowSink.FILESYSTEM) TestClass1(param);
    }

    @Sinks(FlowSink.NETWORK) TestClass3 sink3;

    void method3() {
        // Subset of sinks can be specified at instantiation.
        sink3 = new @Sinks(FlowSink.NETWORK) TestClass3(param);
    }

    TestClass2 unqual_field;
    @Sinks({FlowSink.CONDITIONAL,FlowSink.FILESYSTEM}) TestClass2 fs_field;

   	@Sinks({FlowSink.CONDITIONAL, FlowSink.FILESYSTEM})  String fs;

    void foo() {
 
        TestClass2 local= new TestClass2(fs);
        fs_field = local;
        // Mismatch between explicitly given type and result of poly-resolution.
        //:: error: (constructor.invocation.invalid)
        local = new @Sources(FlowSource.CAMERA) TestClass2(fs);

        // Specific sink is a subtype of empty sinks.
        unqual_field = new @Sinks({FlowSink.FILESYSTEM, FlowSink.CONDITIONAL, FlowSink.NETWORK}) @Sources(FlowSource.LITERAL) TestClass2(fs);
        //:: error: (assignment.type.incompatible)
        unqual_field = new @Sources(FlowSource.CAMERA) TestClass2(fs);

        fs_field = new @Sinks({FlowSink.FILESYSTEM,FlowSink.CONDITIONAL}) TestClass2(fs);

        fs_field = new @Sources({FlowSource.CAMERA, FlowSource.LITERAL}) TestClass2(fs); //Allowed via Flow-policy

        //:: error: (assignment.type.incompatible)
        fs_field = new @Sources(FlowSource.NETWORK) TestClass2(fs);

    }
}

// Test specific constructor return type.
class TestClass1 {
    @Sinks(FlowSink.NETWORK) TestClass1(@Sinks(FlowSink.NETWORK) String tc1Params) {}
}

// Test polymorphic constructor return type.
class TestClass2 {
    @PolySources @PolySinks TestClass2(@PolySources @PolySinks String spec) {}
}

// Test that concrete instantiation can restrict return type.
class TestClass3 {
    @Sinks({FlowSink.NETWORK, FlowSink.FILESYSTEM}) TestClass3(@Sinks(FlowSink.NETWORK) String param) {}
}

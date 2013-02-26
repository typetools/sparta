import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;

import java.io.*;
import java.net.*;

// Test for annotations on constructor returns and polymorphism
// with constructors.
class NewTest {
    @FlowSinks(FlowSink.NETWORK) TestClass1 sink;

    @FlowSinks(FlowSink.NETWORK) String param = "jkl";

    void method1() {
        sink = new TestClass1(param);
        sink = new @FlowSinks({FlowSink.NETWORK, FlowSink.CONDITIONAL}) TestClass1(param);
        //:: error: (assignment.type.incompatible)
        sink = new @FlowSinks(FlowSink.FILESYSTEM) TestClass1(param);
    }

    @FlowSinks(FlowSink.NETWORK) TestClass3 sink3;

    void method3() {
        // Subset of sinks can be specified at instantiation.
        sink3 = new @FlowSinks(FlowSink.NETWORK) TestClass3(param);
    }

    TestClass2 unqual_field;
    @FlowSinks({FlowSink.CONDITIONAL,FlowSink.FILESYSTEM}) TestClass2 fs_field;

   	@FlowSinks({FlowSink.CONDITIONAL, FlowSink.FILESYSTEM})  String fs;

    void foo() {
 
        TestClass2 local= new TestClass2(fs);
        fs_field = local;
        // Mismatch between explicitly given type and result of poly-resolution.
        //:: error: (constructor.invocation.invalid)
        local = new @FlowSources(FlowSource.CAMERA) TestClass2(fs);

        // Specific sink is a subtype of empty sinks.
        unqual_field = new @FlowSinks({FlowSink.FILESYSTEM, FlowSink.CONDITIONAL, FlowSink.NETWORK}) @FlowSources(FlowSource.LITERAL) TestClass2(fs);
        //:: error: (assignment.type.incompatible)
        unqual_field = new @FlowSources(FlowSource.CAMERA) TestClass2(fs);

        fs_field = new @FlowSinks({FlowSink.FILESYSTEM,FlowSink.CONDITIONAL}) TestClass2(fs);

        fs_field = new @FlowSources({FlowSource.CAMERA, FlowSource.LITERAL}) TestClass2(fs); //Allowed via Flow-policy

        //:: error: (assignment.type.incompatible)
        fs_field = new @FlowSources(FlowSource.NETWORK) TestClass2(fs);

    }
}

// Test specific constructor return type.
class TestClass1 {
    @FlowSinks(FlowSink.NETWORK) TestClass1(@FlowSinks(FlowSink.NETWORK) String tc1Params) {}
}

// Test polymorphic constructor return type.
class TestClass2 {
    @PolyFlowSources @PolyFlowSinks TestClass2(@PolyFlowSources @PolyFlowSinks String spec) {}
}

// Test that concrete instantiation can restrict return type.
class TestClass3 {
    @FlowSinks({FlowSink.NETWORK, FlowSink.FILESYSTEM}) TestClass3(@FlowSinks(FlowSink.NETWORK) String param) {}
}

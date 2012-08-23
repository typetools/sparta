import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;

import java.io.*;
import java.net.*;

// Test for annotations on constructor returns and polymorphism
// with constructors.
class NewTest {
    private @FlowSinks(FlowSink.NETWORK) TestClass1 sink;

    @SuppressWarnings("flow") // Variable initialization.
    private @FlowSinks(FlowSink.NETWORK) String param = "jkl";

    void method() {
        sink = new TestClass1(param);
        sink = new @FlowSinks(FlowSink.NETWORK) TestClass1(param);
        //:: error: (assignment.type.incompatible)
        sink = new @FlowSinks(FlowSink.FILESYSTEM) TestClass1(param); 
    }

    private TestClass2 tempUrl;

    void foo() {
        @SuppressWarnings("flow")
        @FlowSinks(FlowSink.FILESYSTEM) @FlowSources({}) String url = "";

        // Specific sink is a subtype of empty sinks.
        tempUrl = new @FlowSinks(FlowSink.FILESYSTEM) TestClass2(url);
        //:: error: (assignment.type.incompatible)
        tempUrl = new @FlowSources(FlowSource.CAMERA) TestClass2(url);
    }
}

// Test specific constructor return type.
class TestClass1 {
    @FlowSinks(FlowSink.NETWORK) TestClass1(@FlowSinks(FlowSink.NETWORK) String param) {}
}

// Test polymorphic constructor return type.
class TestClass2 {
    @PolyFlowSources @PolyFlowSinks TestClass2(@PolyFlowSources @PolyFlowSinks String spec) {}
}
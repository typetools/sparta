import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.SPARTA_Permission;
import sparta.checkers.quals.Sinks.SPARTA_Permission;

import java.io.*;
import java.net.*;

// Test for annotations on constructor returns and polymorphism
// with constructors.
class NewTest {
    @Sinks(SPARTA_Permission.NETWORK) TestClass1 sink;

    @Sinks(SPARTA_Permission.NETWORK) String param = "jkl";

    void method1() {
        sink = new TestClass1(param);
        sink = new @Sinks({SPARTA_Permission.NETWORK, SPARTA_Permission.CONDITIONAL}) TestClass1(param);
        //:: error: (assignment.type.incompatible)
        sink = new @Sinks(SPARTA_Permission.FILESYSTEM) TestClass1(param);
    }

    @Sinks(SPARTA_Permission.NETWORK) TestClass3 sink3;

    void method3() {
        // Subset of sinks can be specified at instantiation.
        sink3 = new @Sinks(SPARTA_Permission.NETWORK) TestClass3(param);
    }

    TestClass2 unqual_field;
    @Sinks({SPARTA_Permission.CONDITIONAL,SPARTA_Permission.FILESYSTEM}) TestClass2 fs_field;

   	@Sinks({SPARTA_Permission.CONDITIONAL, SPARTA_Permission.FILESYSTEM})  String fs;

    void foo() {
 
        TestClass2 local= new TestClass2(fs);
        fs_field = local;
        // Mismatch between explicitly given type and result of poly-resolution.
        //:: error: (constructor.invocation.invalid)
        local = new @Sources(SPARTA_Permission.CAMERA) TestClass2(fs);

        // Specific sink is a subtype of empty sinks.
        unqual_field = new @Sinks({SPARTA_Permission.FILESYSTEM, SPARTA_Permission.CONDITIONAL, SPARTA_Permission.NETWORK}) @Sources(SPARTA_Permission.LITERAL) TestClass2(fs);
        //:: error: (assignment.type.incompatible)
        unqual_field = new @Sources(SPARTA_Permission.CAMERA) TestClass2(fs);

        fs_field = new @Sinks({SPARTA_Permission.FILESYSTEM,SPARTA_Permission.CONDITIONAL}) TestClass2(fs);

        fs_field = new @Sources({SPARTA_Permission.CAMERA, SPARTA_Permission.LITERAL}) TestClass2(fs); //Allowed via Flow-policy

        //:: error: (assignment.type.incompatible)
        fs_field = new @Sources(SPARTA_Permission.NETWORK) TestClass2(fs);

    }
}

// Test specific constructor return type.
class TestClass1 {
    @Sinks(SPARTA_Permission.NETWORK) TestClass1(@Sinks(SPARTA_Permission.NETWORK) String tc1Params) {}
}

// Test polymorphic constructor return type.
class TestClass2 {
    @PolySources @PolySinks TestClass2(@PolySources @PolySinks String spec) {}
}

// Test that concrete instantiation can restrict return type.
class TestClass3 {
    @Sinks({SPARTA_Permission.NETWORK, SPARTA_Permission.FILESYSTEM}) TestClass3(@Sinks(SPARTA_Permission.NETWORK) String param) {}
}

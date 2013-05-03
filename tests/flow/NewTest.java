import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;
import sparta.checkers.quals.SpartaPermission;

import java.io.*;
import java.net.*;

// Test for annotations on constructor returns and polymorphism
// with constructors.
class NewTest {
    @Sinks(SpartaPermission.INTERNET) TestClass1 sink;

    @Sinks(SpartaPermission.INTERNET) String param = "jkl";

    void method1() {
        sink = new TestClass1(param);
        sink = new @Sinks({SpartaPermission.INTERNET, SpartaPermission.CONDITIONAL}) TestClass1(param);
        //:: error: (assignment.type.incompatible)
        sink = new @Sinks(SpartaPermission.FILESYSTEM) TestClass1(param);
    }

    @Sinks(SpartaPermission.INTERNET) TestClass3 sink3;

    void method3() {
        // Subset of sinks can be specified at instantiation.
        sink3 = new @Sinks(SpartaPermission.INTERNET) TestClass3(param);
    }

    TestClass2 unqual_field;
    @Sinks({SpartaPermission.CONDITIONAL,SpartaPermission.FILESYSTEM}) TestClass2 fs_field;

   	@Sinks({SpartaPermission.CONDITIONAL, SpartaPermission.FILESYSTEM})  String fs;

    void foo() {
 
        TestClass2 local= new TestClass2(fs);
        fs_field = local;
        // Mismatch between explicitly given type and result of poly-resolution.
        //:: error: (constructor.invocation.invalid)
        local = new @Sources(SpartaPermission.CAMERA) TestClass2(fs);

        // Specific sink is a subtype of empty sinks.
        unqual_field = new @Sinks({SpartaPermission.FILESYSTEM, SpartaPermission.CONDITIONAL, SpartaPermission.INTERNET}) @Sources(SpartaPermission.LITERAL) TestClass2(fs);
        //:: error: (assignment.type.incompatible)
        unqual_field = new @Sources(SpartaPermission.CAMERA) TestClass2(fs);

        fs_field = new @Sinks({SpartaPermission.FILESYSTEM,SpartaPermission.CONDITIONAL}) TestClass2(fs);

        fs_field = new @Sources({SpartaPermission.CAMERA, SpartaPermission.LITERAL}) TestClass2(fs); //Allowed via Flow-policy

        //:: error: (assignment.type.incompatible)
        fs_field = new @Sources(SpartaPermission.INTERNET) TestClass2(fs);

    }
}

// Test specific constructor return type.
class TestClass1 {
    @Sinks(SpartaPermission.INTERNET) TestClass1(@Sinks(SpartaPermission.INTERNET) String tc1Params) {}
}

// Test polymorphic constructor return type.
class TestClass2 {
    @PolySources @PolySinks TestClass2(@PolySources @PolySinks String spec) {}
}

// Test that concrete instantiation can restrict return type.
class TestClass3 {
    @Sinks({SpartaPermission.INTERNET, SpartaPermission.FILESYSTEM}) TestClass3(@Sinks(SpartaPermission.INTERNET) String param) {}
}

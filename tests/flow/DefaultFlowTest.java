import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import static sparta.checkers.quals.FlowPermission.*;
/**
 * This class is intended to test the default 
 * for return, parameter, receiver, and field types.
 *
 */
class DefaultFlowTest {
    @Source({ INTERNET }) @Sink({}) int source = 1;
    @Source({}) @Sink(INTERNET) int sink = 2;
    @Source({ INTERNET }) @Sink(INTERNET) int sourceSink = 3;
    int none = 4;

    @Source(INTERNET) @Sink({}) TestClass classSource;
    @Source({})  @Sink(INTERNET) TestClass classSink;
    @Source(INTERNET) @Sink(INTERNET) TestClass classSourceSink;
    TestClass classNone;

    int testMethod(int input) {
        return 0;
    }

    int testMethod() {
        return 0;
    }

    //test on return, receiver, and whole class
    void testParameter() {
        //::error: (argument.type.incompatible)
        testMethod(source);
        testMethod(sink);
        //::error: (argument.type.incompatible)
        testMethod(sourceSink);
        testMethod(none);
    }

    void testReturn() {
        source = testMethod();
        sink = testMethod();
        sourceSink = testMethod();
        none = testMethod();
    }

    void testReceiver() {
        none = classSource.testClassMethod(none);
        none = classSink.testClassMethod(none);
        none = classSourceSink.testClassMethod(none);
        none = classNone.testClassMethod(none);
    }

}

class TestClass {
    int testClassMethod(int input) {
        return 0;
    }
}
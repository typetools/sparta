import sparta.checkers.quals.Source;
import sparta.checkers.quals.FlowPermission;

import sparta.checkers.quals.Sink;
import static sparta.checkers.quals.FlowPermissionString.*;

/**
 * This is class is intended to test the Flow Policy Completion defaulting.
 * 
 * @author smillst
 * 
 */

/**
 * This class tests all the different ways that flow completion might happend
 */
class FlowCompleteWays{
    /*
     * READ_SMS -> FILESYSTEM
     * READ_TIME -> FILESYSTEM, INTERNET
     * READ_CONTACTS -> WRITE_TIME
     */
    @Source(READ_SMS) Object oneSource(){return null;}
    @Source(READ_TIME) Object oneSourceMapsToTwoSinks(){return null;}
    @Source({READ_SMS, READ_TIME}) Object twoSource(){return null;}
    @Source({READ_TIME, READ_CONTACTS}) Object twoSourceNoIntersect(){return null;}
    
    @Sink(FILESYSTEM) Object oneSink(){return null;}
    @Sink({FILESYSTEM, INTERNET}) Object twoSink(){return null;}
    @Sink({FILESYSTEM, WRITE_TIME}) Object twoSinkNoIntersect(){return null;}
    @Sink(WRITE_TIME) Object oneSinkOneSource(){return null;}
    
    @Sink({}) Object emptySinkAnySource(){return null;}
    @Source({}) Object emptySourceAnySink(){return null;}
    
    void test(){
        @Source(READ_SMS) @Sink(FILESYSTEM) Object a = oneSource();
        @Source(READ_TIME) @Sink({FILESYSTEM, INTERNET}) Object b = oneSourceMapsToTwoSinks();
        @Source({READ_SMS, READ_TIME}) @Sink(FILESYSTEM) Object c = twoSource();
        @Source({READ_TIME, READ_CONTACTS}) @Sink({}) Object d = twoSourceNoIntersect();
        
        @Source({READ_SMS, READ_TIME}) @Sink(FILESYSTEM) Object e = oneSink();
        @Source(READ_TIME) @Sink({FILESYSTEM, INTERNET}) Object f = twoSink();
        @Source({}) @Sink({FILESYSTEM,WRITE_TIME}) Object g = twoSinkNoIntersect();    
        @Source(READ_CONTACTS) @Sink(WRITE_TIME) Object h = oneSinkOneSource();
        
        @Source(ANY) @Sink({}) Object i = emptySinkAnySource();
        @Source({}) @Sink (ANY) Object j = emptySourceAnySink();
    }
}


/**
 * This class tests all major code locations that might use flow complete
 */
class FlowCompletionSource {
    public @Source({ READ_SMS, READ_TIME }) FlowCompletionSource testField = null;

    //Fields to help test types.    
    public @Source({ READ_SMS, READ_TIME }) @Sink(FILESYSTEM) FlowCompletionSource c = null;
    public @Source(ANY) @Sink({}) FlowCompletionSource top = null;
    public @Source({}) @Sink(ANY) FlowCompletionSource bottom = null;

    public @Source({ READ_SMS, READ_TIME })
    FlowCompletionSource() {
    }

    void testConstructor() {
        @Source({ READ_SMS, READ_TIME }) @Sink(FILESYSTEM) FlowCompletionSource c = new FlowCompletionSource();
        //:: error: (argument.type.incompatible)
        bottom(new FlowCompletionSource());
        //:: error: (argument.type.incompatible)
        internetSink(new FlowCompletionSource());
    }

    void testThis(@Source({READ_SMS, READ_TIME}) FlowCompletionSource this){
        writeToFile(this);
      //:: error: (argument.type.incompatible)
        bottom(this);
        //:: error: (argument.type.incompatible)
        internetSink(this);
    }
    void testReceiverCall() {
        c.testThis();
        //:: error: (method.invocation.invalid)
        top.testThis();
        bottom.testThis();
    }

    void testParam(@Source({ READ_SMS, READ_TIME }) Object o) {
        writeToFile(o);
        //:: error: (argument.type.incompatible)
        bottom(o);
        //:: error: (argument.type.incompatible)
        internetSink(o);
    }

    void testParmCall() {
        testParam(c);
        //:: error: (argument.type.incompatible)
        testParam(top);
        testParam(bottom);
    }

    @Source({ READ_SMS, READ_TIME })
    FlowCompletionSource testReturn() {
        for (int i = 0; i < 3; i++) {
            if (i == 0) {
                return c;
            } else if (i == 1) {
                //:: error: (return.type.incompatible)
                return top;
            }
        }
        return bottom;
    }

    void testReturnCall(){
        c = testReturn();
        top = testReturn();
      //:: error: (assignment.type.incompatible)
        bottom = testReturn();
        //:: error: (argument.type.incompatible)
        internetSink(testReturn());
    }

    void testCast(){
        writeToFile((/*@Source({READ_SMS, READ_TIME})*/ FlowCompletionSource) bottom);
      //:: error: (argument.type.incompatible)
        bottom((/*@Source({READ_SMS, READ_TIME})*/ FlowCompletionSource) bottom);
        //:: error: (argument.type.incompatible)
        internetSink((/*@Source({READ_SMS, READ_TIME})*/ FlowCompletionSource) bottom);

        writeToFile((/*@Source({READ_SMS, READ_TIME})*/ String) "hello");
      //:: error: (argument.type.incompatible)
        bottom((/*@Source({READ_SMS, READ_TIME})*/ String) "hello");
        //:: error: (argument.type.incompatible)
        internetSink((/*@Source({READ_SMS, READ_TIME})*/ String) "hello");
    }

    void testField() {
        c = testField;
        top = testField;
        //:: error: (assignment.type.incompatible)
        bottom = testField;
    }

    void testFieldAssignment() {
        //:: error: (argument.type.incompatible)
        internetSink(testField);
        testField = c;
        //:: error: (assignment.type.incompatible)
        testField = top;
        testField = bottom;
    }

    //Mehtods to help test types.
    void writeToFile(@Source({ READ_SMS, READ_TIME }) @Sink(FILESYSTEM) Object o) {
    }

    void bottom(@Source({}) @Sink(ANY) Object o) {
    }

    void internetSink(@Sink(INTERNET) Object o) {
    }
}

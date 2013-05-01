import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.Sources;
import sparta.checkers.quals.PolySinks;
import sparta.checkers.quals.PolySources;
import sparta.checkers.quals.Sinks.FlowSink;
import sparta.checkers.quals.Sources.FlowSource;

/**
 * Test the defaults for numeric and string literals, and
 * also for unqualified object instantiation.
 *
 * Do we want to change the default for literals to be the
 * bottom type?
 */
class TestLiteralDefaults {
    //:: error: (forbidden.flow)
    @Sinks({}) @Sources({FlowSource.ANY}) float ntop;
    @PolySources @PolySinks float npoly;

    //:: error: (forbidden.flow)
   @Sinks({FlowSink.ANY}) @Sources({}) float nbot;
    float nunqual;

    void testNumeric() {
        ntop = 2f;
        //:: error: (assignment.type.incompatible)
        npoly = 2f;
        //:: error: (assignment.type.incompatible)
        nbot = 2f;
        nunqual = 2f;
    }

    //:: error: (forbidden.flow)
    @Sinks({}) @Sources({FlowSource.ANY}) Object rtop;
    @PolySources @PolySinks Object rpoly;

    //:: error: (forbidden.flow)
    @Sinks({FlowSink.ANY}) @Sources({}) Object rbot;
    Object runqual;

    void testReference() {
        rtop = "a";
        //:: error: (assignment.type.incompatible)
        rpoly = "b";
        //:: error: (assignment.type.incompatible)
        rbot = "c";
        runqual = "d";

        rtop = new Object();
        //:: error: (assignment.type.incompatible)
        rpoly = new Object();
        //:: error: (assignment.type.incompatible)
        rbot = new Object();
        runqual = new Object();
    }

    //:: error: (forbidden.flow)
    @Sinks({}) @Sources({FlowSource.ANY}) char ctop;
    @PolySources @PolySinks char cpoly;
    //:: error: (forbidden.flow)
    @Sinks({FlowSink.ANY}) @Sources({}) char cbot;
    char cunqual;

    void testChar() {
        rtop = 'a';
        //:: error: (assignment.type.incompatible)
        rpoly = 'b';
        //:: error: (assignment.type.incompatible)
        rbot = 'c';
        runqual = 'd';
    }
}
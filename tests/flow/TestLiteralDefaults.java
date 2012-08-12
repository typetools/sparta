import sparta.checkers.quals.FlowSinks;
import sparta.checkers.quals.FlowSources;
import sparta.checkers.quals.PolyFlowSinks;
import sparta.checkers.quals.PolyFlowSources;
import sparta.checkers.quals.FlowSinks.FlowSink;
import sparta.checkers.quals.FlowSources.FlowSource;

/**
 * Test the defaults for numeric and string literals, and
 * also for unqualified object instantiation.
 *
 * Do we want to change the default for literals to be the
 * bottom type?
 */
class TestLiteralDefaults {
    @FlowSinks({}) @FlowSources({FlowSource.ANY}) float ntop;
    @PolyFlowSources @PolyFlowSinks float npoly;
    @FlowSinks({FlowSink.ANY}) @FlowSources({}) float nbot;
    float nunqual;

    void testNumeric() {
        ntop = 2f;
        //:: error: (assignment.type.incompatible)
        npoly = 2f;
        //:: error: (assignment.type.incompatible)
        nbot = 2f;
        nunqual = 2f;
    }

    @FlowSinks({}) @FlowSources({FlowSource.ANY}) Object rtop;
    @PolyFlowSources @PolyFlowSinks Object rpoly;
    @FlowSinks({FlowSink.ANY}) @FlowSources({}) Object rbot;
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

    @FlowSinks({}) @FlowSources({FlowSource.ANY}) char ctop;
    @PolyFlowSources @PolyFlowSinks char cpoly;
    @FlowSinks({FlowSink.ANY}) @FlowSources({}) char cbot;
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
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;
import sparta.checkers.quals.ParameterizedFlowPermission;
import sparta.checkers.quals.FlowPermission;

/**
 * Test the defaults for numeric and string literals, and
 * also for unqualified object instantiation.
 *
 * Do we want to change the default for literals to be the
 * bottom type?
 */
class TestLiteralDefaults {
    
    //:: error: (forbidden.flow)
    @Sink({}) @sparta.checkers.quals.Source({FlowPermission.ANY}) float ntop;
    @PolySource @PolySink float npoly;

    //:: error: (forbidden.flow)
    @Sink({FlowPermission.ANY}) @sparta.checkers.quals.Source({}) float nbot;
  
    float nunqual;

    void testNumeric() {
        //:: error: (forbidden.flow)
        ntop = 2f;
        //:: error: (assignment.type.incompatible)
        npoly = 2f;
        //:: error: (assignment.type.incompatible)
        nbot = 2f;
        nunqual = 2f;
    }

    //:: error: (forbidden.flow)
    @Sink({}) @sparta.checkers.quals.Source({FlowPermission.ANY}) Object rtop;
    @PolySource @PolySink Object rpoly;

    //:: error: (forbidden.flow)
    @Sink({FlowPermission.ANY}) @sparta.checkers.quals.Source({}) Object rbot;

    Object runqual;

    void testReference() {
        //:: error: (forbidden.flow)
        rtop = "a";
        //:: error: (assignment.type.incompatible)
        rpoly = "b";
        //:: error: (assignment.type.incompatible)
        rbot = "c";
        runqual = "d";
        //:: error: (forbidden.flow)
        rtop = new Object();
 
        //:: error: (assignment.type.incompatible)
        rpoly = new Object();

        //:: error: (assignment.type.incompatible)
        rbot = new Object();
        runqual = new Object();
    }

    //:: error: (forbidden.flow)
    @Sink({}) @sparta.checkers.quals.Source({FlowPermission.ANY}) char ctop;
    @PolySource @PolySink char cpoly;
 
    //:: error: (forbidden.flow)
    @Sink({FlowPermission.ANY}) @sparta.checkers.quals.Source({}) char cbot;

    char cunqual;

    void testChar() {
        //:: error: (forbidden.flow)
        rtop = 'a';
        //:: error: (assignment.type.incompatible)
        rpoly = 'b';
        //:: error: (assignment.type.incompatible)
        rbot = 'c';
        runqual = 'd';
    }
}
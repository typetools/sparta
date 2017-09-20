import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;
import sparta.checkers.quals.FlowPermissionString;
import sparta.checkers.quals.FlowPermission;

/**
 * Test the defaults for numeric and string literals, and
 * also for unqualified object instantiation.
 *
 * Do we want to change the default for literals to be the
 * bottom type?
 */
class TestLiteralDefaults {

    @Sink({}) @sparta.checkers.quals.Source({FlowPermissionString.ANY}) float ntop;
    @PolySource @PolySink float npoly;

    @Sink({FlowPermissionString.ANY}) @sparta.checkers.quals.Source({}) float nbot;

    float nunqual;

    void testNumeric() {
        ntop = 2f;
        npoly = 2f;
        nbot = 2f;
        nunqual = 2f;
    }

    @Sink({}) @sparta.checkers.quals.Source({FlowPermissionString.ANY}) Object rtop;
    @PolySource @PolySink Object rpoly;

    @Sink({FlowPermissionString.ANY}) @sparta.checkers.quals.Source({}) Object rbot;

    Object runqual;

    void testReference() {
        rtop = "a";
        rpoly = "b";
        rbot = "c";
        runqual = "d";
        rtop = new Object();

        rpoly = new Object();

        rbot = new Object();
        runqual = new Object();
    }

    @Sink({}) @sparta.checkers.quals.Source({FlowPermissionString.ANY}) char ctop;
    @PolySource @PolySink char cpoly;

    @Sink({FlowPermissionString.ANY}) @sparta.checkers.quals.Source({}) char cbot;

    char cunqual;

    void testChar() {
        rtop = 'a';
        rpoly = 'b';
        rbot = 'c';
        runqual = 'd';
    }
}
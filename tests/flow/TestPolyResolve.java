import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;

class TestPolyResolve {
    float x1 = 2f;
    float factor;

    /** Ensure that the LUB of PolyFlowSource PolyFlowSink with unannotated
     * gives the right thing.
     */
    void mod2pi(@PolyFlowSources @PolyFlowSinks float x) {
        factor = x / x1;
    }
}
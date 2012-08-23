import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.PolyFlowSinks;
import sparta.checkers.quals.PolyFlowSources;

class CastTest {
    @FlowSources(FlowSource.LOCATION) double result;
    @FlowSources(FlowSource.LOCATION) int x;

    void m() {
        result = bar(x);
        //:: error: (argument.type.incompatible)
        result = foo(x);
    }

    public @FlowSources(FlowSource.LOCATION) double bar(@FlowSources(FlowSource.LOCATION) int x) {
        return (double) x;
    }

    public @FlowSources(FlowSource.LOCATION) double foo(int x) {
        return (@FlowSources(FlowSource.LOCATION) double) x;
    }

}

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

    //int x will now have type LITERAL -> CONDITIONAL which is different from ANY -> CONDITIONAL
    //which cannot be coerced down to LOCATION -> CONDITIONAL
    public @FlowSources(FlowSource.LOCATION) double foo(int x) {
        //:: warning: (cast.unsafe)
        return (@FlowSources(FlowSource.LOCATION) double) x;
    }

    public @FlowSources(FlowSource.LOCATION) double foo2( @FlowSources(FlowSource.LOCATION) int x) {
        return (@FlowSources(FlowSource.LOCATION) double) x;
    }

    public @FlowSources(FlowSource.LOCATION) double fromAny(@FlowSources(FlowSource.ANY) int x) {
        //:: warning: (cast.unsafe)
        return (@FlowSources(FlowSource.LOCATION) double) x;
    }

    public @FlowSources({FlowSource.LOCATION}) double anyToLoc(@FlowSources(FlowSource.ANY) int x) {
        //:: warning: (cast.unsafe)
        return (@FlowSources({FlowSource.LOCATION}) double) x;
    }

}

import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.FlowSource;
import sparta.checkers.quals.PolySinks;
import sparta.checkers.quals.PolySources;

class CastTest {
    @Sources(FlowSource.LOCATION) double result;
    @Sources(FlowSource.LOCATION) int x;

    void m() {
        result = bar(x);
        //:: error: (argument.type.incompatible)
        result = foo(x);
    }

    public @Sources(FlowSource.LOCATION) double bar(@Sources(FlowSource.LOCATION) int x) {
        return (double) x;
    }

    //int x will now have type LITERAL -> CONDITIONAL which is different from ANY -> CONDITIONAL
    //which cannot be coerced down to LOCATION -> CONDITIONAL
    public @Sources(FlowSource.LOCATION) double foo(int x) {
        //:: warning: (cast.unsafe)
        return (@Sources(FlowSource.LOCATION) double) x;
    }

    public @Sources(FlowSource.LOCATION) double foo2( @Sources(FlowSource.LOCATION) int x) {
        return (@Sources(FlowSource.LOCATION) double) x;
    }

    public @Sources(FlowSource.LOCATION) double fromAny(@Sources(FlowSource.ANY) int x) {
        //:: warning: (cast.unsafe)
        return (@Sources(FlowSource.LOCATION) double) x;
    }

    public @Sources({FlowSource.LOCATION}) double anyToLoc(@Sources(FlowSource.ANY) int x) {
        //:: warning: (cast.unsafe)
        return (@Sources({FlowSource.LOCATION}) double) x;
    }

}

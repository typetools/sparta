import sparta.checkers.quals.*;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;

import static sparta.checkers.quals.FlowPermission.*;

class ParameterizedCastTest {
    @Source(ACCESS_FINE_LOCATION) double result;
    @Source(value={}, finesources={@FineSource(value=ACCESS_FINE_LOCATION, params={"myLocation"})}) int x;

    void m() {
        result = bar(x);
        result = foo(x);
    }

    public @Source(ACCESS_FINE_LOCATION) double bar(@Source(ACCESS_FINE_LOCATION) int x) {
        return (double) x;
    }


    public @Source(ACCESS_FINE_LOCATION) double foo(@Source(value={}, finesources={@FineSource(value=ACCESS_FINE_LOCATION, params={"my*"})}) int x) {
        //:: warning: (cast.unsafe)
        return (@Source(value={}, finesources={@FineSource(value=ACCESS_FINE_LOCATION, params={"badParam"})}) double) x;
    }

    public @Source(ACCESS_FINE_LOCATION) double foo2( @Source(ACCESS_FINE_LOCATION) int x) {
        return (@Source(ACCESS_FINE_LOCATION) double) x;
    }

    public @Source(ACCESS_FINE_LOCATION) double fromAny(@Source(ANY) int x) {
        //:: warning: (cast.unsafe)
        return (@Source(ACCESS_FINE_LOCATION) double) x;
    }

    public @Source(ACCESS_FINE_LOCATION) double anyToLoc(@Source(ANY) int x) {
        //:: warning: (cast.unsafe)
        return (@Source(ACCESS_FINE_LOCATION) double) x;
    }

}
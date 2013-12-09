import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;

class CastTest {
    @Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) double result;
    @Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) int x;

    void m() {
        result = bar(x);
        result = foo(x);
    }

    public @Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) double bar(@Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) int x) {
        return (double) x;
    }


    public @Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) double foo(int x) {
        //:: warning: (cast.unsafe)
        return (@Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) double) x;
    }

    public @Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) double foo2( @Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) int x) {
        return (@Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) double) x;
    }

    public @Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) double fromAny(@Source(CoarseFlowPermission.ANY) int x) {
        //:: warning: (cast.unsafe)
        return (@Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) double) x;
    }

    public @Source({CoarseFlowPermission.ACCESS_FINE_LOCATION}) double anyToLoc(@Source(CoarseFlowPermission.ANY) int x) {
        //:: warning: (cast.unsafe)
        return (@Source({CoarseFlowPermission.ACCESS_FINE_LOCATION}) double) x;
    }

}

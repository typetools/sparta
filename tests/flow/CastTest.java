import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;

class CastTest {
    @Source(FlowPermission.ACCESS_FINE_LOCATION) double result;
    @Source(FlowPermission.ACCESS_FINE_LOCATION) int x;

    void m() {
        result = bar(x);
        //:: error: (argument.type.incompatible)
        result = foo(x);
    }

    public @Source(FlowPermission.ACCESS_FINE_LOCATION) double bar(@Source(FlowPermission.ACCESS_FINE_LOCATION) int x) {
        return (double) x;
    }

    //int x will now have type LITERAL -> CONDITIONAL which is different from ANY -> CONDITIONAL
    //which cannot be coerced down to ACCESS_FINE_LOCATION -> CONDITIONAL
    public @Source(FlowPermission.ACCESS_FINE_LOCATION) double foo(int x) {
        //:: warning: (cast.unsafe)
        return (@Source(FlowPermission.ACCESS_FINE_LOCATION) double) x;
    }

    public @Source(FlowPermission.ACCESS_FINE_LOCATION) double foo2( @Source(FlowPermission.ACCESS_FINE_LOCATION) int x) {
        return (@Source(FlowPermission.ACCESS_FINE_LOCATION) double) x;
    }

    public @Source(FlowPermission.ACCESS_FINE_LOCATION) double fromAny(@Source(FlowPermission.ANY) int x) {
        //:: warning: (cast.unsafe)
        return (@Source(FlowPermission.ACCESS_FINE_LOCATION) double) x;
    }

    public @Source({FlowPermission.ACCESS_FINE_LOCATION}) double anyToLoc(@Source(FlowPermission.ANY) int x) {
        //:: warning: (cast.unsafe)
        return (@Source({FlowPermission.ACCESS_FINE_LOCATION}) double) x;
    }

}

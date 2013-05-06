import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;
import sparta.checkers.quals.PolySinks;
import sparta.checkers.quals.PolySources;

class CastTest {
    @Sources(SpartaPermission.ACCESS_FINE_LOCATION) double result;
    @Sources(SpartaPermission.ACCESS_FINE_LOCATION) int x;

    void m() {
        result = bar(x);
        //:: error: (argument.type.incompatible)
        result = foo(x);
    }

    public @Sources(SpartaPermission.ACCESS_FINE_LOCATION) double bar(@Sources(SpartaPermission.ACCESS_FINE_LOCATION) int x) {
        return (double) x;
    }

    //int x will now have type LITERAL -> CONDITIONAL which is different from ANY -> CONDITIONAL
    //which cannot be coerced down to ACCESS_FINE_LOCATION -> CONDITIONAL
    public @Sources(SpartaPermission.ACCESS_FINE_LOCATION) double foo(int x) {
        //:: warning: (cast.unsafe)
        return (@Sources(SpartaPermission.ACCESS_FINE_LOCATION) double) x;
    }

    public @Sources(SpartaPermission.ACCESS_FINE_LOCATION) double foo2( @Sources(SpartaPermission.ACCESS_FINE_LOCATION) int x) {
        return (@Sources(SpartaPermission.ACCESS_FINE_LOCATION) double) x;
    }

    public @Sources(SpartaPermission.ACCESS_FINE_LOCATION) double fromAny(@Sources(SpartaPermission.ANY) int x) {
        //:: warning: (cast.unsafe)
        return (@Sources(SpartaPermission.ACCESS_FINE_LOCATION) double) x;
    }

    public @Sources({SpartaPermission.ACCESS_FINE_LOCATION}) double anyToLoc(@Sources(SpartaPermission.ANY) int x) {
        //:: warning: (cast.unsafe)
        return (@Sources({SpartaPermission.ACCESS_FINE_LOCATION}) double) x;
    }

}

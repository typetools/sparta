import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.SPARTA_Permission;
import sparta.checkers.quals.PolySinks;
import sparta.checkers.quals.PolySources;

class CastTest {
    @Sources(SPARTA_Permission.LOCATION) double result;
    @Sources(SPARTA_Permission.LOCATION) int x;

    void m() {
        result = bar(x);
        //:: error: (argument.type.incompatible)
        result = foo(x);
    }

    public @Sources(SPARTA_Permission.LOCATION) double bar(@Sources(SPARTA_Permission.LOCATION) int x) {
        return (double) x;
    }

    //int x will now have type LITERAL -> CONDITIONAL which is different from ANY -> CONDITIONAL
    //which cannot be coerced down to LOCATION -> CONDITIONAL
    public @Sources(SPARTA_Permission.LOCATION) double foo(int x) {
        //:: warning: (cast.unsafe)
        return (@Sources(SPARTA_Permission.LOCATION) double) x;
    }

    public @Sources(SPARTA_Permission.LOCATION) double foo2( @Sources(SPARTA_Permission.LOCATION) int x) {
        return (@Sources(SPARTA_Permission.LOCATION) double) x;
    }

    public @Sources(SPARTA_Permission.LOCATION) double fromAny(@Sources(SPARTA_Permission.ANY) int x) {
        //:: warning: (cast.unsafe)
        return (@Sources(SPARTA_Permission.LOCATION) double) x;
    }

    public @Sources({SPARTA_Permission.LOCATION}) double anyToLoc(@Sources(SPARTA_Permission.ANY) int x) {
        //:: warning: (cast.unsafe)
        return (@Sources({SPARTA_Permission.LOCATION}) double) x;
    }

}

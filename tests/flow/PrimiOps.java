import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.Sources;
import sparta.checkers.quals.PolySinks;
import sparta.checkers.quals.PolySources;
import sparta.checkers.quals.Sinks.SPARTA_Permission;
import sparta.checkers.quals.Sources.SPARTA_Permission;


class PrimiOps {
    @SuppressWarnings("flow")
    @Sinks({}) @Sources({SPARTA_Permission.ANY}) float top;
    @PolySources @PolySinks float poly;
    @SuppressWarnings("flow")
    @Sinks({SPARTA_Permission.ANY}) @Sources({}) float bot;
    float unqual;

    void mod2pi() {
        top = top / top;
        poly = poly * poly;
        bot = bot / bot;
        unqual = unqual * unqual;

        top = unqual / top;
        top = top / unqual;
        unqual = unqual / bot;

        //:: error: (assignment.type.incompatible)
        poly = poly / bot;

        // This does not work, because unqualified is
        // a top/bottom mix.
        //:: error: (assignment.type.incompatible)
        poly = poly / unqual;
        //:: error: (assignment.type.incompatible)
        unqual = poly / unqual;
    }

    // TODO: ops on Strings.
}
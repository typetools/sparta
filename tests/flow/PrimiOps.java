import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.*;


class PrimiOps {
    @SuppressWarnings("flow")
    @Sink({}) @Source({CoarseFlowPermission.ANY}) float top;
    @PolySource @PolySink float poly;
    @SuppressWarnings("flow")
    @Sink({CoarseFlowPermission.ANY}) @Source({}) float bot;
    float unqual;

    void mod2pi() {
        top = top / top;
        poly = poly * poly;
        bot = bot / bot;
        unqual = unqual * unqual;

        top = unqual / top;
        top = top / unqual;
        unqual = unqual / bot;

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
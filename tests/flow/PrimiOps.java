import sparta.checkers.quals.FlowSinks;
import sparta.checkers.quals.FlowSources;
import sparta.checkers.quals.PolyFlowSinks;
import sparta.checkers.quals.PolyFlowSources;
import sparta.checkers.quals.FlowSinks.FlowSink;
import sparta.checkers.quals.FlowSources.FlowSource;


class PrimiOps {
    @SuppressWarnings("flow")
    @FlowSinks({}) @FlowSources({FlowSource.ANY}) float top;
    @PolyFlowSources @PolyFlowSinks float poly;
    @SuppressWarnings("flow")
    @FlowSinks({FlowSink.ANY}) @FlowSources({}) float bot;
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
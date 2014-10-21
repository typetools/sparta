import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.*;

class PrimiOps {
    @Sink({}) @Source({ FlowPermissionString.ANY }) float top;
    @PolySource @PolySink float poly;
    @Sink({ FlowPermissionString.ANY }) @Source({}) float bot;

    void mod2pi() {
        top = top / top;
        poly = poly * poly;
        bot = bot / bot;
        poly = poly / bot;
    }

    // TODO: ops on Strings.
}
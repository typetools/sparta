import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;

class Conditions {
    void good(int p) {
        if (p < 11) {}
    }

    void bad(@FlowSources(FlowSource.ANY) int p) {
        //:: error: (condition.flow)
        if (p > 9) {
            // boom.
        }

        //:: error: (condition.flow)
        while ((p % 5) > 2) {}

        // Flow propagates source from p to b.
        boolean b = p < 9;
        //:: error: (condition.flow)
        int answer = b ? 42 : 33;
    }
}
import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.FlowSource;
import sparta.checkers.quals.Sinks.FlowSink;

class Conditions {
    void good(int p) {
        if (p < 11) {}

        @Sinks({FlowSink.CONDITIONAL}) boolean someBool = true;
        if(someBool) {
            // good
        }
    }

    void bad(@Sinks(FlowSink.ACCOUNTS) int p) {
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

    void bad(@Sinks(FlowSink.ACCOUNTS) boolean p) {
        //:: error: (condition.flow)
        if(p) {
            // bad.
        }
    }
}
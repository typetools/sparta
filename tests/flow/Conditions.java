import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.SPARTA_Permission;
import sparta.checkers.quals.Sinks.SPARTA_Permission;

class Conditions {
    void good(int p) {
        if (p < 11) {}

        @Sinks({SPARTA_Permission.CONDITIONAL}) boolean someBool = true;
        if(someBool) {
            // good
        }
    }

    void bad(@Sinks(SPARTA_Permission.ACCOUNTS) int p) {
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

    void bad(@Sinks(SPARTA_Permission.ACCOUNTS) boolean p) {
        //:: error: (condition.flow)
        if(p) {
            // bad.
        }
    }
}
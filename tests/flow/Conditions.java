import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;

class Conditions {
    void good(int p) {
        if (p < 11) {}

        @Sinks({SpartaPermission.CONDITIONAL}) boolean someBool = true;
        if(someBool) {
            // good
        }
    }

    void bad(@Sinks(SpartaPermission.MANAGE_ACCOUNTS) int p) {
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

    void bad(@Sinks(SpartaPermission.MANAGE_ACCOUNTS) boolean p) {
        //:: error: (condition.flow)
        if(p) {
            // bad.
        }
    }
}
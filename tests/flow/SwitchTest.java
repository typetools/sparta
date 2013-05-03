import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;
import sparta.checkers.quals.SpartaPermission;
import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.PolySinks;
import sparta.checkers.quals.PolySources;

class SwitchTest {
    void foo() {
        @Sources({SpartaPermission.LOCATION, SpartaPermission.LITERAL}) int info = 1;

        //Explicitly forbid this field from having SpartaPermission.CONDITIONAL
        //:: error: (forbidden.flow)
        @Sources({SpartaPermission.LOCATION}) @Sinks({}) int badInfo = 1;

        @Sources(SpartaPermission.LITERAL) int noInfo = 1;

        //This field gets SpartaPermission.CONDITIONAL added by default
        final @Sources({SpartaPermission.LOCATION, SpartaPermission.LITERAL}) int caseInfo = 1;
        final int caseNoInfo = 2;


        //Explicitly forbid this field from having SpartaPermission.CONDITIONAL
        //:: error: (forbidden.flow)
        final @Sources({SpartaPermission.LOCATION, SpartaPermission.LITERAL}) @Sinks({}) int badCaseInfo = 3;

        switch (info) {

            case caseInfo: {
                info++;
            }
            case caseNoInfo: {
                info++;
            }
        }

        //:: error: (condition.flow)
        switch (badInfo) {
            //:: error: (condition.flow)
            case badCaseInfo: {
                info++;
            }
            case caseNoInfo: {
                info++;
            }
        }

        switch (noInfo) {
            //:: error: (condition.flow)
            case badCaseInfo: {
                info++;
            }

            case caseInfo: {
                info++;
            }

            case caseNoInfo: {
                info++;
            }
        }
    }
}

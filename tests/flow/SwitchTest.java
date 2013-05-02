import sparta.checkers.quals.*;
import sparta.checkers.quals.SPARTA_Permission;
import sparta.checkers.quals.SPARTA_Permission;
import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.PolySinks;
import sparta.checkers.quals.PolySources;

class SwitchTest {
    void foo() {
        @Sources({SPARTA_Permission.LOCATION, SPARTA_Permission.LITERAL}) int info = 1;

        //Explicitly forbid this field from having SPARTA_Permission.CONDITIONAL
        //:: error: (forbidden.flow)
        @Sources({SPARTA_Permission.LOCATION}) @Sinks({}) int badInfo = 1;

        @Sources(SPARTA_Permission.LITERAL) int noInfo = 1;

        //This field gets SPARTA_Permission.CONDITIONAL added by default
        final @Sources({SPARTA_Permission.LOCATION, SPARTA_Permission.LITERAL}) int caseInfo = 1;
        final int caseNoInfo = 2;


        //Explicitly forbid this field from having SPARTA_Permission.CONDITIONAL
        //:: error: (forbidden.flow)
        final @Sources({SPARTA_Permission.LOCATION, SPARTA_Permission.LITERAL}) @Sinks({}) int badCaseInfo = 3;

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

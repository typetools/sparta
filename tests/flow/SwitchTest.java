import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import static sparta.checkers.quals.CoarseFlowPermission.*;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;

class SwitchTest {
    void foo(@Sink(INTERNET) int badInfo) {
        @Source({CoarseFlowPermission.ACCESS_FINE_LOCATION, CoarseFlowPermission.LITERAL}) int info = 1;


        @Source(CoarseFlowPermission.LITERAL) int noInfo = 1;

        //This field gets CoarseFlowPermission.CONDITIONAL added by default
        final @Source({CoarseFlowPermission.ACCESS_FINE_LOCATION, CoarseFlowPermission.LITERAL}) int caseInfo = 1;
        final int caseNoInfo = 2;


        //Explicitly forbid this field from having CoarseFlowPermission.CONDITIONAL
        //:: error: (forbidden.flow)
        final @Source({CoarseFlowPermission.ACCESS_FINE_LOCATION, CoarseFlowPermission.LITERAL}) @Sink({}) int badCaseInfo = 3;

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
        //This works because the inferred type is @Source(LITERAL)
            case badCaseInfo: {
                info++;
            }
            case caseNoInfo: {
                info++;
            }
        }

        switch (noInfo) {
            //This works because the inferred type is @Source(LITERAL)
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

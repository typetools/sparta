import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import static sparta.checkers.quals.FlowPermission.*;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;

class SwitchTest {
    void foo(@Sink(INTERNET) int badInfo) {
        @Source({FlowPermission.ACCESS_FINE_LOCATION}) int info = 1;


        @Source({}) int noInfo = 1;

        
        final @Source({FlowPermission.ACCESS_FINE_LOCATION}) int caseInfo = 1;
        final int caseNoInfo = 2;


        final @Source({FlowPermission.ACCESS_FINE_LOCATION}) @Sink({}) int badCaseInfo = 3;

        switch (info) {

            case caseInfo: {
                info++;
            }
            case caseNoInfo: {
                info++;
            }
        }

        switch (badInfo) {
        //This works because the inferred type is @Source()
            case badCaseInfo: {
                info++;
            }
            case caseNoInfo: {
                info++;
            }
        }

        switch (noInfo) {
            //This works because the inferred type is @Source()
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

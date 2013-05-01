import sparta.checkers.quals.*;
import sparta.checkers.quals.Sinks.FlowSink;
import sparta.checkers.quals.Sources.FlowSource;
import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.PolySinks;
import sparta.checkers.quals.PolySources;

class SwitchTest {
    void foo() {
        @Sources({FlowSource.LOCATION, FlowSource.LITERAL}) int info = 1;

        //Explicitly forbid this field from having FlowSink.CONDITIONAL
        //:: error: (forbidden.flow)
        @Sources({FlowSource.LOCATION}) @Sinks({}) int badInfo = 1;

        @Sources(FlowSource.LITERAL) int noInfo = 1;

        //This field gets FlowSink.CONDITIONAL added by default
        final @Sources({FlowSource.LOCATION, FlowSource.LITERAL}) int caseInfo = 1;
        final int caseNoInfo = 2;


        //Explicitly forbid this field from having FlowSink.CONDITIONAL
        //:: error: (forbidden.flow)
        final @Sources({FlowSource.LOCATION, FlowSource.LITERAL}) @Sinks({}) int badCaseInfo = 3;

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

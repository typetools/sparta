import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSinks.FlowSink;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks;
import sparta.checkers.quals.PolyFlowSinks;
import sparta.checkers.quals.PolyFlowSources;

class SwitchTest {
    void foo() {
        @FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) int info = 1;

        //Explicitly forbid this field from having FlowSink.CONDITIONAL
        //:: error: (forbidden.flow)
        @FlowSources({FlowSource.LOCATION}) @FlowSinks({}) int badInfo = 1;

        @FlowSources(FlowSource.LITERAL) int noInfo = 1;

        //This field gets FlowSink.CONDITIONAL added by default
        final @FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) int caseInfo = 1;
        final int caseNoInfo = 2;


        //Explicitly forbid this field from having FlowSink.CONDITIONAL
        //:: error: (forbidden.flow)
        final @FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) @FlowSinks({}) int badCaseInfo = 3;

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

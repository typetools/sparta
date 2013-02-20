import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSinks.FlowSink;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks;
import sparta.checkers.quals.PolyFlowSinks;
import sparta.checkers.quals.PolyFlowSources;

class SwitchTest {
    void foo() {
        @FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) int info = 1;
        @FlowSources(FlowSource.LITERAL) int noInfo = 1;
        final @FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) int caseInfo = 1;
        final int caseNoInfo = 2;

        //:: error: (condition.flow)
        switch (info) {
            //:: error: (condition.flow)
            case caseInfo: {
                info++;
            }
            case caseNoInfo: {
                info++;
            }
        }

        switch (noInfo) {
            //:: error: (condition.flow)
            case caseInfo: {
                info++;
            }
            case caseNoInfo: {
                info++;
            }
        }
    }
}

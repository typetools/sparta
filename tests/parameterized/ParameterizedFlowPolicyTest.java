import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermissionString.*;

class ParameterizedFlowPolicyTestSinks {

    //Testing: READ_SMS -> WRITE_LOGS("/var/*, tmp")
    @Source(READ_SMS) @Sink(WRITE_LOGS+"(/var/*)") long oneExactParam;
    @Source(READ_SMS) @Sink(WRITE_LOGS+"(/var/www/html)") long oneMatchingParam;
    // :: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(WRITE_LOGS+"(*)") long oneNotMathingReverseDoesMatch;
    // :: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(WRITE_LOGS+"(/other/var/)") long oneNotMatching;
    // :: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(WRITE_LOGS+"(tmpabc)") long otherParamNotMatch;
    @Source(READ_SMS) @Sink(WRITE_LOGS+"(/var/*, tmp)") long twoEqualParams;
    @Source(READ_SMS) @Sink(WRITE_LOGS+"(tmp, /var/www/html)") long twoMatching;
    // :: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(WRITE_LOGS+"(/var/*, tmpabc)") long twoOneEqualsOneDoesNot;
    // :: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(WRITE_LOGS+"(/tmp, /var/www/html)") long twoOneMatchesOneDoesNot ;
    // :: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(WRITE_LOGS+"(/home/var/*, tmp)") long anothertwoOneMatchesOneDoesNot;
    // :: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(WRITE_LOGS+"(/tmp, banana)") long twoNotMatching;
    // :: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(WRITE_LOGS+"(/var/*, no)") long anothertwoOneEqualsOneDoesNot;
    // :: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(value={FILESYSTEM, WRITE_LOGS+"(/var/*)"}) long extraSink;
    // :: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(value={FILESYSTEM, WRITE_LOGS+"(/var/www/html)"}) long extraSinkMatching;
    @Source(READ_SMS) @Sink(WRITE_LOGS+"(/var/*)") long matchingFineSourceNoParam;
    @Source(READ_SMS+"(*)") @Sink(WRITE_LOGS+"(/var/*)") long matchingFineSourceStarPara;
    @Source(READ_SMS+"(tomyfriend)") @Sink(WRITE_LOGS+"(/var/*)") long matchingFineSourceMathingPara;
    // :: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(WRITE_LOGS) long noParamsFineSink;
    // :: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(WRITE_LOGS) long noParams;
    @Source(READ_SMS+"(tmp)") @Sink(WRITE_LOGS+"(/var/*)") long matchingFineSourceParaMathingPara;
}

class TestSources{

    /*
     *    INTERNET("domain.com, otherdomain.com/*") -> WRITE_SMS
    FILESYSTEM("myfile1.txt, home/*") -> FILESYSTEM("myfile1.txt, home/*")
     */
    //Testing: INTERNET("domain.com, otherdomain.com/*") -> WRITE_SMS
//    @Source(finesources=@FineSource(value=INTERNET, p)+"(tomyfriend") @Sink(WRITE_LOGS+"(/var/*)") String s;

}

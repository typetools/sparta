import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;

class ParameterizedFlowPolicyTestSinks {

    //Testing: READ_SMS -> WRITE_LOGS("/var/*", "tmp")
    @Source(READ_SMS) @Sink(finesinks=@FineSink(value=WRITE_LOGS, params="/var/*")) long oneExactParam;    
    @Source(READ_SMS) @Sink(finesinks=@FineSink(value=WRITE_LOGS, params="/var/www/html")) long oneMatchingParam;
    //:: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(finesinks=@FineSink(value=WRITE_LOGS, params="*")) long oneNotMathingReverseDoesMatch;
    //:: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(finesinks=@FineSink(value=WRITE_LOGS, params="/other/var/")) long oneNotMatching;
    //:: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(finesinks=@FineSink(value=WRITE_LOGS, params="tmpabc")) long otherParamNotMatch;
    @Source(READ_SMS) @Sink(finesinks=@FineSink(value=WRITE_LOGS, params={"/var/*", "tmp"})) long twoEqualParams;    
    @Source(READ_SMS) @Sink(finesinks=@FineSink(value=WRITE_LOGS, params={"tmp","/var/www/html"})) long twoMatching;
    //:: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(finesinks=@FineSink(value=WRITE_LOGS, params={"/var/*", "tmpabc"})) long twoOneEqualsOneDoesNot;
    //:: error: (forbidden.flow)    
    @Source(READ_SMS) @Sink(finesinks=@FineSink(value=WRITE_LOGS, params={"/tmp","/var/www/html"})) long twoOneMatchesOneDoesNot ;
    //:: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(finesinks=@FineSink(value=WRITE_LOGS, params={"/home/var/*", "tmp"})) long anothertwoOneMatchesOneDoesNot;
    //:: error: (forbidden.flow)   
    @Source(READ_SMS) @Sink(finesinks=@FineSink(value=WRITE_LOGS, params={"/tmp","banana"})) long twoNotMatching;
    //:: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(finesinks=@FineSink(value=WRITE_LOGS, params={"/var/*", "no"})) long anothertwoOneEqualsOneDoesNot;   
    //:: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(value=FILESYSTEM, finesinks=@FineSink(value=WRITE_LOGS, params="/var/*")) long extraSink;     
    //:: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(value=FILESYSTEM, finesinks=@FineSink(value=WRITE_LOGS, params="/var/www/html")) long extraSinkMatching;
    @Source(finesources=@FineSource(READ_SMS)) @Sink(finesinks=@FineSink(value=WRITE_LOGS, params="/var/*")) long matchingFineSourceNoParam; 
    @Source(finesources=@FineSource(value=READ_SMS, params="*")) @Sink(finesinks=@FineSink(value=WRITE_LOGS, params="/var/*")) long matchingFineSourceStarPara;   
    @Source(finesources=@FineSource(value=READ_SMS, params="tomyfriend")) @Sink(finesinks=@FineSink(value=WRITE_LOGS, params="/var/*")) long matchingFineSourceMathingPara;
    //:: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(finesinks=@FineSink(WRITE_LOGS)) long noParamsFineSink;
    //:: error: (forbidden.flow)
    @Source(READ_SMS) @Sink(WRITE_LOGS) long noParams;
    @Source(finesources=@FineSource(value=READ_SMS, params="tmp")) @Sink(finesinks=@FineSink(value=WRITE_LOGS, params="/var/*")) long matchingFineSourceParaMathingPara;
}

class TestSources{
    
    /*
     *    INTERNET("domain.com", "otherdomain.com/*") -> WRITE_SMS
    FILESYSTEM("myfile1.txt", "home/*") -> FILESYSTEM("myfile1.txt", "home/*") 
     */
    //Testing: INTERNET("domain.com", "otherdomain.com/*") -> WRITE_SMS
//    @Source(finesources=@FineSource(value=INTERNET, p), params="tomyfriend") @Sink(finesinks=@FineSink(value=WRITE_LOGS, params="/var/*")) String s;    

}

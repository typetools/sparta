import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;

class ParameterizedFlowPolicyTest {
    @Source(LITERAL) @Sink(value={}, finesinks={@FineSink(value=WRITE_LOGS, params={"/var/*"})}) long time = 5;
    
    void testFlowPolicy() {    
        @Source(LITERAL) @Sink(value={}, finesinks={@FineSink(value=WRITE_LOGS, params={"/var/www/html"})}) long timePass;
        @Source(LITERAL) @Sink(value={}, finesinks={@FineSink(value=WRITE_LOGS, params={"/*"})}) long timeFail;
        
        // Test assignments
        timePass = time;
        //:: error: (assignment.type.incompatible)
        timeFail = time;
        
        // Test method calls
        testSinkPass(time);
        
        //:: error: (argument.type.incompatible)
        testSinkFail1(time);
        //:: error: (argument.type.incompatible)
        testSinkFail2(time);        
    }
    
    void testSinkFail1(@Source(LITERAL) @Sink(value={}, finesinks={@FineSink(value=WRITE_LOGS, params={"/usr/bin"})}) long time) {
    }
    
    void testSinkFail2(@Source(LITERAL) @Sink(value={}, finesinks={@FineSink(value=WRITE_LOGS, params={"/*"})}) long time) {
    }
    
    void testSinkPass(@Source(LITERAL) @Sink(value={}, finesinks={@FineSink(value=WRITE_LOGS, params={"/var/log"})}) long time) {
        
    }
}

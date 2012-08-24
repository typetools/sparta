import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSinks.FlowSink;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks;
import sparta.checkers.quals.PolyFlowSinks;
import sparta.checkers.quals.PolyFlowSources;

class ArrayCast {
    
    void foo() {
 	   @SuppressWarnings("flow")
 	   @FlowSinks(FlowSink.NETWORK) Object[] params = new /*@FlowSinks(FlowSink.NETWORK)*/ Object[1]; 
 	   Object[] result = (Object[]) call("method", params);
 	   callFinished(result);
    }
     
    @FlowSources(FlowSource.NETWORK) @FlowSinks(FlowSink.NETWORK) Object call(
 		   String method, @FlowSinks(FlowSink.NETWORK) Object[] params) {
 	   @FlowSources(FlowSource.NETWORK) @FlowSinks(FlowSink.NETWORK) Object a = params[0];
 	   return a;
    }
    
     void callFinished(@FlowSources(FlowSource.NETWORK) @FlowSinks(FlowSink.NETWORK) Object[] result) {
     	
     }
}

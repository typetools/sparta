import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.FlowSource;
import sparta.checkers.quals.Sinks.FlowSink;
import sparta.checkers.quals.PolyFlow;

@PolyFlow
class HttpGet {
    @Sinks(FlowSink.NETWORK)  HttpGet( 
            @Sinks(FlowSink.NETWORK)  String uri) {
    }
}

class PolyConstructors {

    void testPolyConstructor(@Sources(FlowSource.LOCATION) @Sinks(FlowSink.NETWORK) String in) {	
    	@Sources(FlowSource.LOCATION) @Sinks(FlowSink.NETWORK)
//:: error: (constructor.invocation.invalid)   
    	HttpGet request = new HttpGet(in);
    }

}

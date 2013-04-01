import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;
import sparta.checkers.quals.PolyFlow;

@PolyFlow
class HttpGet {
    @FlowSinks(FlowSink.NETWORK)  HttpGet( 
            @FlowSinks(FlowSink.NETWORK)  String uri) {
    }
}

class PolyConstructors {

//:: error: (constructor.invocation.invalid)   
    void testPolyConstructor(@FlowSources(FlowSource.LOCATION) @FlowSinks(FlowSink.NETWORK) String in) {	
    	@FlowSources(FlowSource.LOCATION) @FlowSinks(FlowSink.NETWORK)
    	HttpGet request = new HttpGet(in);
    }

}

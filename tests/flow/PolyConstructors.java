import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;

class HttpGet {
    @FlowSinks(FlowSink.NETWORK) @PolyFlowSources HttpGet(
            @FlowSinks(FlowSink.NETWORK) @PolyFlowSources HttpGet this, // TODO: valid? remove?
            @FlowSinks(FlowSink.NETWORK) @PolyFlowSources String uri) {
    }
}

class PolyConstructors {

    @FlowSources(FlowSource.LOCATION) @FlowSinks(FlowSink.NETWORK) HttpGet request;
    void testPolyConstructor(@FlowSources(FlowSource.LOCATION) @FlowSinks(FlowSink.NETWORK) String in) {	
        request = new HttpGet(in);
    }

}
import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;

import org.apache.http.client.methods.HttpGet;
class MiscTests {
	
	@FlowSources(FlowSource.LOCATION) @FlowSinks(FlowSink.NETWORK) HttpGet request;
	void testPolyConstructor() {	
		@SuppressWarnings("flow")
		@FlowSources(FlowSource.LOCATION) @FlowSinks(FlowSink.NETWORK) String in = "asdf";
		
		request = new HttpGet(in);
	}
	
}
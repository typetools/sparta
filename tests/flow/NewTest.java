import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;

import java.io.*;
import java.net.*;
class NewTest {
	@SuppressWarnings("flow")
	private @FlowSinks(FlowSink.NETWORK) TestClass sdf;
	@SuppressWarnings("flow") //Variable initialization.
	private @FlowSinks(FlowSink.NETWORK) String param = "jkl";
	
	void method() {
		//:: error: (assignment.type.incompatible)
		sdf = new TestClass(param);
		sdf = new /*@FlowSinks(FlowSink.NETWORK)*/ TestClass(param); 
	}
	
	
	private URL tempUrl;
	//private URLConnection connection;
	//private InputStream stream;
	//private BufferedInputStream in;
	//private ByteArrayOutputStream out;
	
	void foo() {
		@SuppressWarnings("flow")
		@FlowSinks(FlowSink.FILESYSTEM) @FlowSources({}) String url = "";
		@SuppressWarnings("flow") 
		@FlowSinks(FlowSink.FILESYSTEM) @FlowSources({}) File cache;
		
		try {
			//Note: annotation for URL() is: 
			//@PolyFlowSources @PolyFlowSinks URL(@PolyFlowSources @PolyFlowSinks String spec);
			
			//:: error: (assignment.type.incompatible)
			tempUrl = new @FlowSinks(FlowSink.FILESYSTEM) URL(url);
			//connection = tempUrl.openConnection();
			//stream = connection.getInputStream();
			//in = new BufferedInputStream(stream);
			//out = new ByteArrayOutputStream(10240);
		} 
		catch (Throwable t) {
			
		}
	}
}

class TestClass {
	
	public TestClass(/*>>> @FlowSinks(FlowSink.NETWORK) TestClass this, */ @FlowSinks(FlowSink.NETWORK) String param) {
		
	}
}
import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;

class NewTest {
	@SuppressWarnings("flow")
	private @FlowSinks(FlowSink.NETWORK) TestClass sdf;
	@SuppressWarnings("flow") //Variable initialization.
	private @FlowSinks(FlowSink.NETWORK) String param = "jkl";
	void method() {
		sdf = new TestClass(param);
		sdf = new /*@FlowSinks(FlowSink.NETWORK)*/ TestClass(param); 
	}
}

class TestClass {
	
	public TestClass(/*>>> @FlowSinks(FlowSink.NETWORK) TestClass this, */ @FlowSinks(FlowSink.NETWORK) String param) {
		
	}
}
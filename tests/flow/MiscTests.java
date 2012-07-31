import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;

class MiscTests {
	@SuppressWarnings("flow")
	@FlowSinks(FlowSink.NETWORK) String WEBSERVICE_URL = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";
    
    @FlowSources(FlowSource.LOCATION) String WEBSERVICE_URL2 = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";
    
    String WEBSERVICE_URL3 = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";
	String result;
	/*
	void test_StringFormat_StringFlowSink() {
		double lat = 1.0;
		double lon = 2.0;
		int days = 3;
		result = String.format(WEBSERVICE_URL, lat, lon, days);
	}
	
	void test_StringFormat_StringFlowSource() {
		double lat = 1.0;
		double lon = 2.0;
		int days = 3;
		//::error: (assignment.type.incompatible)
		result = String.format(WEBSERVICE_URL2, lat, lon, days);
	}
	*/
	void test_StringFormat_ObjectFlowSink() {
		@SuppressWarnings("flow")
		@FlowSinks(FlowSink.NETWORK) @FlowSources double lat = 1.0;
		@SuppressWarnings("flow")
		@FlowSinks(FlowSink.NETWORK) @FlowSources double lon = 2.0;
		@SuppressWarnings("flow")
		@FlowSinks(FlowSink.NETWORK) @FlowSources int days = 3;
		result = String.format(WEBSERVICE_URL3, lat, lon, days);
	}
	/*
	void test_StringFormat_ObjectFlowSource() {
		@FlowSources(FlowSource.LOCATION) double lat = 1.0;
		@FlowSources(FlowSource.LOCATION) double lon = 2.0;
		@FlowSources(FlowSource.LOCATION) int days = 3;
		//::error: (assignment.type.incompatible)
		result = String.format(WEBSERVICE_URL3, lat, lon, days);
	}*/
}
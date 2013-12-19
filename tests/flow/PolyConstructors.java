import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.PolyFlow;

@PolyFlow
class HttpGet {
    @Sink(FlowPermission.INTERNET)  HttpGet( 
            @Sink(FlowPermission.INTERNET)  String uri) {
    }
}

class PolyConstructors {

    void testPolyConstructor(@Source(FlowPermission.ACCESS_FINE_LOCATION) @Sink(FlowPermission.INTERNET) String in) {	
    	@Source(FlowPermission.ACCESS_FINE_LOCATION) @Sink(FlowPermission.INTERNET)

    	HttpGet request = new HttpGet(in);
    }

}

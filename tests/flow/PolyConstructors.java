import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.PolyFlow;

@PolyFlow
class HttpGet {
    @Sink(FlowPermissionString.INTERNET)  HttpGet(
            @Sink(FlowPermissionString.INTERNET)  String uri) {
    }
}

class PolyConstructors {

    void testPolyConstructor(@Source(FlowPermissionString.ACCESS_FINE_LOCATION) @Sink(FlowPermissionString.INTERNET) String in) {	
    	@Source(FlowPermissionString.ACCESS_FINE_LOCATION) @Sink(FlowPermissionString.INTERNET)

    	HttpGet request = new HttpGet(in);
    }

}

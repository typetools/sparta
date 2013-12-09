import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.PolyFlow;

@PolyFlow
class HttpGet {
    @Sink(CoarseFlowPermission.INTERNET)  HttpGet( 
            @Sink(CoarseFlowPermission.INTERNET)  String uri) {
    }
}

class PolyConstructors {

    void testPolyConstructor(@Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) @Sink(CoarseFlowPermission.INTERNET) String in) {	
    	@Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) @Sink(CoarseFlowPermission.INTERNET)

    	HttpGet request = new HttpGet(in);
    }

}

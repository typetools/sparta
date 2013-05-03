import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;
import sparta.checkers.quals.SpartaPermission;
import sparta.checkers.quals.PolyFlow;

@PolyFlow
class HttpGet {
    @Sinks(SpartaPermission.INTERNET)  HttpGet( 
            @Sinks(SpartaPermission.INTERNET)  String uri) {
    }
}

class PolyConstructors {

    void testPolyConstructor(@Sources(SpartaPermission.LOCATION) @Sinks(SpartaPermission.INTERNET) String in) {	
    	@Sources(SpartaPermission.LOCATION) @Sinks(SpartaPermission.INTERNET)
//:: error: (constructor.invocation.invalid)   
    	HttpGet request = new HttpGet(in);
    }

}

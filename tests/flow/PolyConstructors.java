import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.SPARTA_Permission;
import sparta.checkers.quals.Sinks.SPARTA_Permission;
import sparta.checkers.quals.PolyFlow;

@PolyFlow
class HttpGet {
    @Sinks(SPARTA_Permission.NETWORK)  HttpGet( 
            @Sinks(SPARTA_Permission.NETWORK)  String uri) {
    }
}

class PolyConstructors {

    void testPolyConstructor(@Sources(SPARTA_Permission.LOCATION) @Sinks(SPARTA_Permission.NETWORK) String in) {	
    	@Sources(SPARTA_Permission.LOCATION) @Sinks(SPARTA_Permission.NETWORK)
//:: error: (constructor.invocation.invalid)   
    	HttpGet request = new HttpGet(in);
    }

}

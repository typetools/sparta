import sparta.checkers.quals.*;
import sparta.checkers.quals.Source.*;
import sparta.checkers.quals.Sink.*;
import static sparta.checkers.quals.CoarseFlowPermission.*; 

@PolyFlow
class Cons {
	String get(String s){
		return s;
	}
	@PolyFlowReceiver
	String m(String s){return s;}
}

class Use {

	@Source(LITERAL) String s = "";
	@Source(READ_SMS) String k;
	@Source({LITERAL, READ_SMS}) String readLit;

    void demo(@Source(LITERAL) Cons c) {
       //:: error: (assignment.type.incompatible) 
       s = c.get(k);
       readLit = c.get(k);
       
       s = c.m(s);
       readLit = c.m(s);
       readLit = c.m(k);

       //:: error: (assignment.type.incompatible) 
       s = c.m(k);
    
       s = c.m(s);


    }
}
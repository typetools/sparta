import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.*;
import sparta.checkers.quals.FlowSinks.*;

@PolyFlow
class Cons {
	String get(String s){
		return s;
	}
}

class Use {

	String s = "";
	String k;

    void demo(Cons c) {
       s = c.get(k);

    }
}
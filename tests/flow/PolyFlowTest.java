import sparta.checkers.quals.*;
import sparta.checkers.quals.Source.*;
import sparta.checkers.quals.Sink.*;

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
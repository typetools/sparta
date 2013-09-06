
import java.util.HashMap;

import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;

class ListTest {
    // Simple test to ensure that defaulting on java.util.List works correctly.
    java.util.List<String> s;
    String t = s.get(1);
}


class List<T extends @Sink(CONDITIONAL) @Source(ANY) Object> {
    
    T getF( @Source(ANY) List<T> this, int index) { return null; }
    void addF(T p) {}
}

class Generics {

    List<Object> lo = new List<Object>();
    List<@Source(FlowPermission.INTERNET) Object> netok = new List<@Source(FlowPermission.INTERNET) Object>();
    
   
    List<@Source(FlowPermission.INTERNET) Object> netok2 = foo();
 
    //:: error: (assignment.type.incompatible)
    List<@Source(INTERNET) Object> neterr = new List<Object>();
    void use(Object o, @Source(INTERNET) Object neto) {

        netok.addF(neto);
        neto = netok.getF(4);
        //:: error: (assignment.type.incompatible)
        o = netok.getF(4);
    }

    List<@Source(FlowPermission.INTERNET) Object> foo() {

    	return new List<@Source(FlowPermission.INTERNET) Object>();
    }

}

class UpperObject<T extends Object>{
	public void testWildCard(java.util.List<? extends Number> list) {
	}   
	void callWildCard(java.util.List<Integer> list){
		testWildCard(list);
	}
}

class GenObject<@Source(ANY) @Sink({}) Object>{
}
class GenObjectLit<Object>{
}

class TestUpperObject{
    void test(){
        UpperObject<Object> lit;
        UpperObject<@Source(LITERAL) @Sink(CONDITIONAL) Object> lit2;

        //:: error: (forbidden.flow)
        UpperObject<@Source(ANY) @Sink({}) Object> lit3;
        //:: error: (type.argument.type.incompatible)
        GenObject<Object> gen;
        
        //:: error: (forbidden.flow)
        GenObject<@Source(ANY) @Sink({}) Object> gen2;
        GenObjectLit<String> o;
    }
}
    

class TypeAsKeyHashMap<T> {

	public <S extends T> S get(T type) {
		//:: warning: [unchecked] unchecked cast :: warning: (cast.unsafe)
		return (S) type;
	}
}


// class TypeAsKeyHashMap<T> {
//	private HashMap<Class<? extends T>, T> mCollection = new HashMap<Class<? extends T>, T>();
//	
//	public void put(Class<? extends T> type, T value){
//		mCollection.put(type, value);
//	}
//	
//	@SuppressWarnings("unchecked")
//	public <S extends T> S get(Class<S> type){
//		if (mCollection.containsKey(type)){
//			return (S)mCollection.get(type);
//		}else{
//			return null;
//		}
//	}
//	
//	public void remove(Class<? extends T> type){
//		mCollection.remove(type);
//	}
//	
//	public boolean containsKey(Class<? extends T> type){
//		return mCollection.containsKey(type);
//	}
//}
     
     
     

      class AppList<T extends Comparable<T>> {
      
      }
    





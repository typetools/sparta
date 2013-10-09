

import java.util.*;

import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;


class FlowPoilcyDefaulting {

    List<Object> lo = new ArrayList<Object>();
    List<@Source(FlowPermission.INTERNET) Object> netok = new ArrayList<@Source(FlowPermission.INTERNET) Object>();
    
   
    List<@Source(FlowPermission.INTERNET) Object> netok2 = foo();
 
    //:: error: (assignment.type.incompatible)
    List<@Source(INTERNET) Object> neterr = new ArrayList<Object>();
    void use(Object o, @Source(INTERNET) Object neto) {

        netok.add(neto);
        neto = netok.get(4);
        //:: error: (assignment.type.incompatible)
        o = netok.get(4);
    }

    List<@Source(FlowPermission.INTERNET) Object> foo() {
        return new ArrayList<@Source(FlowPermission.INTERNET) Object>();
    }
    
    void method(@Source(INTERNET) String s){
        sendToInternet(s);
        //:: warning: (cast.unsafe)
        String s1 = (@Source(INTERNET) String) "hello";
        //TODO: BUG
        sendToInternet(s1);
        
        UpperObject<@Source(INTERNET)  Object> uo = new UpperObject<>();
        sendToInternet(uo.getT());
        RecieverTest rt = new RecieverTest();
        //:: error: (method.invocation.invalid)
        rt.internetReciever();
        
        //:: warning: (cast.unsafe)
        RecieverTest uo2 = (@Source(INTERNET) RecieverTest) new RecieverTest();
        uo2.internetReciever();

    }
    void sendToInternet(@Sink(INTERNET) Object o){}

}
class RecieverTest{
    
    void internetReciever(@Source(INTERNET) RecieverTest this){}
}

class UpperObject<@Source(INTERNET) T>{

    T t;
    public void testWildCard(java.util.List<@Source(INTERNET) ?> list) {}   
    void callWildCard(java.util.List<@Source(INTERNET) Integer> list)
    {
        testWildCard(list);    
    }
    T getT()
    {
        return t;
    }
        
    
}



    






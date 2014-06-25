

import java.util.*;

import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;


class FlowPoilcyDefaulting {

    List<Object> lo = new ArrayList<Object>();
    List<@Source(FlowPermission.INTERNET) Object> netok = new ArrayList<@Source(FlowPermission.INTERNET) Object>();
    
   
    @Source(ANY) @Sink({}) List<@Source(FlowPermission.INTERNET) Object> netok2 = foo();
 
    //:: error: (assignment.type.incompatible)
    List<@Source(INTERNET) Object> neterr = new ArrayList<Object>();
    void use(@Source(ANY) Object o, @Source(INTERNET) Object neto) {

        netok.add(neto);
        neto = netok.get(4);
        o = netok.get(4);
    }

    @Source(ANY) @Sink({}) List<@Source(FlowPermission.INTERNET) Object> foo() {
        return new ArrayList<@Source(FlowPermission.INTERNET) Object>();
    }

   @Source({}) @Sink({}) RecieverTest rt = new RecieverTest();
    void method(@Source(INTERNET) String s){
        sendToInternet(s);
        String s1 = (@Source(INTERNET) String) "hello";
        sendToInternet(s1);
        
        UpperObject<@Source(INTERNET)  Object> uo = new UpperObject<>();
        sendToInternet(uo.getT());

        //:: error: (method.invocation.invalid)
        rt.internetReciever();
        

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



    






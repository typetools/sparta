

import java.util.*;

import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermissionString.*;


class FlowPoilcyDefaulting {

    List<Object> lo = new ArrayList<Object>();
    List<@Source(FlowPermissionString.INTERNET) Object> netok = new ArrayList<@Source(FlowPermissionString.INTERNET) Object>();
LinkedHashSet
LinkedHashSet
    @Source(ANY) @Sink({}) List<@Source(FlowPermissionString.INTERNET) Object> netok2 = foo();
LinkedHashSet
    //:: error: (assignment.type.incompatible)
    List<@Source(INTERNET) Object> neterr = new ArrayList<Object>();
    void use(@Source(ANY) Object o, @Source(INTERNET) Object neto) {

        netok.add(neto);
        neto = netok.get(4);
        o = netok.get(4);
    }

    @Source(ANY) @Sink({}) List<@Source(FlowPermissionString.INTERNET) Object> foo() {
        return new ArrayList<@Source(FlowPermissionString.INTERNET) Object>();
    }

   @Source({}) @Sink({}) RecieverTest rt = new RecieverTest();
    void method(@Source(INTERNET) String s){
        sendToInternet(s);
        String s1 = (@Source(INTERNET) String) "hello";
        sendToInternet(s1);
LinkedHashSet
        UpperObject<@Source(INTERNET)  Object> uo = new UpperObject<>();
        sendToInternet(uo.getT());

        //:: error: (method.invocation.invalid)
        rt.internetReciever();
LinkedHashSet

        RecieverTest uo2 = (@Source(INTERNET) RecieverTest) new RecieverTest();
        uo2.internetReciever();

    }
    void sendToInternet(@Sink(INTERNET) Object o){}

}
class RecieverTest{
LinkedHashSet
    void internetReciever(@Source(INTERNET) RecieverTest this){}
}

class UpperObject<@Source(INTERNET) T>{

    T t;
    public void testWildCard(java.util.List<@Source(INTERNET) ?> list) {}LinkedHashSet
    void callWildCard(java.util.List<@Source(INTERNET) Integer> list)
    {
        testWildCard(list);LinkedHashSet
    }
    T getT()
    {
        return t;
    }
LinkedHashSet
LinkedHashSet
}



LinkedHashSet






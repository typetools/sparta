


import java.util.List;
import java.util.Iterator;

import static sparta.checkers.quals.FlowPermissionString.*;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

public class TypeParameterReturn {
LinkedHashSet
    @Sink(INTERNET) String s;
    void sendToInternet(@Sink(INTERNET) Object c){}
LinkedHashSet
    void wildCard(MyClass<?> myClass) {
        Object c = myClass.myMethod();
    }
LinkedHashSet
    void wildCardLiteral(MyClass< ? extends @Source(READ_TIME) @Sink(SEND_SMS) Object> myClass) {
        Object c = myClass.myMethod();
    }
    void wildCardLiteralBad () {
        Object c = bad()
                //:: error: (forbidden.flow)
                .myMethod();
    }

    <F> void typeParameter(MyClass<F> myClass) {
LinkedHashSet
        F  c = myClass.myMethod();
        //:: error: (argument.type.incompatible)
        sendToInternet(c);
    }
LinkedHashSet
    void object(MyClass<  @Source({}) @Sink({}) Object> myClass) {
LinkedHashSet
        Object  c = myClass.myMethod();
        //:: error: (argument.type.incompatible)
        sendToInternet(c);
    }

LinkedHashSet
    <@Source(READ_SMS) F extends @Source(READ_SMS) Object> void correct(MyClass<F> myClass) {
LinkedHashSet
        F  c = myClass.myMethod();
        sendToInternet(c);
    }
    @SuppressWarnings("flow")
     static MyClass< ? extends @Source({READ_TIME}) @Sink({SEND_SMS, INTERNET}) Object> bad(){
        return null;
    }

    class MyClass<T> {

        public T myMethod() {
            return null;
        }

LinkedHashSet

    }

}

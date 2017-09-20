import java.lang.reflect.Method;

import org.checkerframework.common.reflection.qual.MethodVal;

import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import static sparta.checkers.quals.FlowPermissionString.*;

public class ReflectionTest {
    static public final String CN = "ReflectionTest";
    @Source(READ_SMS) @Sink(INTERNET) Object returnM() {
        return null;
    }
    void returnMTest(@MethodVal(className=CN, methodName="returnM", params=0) Method m) throws Exception{
        @Source(READ_SMS) @Sink(INTERNET) Object o = m.invoke(this);
    }

    @Source(READ_SMS) @Sink(INTERNET) Object returnParameter(
            @Source(READ_SMS) @Sink(INTERNET) Object o1) {
        return null;
    }
    @Source(READ_SMS) @Sink(INTERNET) Object o1 = new Object();
    void returnParameterTest(@MethodVal(className=CN, methodName="returnParameter", params=1) Method m) throws Exception{
        @Source(READ_SMS) @Sink(INTERNET) Object o = m.invoke(this,o1);
    }
LinkedHashSet
    void parameter( @Source(READ_SMS) @Sink(INTERNET) Object o1) {
    }
LinkedHashSet
    void parameterTest(@MethodVal(className=CN, methodName="parameter", params=1) Method m) throws Exception{
        @Source({}) @Sink(ANY) Object o = m.invoke(this,o1);
    }
LinkedHashSet
   static @Source(READ_SMS) @Sink(INTERNET) Object returnParameterStatic(
            @Source(READ_SMS) @Sink(INTERNET) Object o1) {
        return null;
    }
   void returnParameterStaticTest(@MethodVal(className=CN, methodName="returnParameterStatic", params=1) Method m) throws Exception{
       @Source(READ_SMS) @Sink(INTERNET) Object oR = m.invoke(this,o1);
       @Source(READ_SMS) @Sink(INTERNET) Object oR2 = m.invoke(null,o1);
   }

}

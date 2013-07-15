import checkers.stringval.quals.*;
import checkers.reflection.quals.*;
import checkers.arraylen.quals.*;
import java.lang.reflect.*;

import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.PolyFlow;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import static sparta.checkers.quals.FlowPermission.*;

public class ReflectionTest {
	
	@SuppressWarnings("flow")
    @Source(READ_SMS)ReflectionTest smsRefTest = new ReflectionTest();
	
	private @Source(INTERNET) String getInternetData(){
    	@SuppressWarnings("flow")
    	@Source(INTERNET) String str = "Sensitive internet data";
    	return str;
    }	
	private @Source(READ_SMS) String getSmsData(@Source(READ_SMS) ReflectionTest this){
    	@SuppressWarnings("flow")
    	@Source(READ_SMS) String str = "Sensitive sms data";
    	return str;
    }
	private @Source(READ_SMS) String concatSmsData(@Source(READ_SMS) ReflectionTest this, @Source(READ_SMS) String str1, @Source(READ_SMS) String str2, @Source(READ_SMS) String str3){
    	@Source(READ_SMS) String str = str1+str2+str3;
    	return str;
    }
	private @Source(READ_SMS) String getSmsData(@Source(READ_SMS) ReflectionTest this, @Source(READ_SMS) String str){
		if(str!=null){
			return str;
		}
    	return getSmsData();
    }
	private String getSmsMethod(){
		return "getSmsData";
	}
	
	public void passSimple() {
        try {
        	Class c = Class.forName("ReflectionTest");
            Method m = c.getMethod("getSmsData", new Class[] {});
            @Source (READ_SMS) String str = (String) m.invoke(smsRefTest, (Object[])null);
        } catch (Exception e) {
        }

    }	
	public void failSimpleBadReceiver() {
        try {
        	Class c = Class.forName("ReflectionTest");
            Method m = c.getMethod("getSmsData", new Class[] {});
            //:: error: (argument.type.incompatible)
            @Source(READ_SMS)String str = (String) m.invoke(this, (Object[])null);
        } catch (Exception e) {
        }

    }
	
	public void passWith1Arg() {
        try {
        	Class c = Class.forName("ReflectionTest");
        	Method m = c.getMethod("getSmsData", new Class[] {String.class});   
        	@Source(READ_SMS)String str = (String) m.invoke(smsRefTest, smsRefTest.getSmsData());
        } catch (Exception e) {
        }

    }
	public void failWith1ArgBadParam() {
        try {
        	Class c = Class.forName("ReflectionTest");
            Method m = c.getMethod("getSmsData", new Class[] {String.class});
            //:: error: (argument.type.incompatible)
            String str = (String) m.invoke(smsRefTest, getInternetData());
        } catch (Exception e) {
        }

    }
	
	public void passWith2Args() {
        try {
        	Class c = Class.forName("java.lang.String");
            Method m = c.getMethod("substring", new Class[] {int.class, int.class});   
            String str = (String) m.invoke(smsRefTest.getSmsData(), 10, 12);
        } catch (Exception e) {
        }

    }
	
	public void passWith3Args() {
        try {
        	Class c = Class.forName("ReflectionTest");
            Method m = c.getMethod("concatSmsData", new Class[] {String.class, String.class, String.class});
            String str2 = smsRefTest.getSmsData();
            String str3 = str2;
            @Source(READ_SMS)String str = (String) m.invoke(smsRefTest, smsRefTest.getSmsData(), str2, str3);
        } catch (Exception e) {
        }

    }
	
	public void failWith3ArgsBadParameter() {
        try {
        	Class c = Class.forName("ReflectionTest");
            Method m = c.getMethod("concatSmsData", new Class[] {String.class, String.class, String.class});
            String str2 = smsRefTest.getSmsData();
            String str3 = str2;
            //:: error: (argument.type.incompatible)
            @Source(READ_SMS)String str = (String) m.invoke(smsRefTest, getInternetData(), str2, str3);
        } catch (Exception e) {
        }

    }
		
	public void passUnresolvedMethodNameCall() {
        try {
        	Class c = Class.forName("ReflectionTest");
            Method m = c.getMethod(getSmsMethod(), new Class[] {});            
            @Source(REFLECTION)String str = (String) m.invoke(this, (Object[])null);
        } catch (Exception e) {
        }

    }
	public void passUnresolvedClassName() {
        try {
        	Class c = Class.forName("ReflectionT");
            Method m = c.getMethod(getSmsMethod(), new Class[] {});            
            @Source(REFLECTION) String str = (String) m.invoke(this, (Object[])null);
        } catch (Exception e) {
        }
    }
}

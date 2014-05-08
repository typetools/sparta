package mypakage;
import sparta.checkers.quals.Source;
import static sparta.checkers.quals.FlowPermission.*;

/*These warnings are from information-flow.astub and they may change*/
//warning: StubParser: Skipping annotation type: android.annotation.TargetApi
//warning: StubParser: Skipping enum type: android.net.NetworkInfo.State
//warning: StubParser: Method getNextPoolable() not found in type android.view.VelocityTracker
//warning: StubParser: Method isPooled() not found in type android.view.VelocityTracker
//warning: StubParser: Method setNextPoolable(T) not found in type android.view.VelocityTracker
//warning: StubParser: Method setPooled(boolean) not found in type android.view.VelocityTracker
//warning: StubParser: Constructor <init>(ArrayList) not found in type android.view.ViewGroup
//warning: StubParser: Method onItemClick(AdapterView,View,int,long) not found in type android.widget.AdapterView.OnItemSelectedListener
//warning: StubParser: Type not found: com.google.android.maps.GeoPoint
//warning: StubParser: Skipping enum type: java.util.concurrent.TimeUnit
//warning: StubParser: Type not found: org.htmlcleaner.HtmlCleaner
//warning: StubParser: Type not found: org.htmlcleaner.TagNode
//warning: StubParser: Method idealByteArraySize(int) not found in type android.support.v4.util.LongSparseArray
//warning: StubParser: Method idealLongArraySize(int) not found in type android.support.v4.util.LongSparseArray
//warning: StubParser: Type not found: android.support.v4.print.PrintHelperKitKat

class TestStubImplicitConstructor { 
    
    static void stubSanity(String fail) { }
}

class TestStubNoParamConstructor {
    TestStubNoParamConstructor() { }
    
    static void stubSanity(String fail) { }
}

class TestStubParamConstructor {
    

    TestStubParamConstructor(String name) { }
    
    static void stubSanity(String fail) { }
}

class TestStubExplicitConstructorType {

    TestStubExplicitConstructorType()  { }
    
    static void stubSanity(String fail) { }
}


class ConstructorStubAnnotation {

    void testConstructor() {
        // Make sure that the stub files are actually working (pointing to the right class, etc).
        //:: error: (argument.type.incompatible)
        TestStubImplicitConstructor.stubSanity("test");
        //:: error: (argument.type.incompatible)
        TestStubNoParamConstructor.stubSanity("test");
        //:: error: (argument.type.incompatible)
        TestStubParamConstructor.stubSanity("test");
        //:: error: (argument.type.incompatible)
        TestStubExplicitConstructorType.stubSanity("test");
        

        //:: error: (assignment.type.incompatible) 
        @Source(READ_SMS) TestStubImplicitConstructor imp = new TestStubImplicitConstructor();

        //:: error: (assignment.type.incompatible)
        @Source(READ_SMS) TestStubNoParamConstructor noParam = new TestStubNoParamConstructor();

        //:: error: (argument.type.incompatible) :: error: (assignment.type.incompatible)
        @Source(READ_SMS) TestStubParamConstructor param = new TestStubParamConstructor("hello");

        //:: error: (assignment.type.incompatible)
        @Source(READ_SMS) TestStubExplicitConstructorType explicit = new TestStubExplicitConstructorType();

        @Source(INTERNET) TestStubExplicitConstructorType explicit2 = new TestStubExplicitConstructorType();
    }
}

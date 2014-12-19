
/*These warnings should happen*/
//warning: StubParser: Field INET_FIELD not found in type stubfile.ExampleApi
//warning: StubParser: Imported package not found: fakepackage
//warning: StubParser: Imported type not found: fakepackage.Test
//warning: StubParser: Imported type not found: fakeclass
//warning: StubParser: Enclosing type of static field Test not found: fakeclass
//warning: StubParser: Enclosing type of static field fakenestedfield not found: stubfile.ExampleApiNotExist
//warning: StubParser: Static field MISSING_FIELD is not imported
//warning: StubParser: Field MISSING_MEMEBER not found in type sparta.checkers.quals.FlowPermissionString
//warning: StubParser: Type MissingClass not found


/*These warnings are from information-flow.astub and they may change*/
//warning: StubParser: Skipping annotation type: android.annotation.TargetApi
//warning: StubParser: Skipping enum type: android.net.NetworkInfo.State
//warning: StubParser: Method idealByteArraySize(int) not found in type android.support.v4.util.LongSparseArray
//warning: StubParser: Method idealLongArraySize(int) not found in type android.support.v4.util.LongSparseArray
//warning: StubParser: Type not found: android.support.v4.print.PrintHelperKitKat
//warning: StubParser: Constructor <init>(ArrayList) not found in type android.view.ViewGroup
//warning: StubParser: Method onItemClick(AdapterView,View,int,long) not found in type android.widget.AdapterView.OnItemSelectedListener
//warning: StubParser: Type not found: com.google.android.maps.GeoPoint
//warning: StubParser: Method get() not found in type java.lang.ref.ReferenceQueue
//warning: StubParser: Skipping enum type: java.util.concurrent.TimeUnit
//warning: StubParser: Type not found: org.htmlcleaner.HtmlCleaner
//warning: StubParser: Type not found: org.htmlcleaner.TagNode


import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import stubfile.*;
import static sparta.checkers.quals.FlowPermissionString.*;

public class StubfileTests {
    @Source(READ_SMS) @Sink({}) int sms;
    void constructorTest() {
        ExampleApi api = new ExampleApi();
        ExampleApi api1 = new ExampleApi("hello");
        
        //:: error: (argument.type.incompatible) :: error: (forbidden.flow)
        ExampleApi api2 = new ExampleApi(sms);
    }

    void polyFlow() {
        ExampleApi api = new ExampleApi();
        String x = "";
        String s = "";

        api.polyFlow();
        x = api.polyFlow1();
        api.polyFlow2(s);
        x = api.polyFlow3(s);
    }

    void polyFlowR() {
        ExampleApi api = new ExampleApi();
        String x = "";
        String s = "";

        api.polyFlowR();
        x = api.polyFlowR1();
        api.polyFlowR2(s);
        x = api.polyFlowR3(s);

    }

    void reviewed() {
        ExampleApi api = new ExampleApi();
        String x = "";
        String s = "";

        api.reviewed();
        x = api.reviewed1();
        api.reviewed2(s);
        x = api.reviewed3(s);
    }
    @Source({ READ_CONTACTS }) @Sink({INTERNET})
    String s = "";
    void reviewedSom() {
        ExampleApi api = new ExampleApi();
        String x = "";


        api.reviewedSomeAnnos();
        x = api.reviewedSomeAnnos1();
        api.reviewedSomeAnnos2(s);
        //:: error: (argument.type.incompatible)
        x = api.reviewedSomeAnnos3(s);
    }
    @Source({ READ_CONTACTS }) @Sink({})
    String s2 = "";
    @Source({}) @Sink({}) ExampleApi api = new ExampleApi();
    void notReviewed() {
        String x = "";    

        //::error: (method.invocation.invalid)
        api.notReviewed();
        //::error: (forbidden.flow)
        x = api.notReviewed1();

        //:: error: (argument.type.incompatible) ::error: (method.invocation.invalid)
        api.notReviewed2(s2);
        //::error: (argument.type.incompatible) ::error: (forbidden.flow) 
        x = api.notReviewed3(s2);
    }

    void staticImports() {
        ExampleApi api = new ExampleApi();
        api.staticImport("");
    }

}

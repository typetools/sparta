
/*These warnings should happen*/
//warning: StubParser: Field INET_FIELD not found in type stubfile.ExampleApi
//warning: StubParser: Imported package not found: fakepackage
//warning: StubParser: Imported type not found: fakepackage.Test
//warning: StubParser: Imported type not found: fakeclass
//warning: StubParser: Enclosing type of static field Test not found: fakeclass
//warning: StubParser: Enclosing type of static field fakenestedfield not found: stubfile.ExampleApiNotExist
//warning: StubParser: Static field MISSING_FIELD is not imported
//warning: StubParser: Field MISSING_MEMEBER not found in type sparta.checkers.quals.FlowPermission
//warning: StubParser: Type MissingClass not found


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
//warning: StubParser: Constructor <init>(long) not found in type java.security.Timestamp
//warning: StubParser: Skipping enum type: java.util.concurrent.TimeUnit
//warning: StubParser: Type not found: org.htmlcleaner.HtmlCleaner
//warning: StubParser: Type not found: org.htmlcleaner.TagNode
//warning: StubParser: Method idealByteArraySize(int) not found in type android.support.v4.util.LongSparseArray
//warning: StubParser: Method idealLongArraySize(int) not found in type android.support.v4.util.LongSparseArray
//warning: StubParser: Type not found: android.support.v4.print.PrintHelperKitKat


import sparta.checkers.quals.Source;
import stubfile.*;
import static sparta.checkers.quals.FlowPermission.*;

public class StubfileTests {
    void constructorTest() {
        ExampleApi api = new ExampleApi();
        ExampleApi api1 = new ExampleApi("hello");
        
        //:: error: (argument.type.incompatible) :: error: (forbidden.flow)
        ExampleApi api2 = new ExampleApi(2);
    }

    void polyFlow() {
        ExampleApi api = new ExampleApi();
        String x = "";
        String s = "";

        api.polyFlow();
        //:: error: (forbidden.flow)
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

    void reviewedSom() {
        ExampleApi api = new ExampleApi();
        String x = "";
        @Source({ READ_CONTACTS, LITERAL })
        String s = "";

        api.reviewedSomeAnnos();
        x = api.reviewedSomeAnnos1();
        api.reviewedSomeAnnos2(s);
        //:: error: (argument.type.incompatible)
        x = api.reviewedSomeAnnos3(s);
    }

    void notReviewed() {
        ExampleApi api = new ExampleApi();
        String x = "";
        String s = "";

        //::error: (method.invocation.invalid)
        api.notReviewed();
        //::error: (forbidden.flow)
        x = api.notReviewed1();

        //:: error: (argument.type.incompatible) ::error: (method.invocation.invalid)
        api.notReviewed2(s);
        //::error: (argument.type.incompatible) ::error: (forbidden.flow) 
        x = api.notReviewed3(s);
    }

    void staticImports() {
        ExampleApi api = new ExampleApi();
        api.staticImport("");
    }

}

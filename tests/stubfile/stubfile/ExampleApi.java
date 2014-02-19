package stubfile;
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

@SuppressWarnings("flow")
public class ExampleApi {
    public ExampleApi(){}
    public ExampleApi(String s){}
    public ExampleApi(int i){}
	
	public void polyFlow(){}
	public String polyFlow1(){return " ";}
	public void polyFlow2(String s){}
	public String polyFlow3(String s){return "";}
	
	public void polyFlowR(){}
	public String polyFlowR1(){return "";}
	public void polyFlowR2(String s){}
	public String polyFlowR3(String s){return "";}

	public void reviewed(){}
	public String reviewed1(){return "";}
	public void reviewed2(String s){}
	public String reviewed3(String s){return "";}
	
	public void reviewedSomeAnnos(){}
	public String reviewedSomeAnnos1(){return "";}
	public void reviewedSomeAnnos2(String s){}
	public String reviewedSomeAnnos3(String s){return "";}
	
	public void notReviewed(){}
	public String notReviewed1(){return "";}
	public void notReviewed2(String s){}
	public String notReviewed3(String s){return "";}
	
	public void staticImport(String s) { }
	@interface MyAnnotation {
		String[] value();
	}
	public final String STATIC_FIELD = "Value";
}

package stubfile;
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
	 public static  class ExampleStaticApi {        
	        public static void polyFlow(){}
	        public static String polyFlow1(){return " ";}
	        public static void polyFlow2(String s){}
	        public static String polyFlow3(String s){return "";}
	        
	        public static void polyFlowR(){}
	        public static  String polyFlowR1(){return "";}
	        public static void polyFlowR2(String s){}
	        public static String polyFlowR3(String s){return "";}

	        public static void reviewed(){}
	        public static String reviewed1(){return "";}
	        public static void reviewed2(String s){}
	        public static String reviewed3(String s){return "";}
	        
	        public static void reviewedSomeAnnos(){}
	        public static String reviewedSomeAnnos1(){return "";}
	        public static void reviewedSomeAnnos2(String s){}
	        public static String reviewedSomeAnnos3(String s){return "";}
	        
	        public static void notReviewed(){}
	        public static String notReviewed1(){return "";}
	        public static void notReviewed2(String s){}
	        public static String notReviewed3(String s){return "";}
	        
	        public final static String STATIC_FIELD = "Value";
	  }
}

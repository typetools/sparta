package stubfile;

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

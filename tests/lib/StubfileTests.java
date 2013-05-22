import sparta.checkers.quals.Source;
import stubfile.*;
import static sparta.checkers.quals.FlowPermission.*;

public class StubfileTests {
//
//	void polyFlow() {
//		ExampleApi api = new ExampleApi();
//		String x = "";
//		String s = "";
//
//		api.polyFlow();
//		x = api.polyFlow1();
//		api.polyFlow2(s);
//		x = api.polyFlow3(s);
//	}
//
//	void polyFlowR() {
//		ExampleApi api = new ExampleApi();
//		String x = "";
//		String s = "";
//
//		api.polyFlowR();
//		x = api.polyFlowR1();
//		api.polyFlowR2(s);
//		x = api.polyFlowR3(s);
//
//	}
//
//	void reviewed() {
//		ExampleApi api = new ExampleApi();
//		String x = "";
//		String s = "";
//
//		api.reviewed();
//		x = api.reviewed1();
//		api.reviewed2(s);
//		x = api.reviewed3(s);
//	}
//
//	void reviewedSom() {
//		ExampleApi api = new ExampleApi();
//		String x = "";
//		@Source({ READ_CONTACTS, LITERAL })
//		String s = "";
//
//		api.reviewedSomeAnnos();
//		x = api.reviewedSomeAnnos1();
//		api.reviewedSomeAnnos2(s);
//		//:: error: (argument.type.incompatible)
//		x = api.reviewedSomeAnnos3(s);
//	}

	void notReviewed() {
		ExampleApi api = new ExampleApi();
		String x = "";
//		String s = "";
//
		api.notReviewed();
		 x = api.notReviewed1();
		 test(x);
		
//		//:: error: (argument.type.incompatible)
//		api.notReviewed2(s);
//		//:: error: (argument.type.incompatible)
//		x = api.notReviewed3(s);
	}
	void test(@Source(LITERAL) String s){
		
	}
}

import sparta.checkers.quals.Source;
import stubfile.*;
import static sparta.checkers.quals.FlowPermission.*;

public class StubfileTests {
    void constructorTest(){
        ExampleApi api = new ExampleApi();
        ExampleApi api1 = new ExampleApi("hello");
      //:: error: (argument.type.incompatible) :: error: (forbidden.flow) :: error: (forbidden.flow)
        ExampleApi api2 = new ExampleApi(2);
    }

	void polyFlow() {
		ExampleApi api = new ExampleApi();
		String x = "";
		String s = "";
		

		api.polyFlow();
		//It doesn't make sense to only have polyflow on the return.
//::error: (forbidden.flow)
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

}


import android.content.ContentValues;
import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;

class PolyTest {
    
	void polyPut() {
		ContentValues values = new ContentValues();
		@FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) double input = 2.0;
		values.put("test", input);
	}
	
	/*void stringFormat() {
		@FlowSources(FlowSource.LOCATION) double mLat = 2.0;
		@FlowSources(FlowSource.LOCATION) double mLon = 2.0;
		int ZOOM_LEVEL = 1;
		@FlowSources({}) String input = "geo:%f,%f?z=%d";
		
		@FlowSources(FlowSource.LOCATION) String result = String.format(input, mLat, mLon, ZOOM_LEVEL);
		
	}*/
	
	/*void testMerge() {
		@FlowSources(FlowSource.LOCATION) String a = "sdf";
		String b = "jkl";
		
		
		@FlowSources(FlowSource.LOCATION) double d = 2.0;
		double e = 3.9;
		
		
		
		String c = PolyTest.merge("abc", d, e, 3);
	}*/
	
	void test_allPrimitive_NoQualifiers() {
		double a = 1.0;
		double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_onePrimitive_NoQualifiers() {
		Double a = 1.0;
		double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_NoQualifiers() {
		Double a = 1.0;
		Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	void test_allPrimitive_FirstQualifiers() {
		@FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) double a = 1.0;
		double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_onePrimitive_FirstQualifiers() {
		@FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) Double a = 1.0;
		double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_FirstQualifiers() {
		@FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) Double a = 1.0;
		Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	void test_allPrimitive_SecondQualifiers() {
		double a = 1.0;
		@FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_onePrimitive_SecondQualifiers() {
		Double a = 1.0;
		@FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_SecondQualifiers() {
		Double a = 1.0;
		@FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	void test_allPrimitive_AllQualifiers() {
		@FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) double a = 1.0;
		@FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_onePrimitive_AllQualifiers() {
		@FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) Double a = 1.0;
		@FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_AllQualifiers() {
		@FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) Double a = 1.0;
		@FlowSources({FlowSource.LOCATION, FlowSource.LITERAL}) Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	static @PolyFlowSources @PolyFlowSinks String merge(@PolyFlowSources @PolyFlowSinks String one,
			@PolyFlowSources @PolyFlowSinks  Object two, @PolyFlowSources @PolyFlowSinks Object three) {
		return one;
	}
}
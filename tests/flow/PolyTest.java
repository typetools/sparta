
import android.content.ContentValues;
import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.FlowSource;

class PolyTest {
    
//ContentValues.put(..) were not annotated correctly before.	
//	void polyPut() {
//		ContentValues values = new ContentValues();
//		@Sources({FlowSource.LOCATION, FlowSource.LITERAL}) double input = 2.0;
//		values.put("test", input);
//	}
	
	/*void stringFormat() {
		@Sources(FlowSource.LOCATION) double mLat = 2.0;
		@Sources(FlowSource.LOCATION) double mLon = 2.0;
		int ZOOM_LEVEL = 1;
		@Sources({}) String input = "geo:%f,%f?z=%d";
		
		@Sources(FlowSource.LOCATION) String result = String.format(input, mLat, mLon, ZOOM_LEVEL);
		
	}*/
	
	/*void testMerge() {
		@Sources(FlowSource.LOCATION) String a = "sdf";
		String b = "jkl";
		
		
		@Sources(FlowSource.LOCATION) double d = 2.0;
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
		@Sources({FlowSource.LOCATION, FlowSource.LITERAL}) double a = 1.0;
		double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_onePrimitive_FirstQualifiers() {
		@Sources({FlowSource.LOCATION, FlowSource.LITERAL}) Double a = 1.0;
		double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_FirstQualifiers() {
		@Sources({FlowSource.LOCATION, FlowSource.LITERAL}) Double a = 1.0;
		Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	void test_allPrimitive_SecondQualifiers() {
		double a = 1.0;
		@Sources({FlowSource.LOCATION, FlowSource.LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_onePrimitive_SecondQualifiers() {
		Double a = 1.0;
		@Sources({FlowSource.LOCATION, FlowSource.LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_SecondQualifiers() {
		Double a = 1.0;
		@Sources({FlowSource.LOCATION, FlowSource.LITERAL}) Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	void test_allPrimitive_AllQualifiers() {
		@Sources({FlowSource.LOCATION, FlowSource.LITERAL}) double a = 1.0;
		@Sources({FlowSource.LOCATION, FlowSource.LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_onePrimitive_AllQualifiers() {
		@Sources({FlowSource.LOCATION, FlowSource.LITERAL}) Double a = 1.0;
		@Sources({FlowSource.LOCATION, FlowSource.LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_AllQualifiers() {
		@Sources({FlowSource.LOCATION, FlowSource.LITERAL}) Double a = 1.0;
		@Sources({FlowSource.LOCATION, FlowSource.LITERAL}) Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	static @PolySources @PolySinks String merge(@PolySources @PolySinks String one,
			@PolySources @PolySinks  Object two, @PolySources @PolySinks Object three) {
		return one;
	}
}
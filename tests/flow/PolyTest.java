
import android.content.ContentValues;
import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;

class PolyTest {
    
//ContentValues.put(..) were not annotated correctly before.	
//	void polyPut() {
//		ContentValues values = new ContentValues();
//		@Source({CoarseFlowPermission.ACCESS_FINE_LOCATION, CoarseFlowPermission.LITERAL}) double input = 2.0;
//		values.put("test", input);
//	}
	
	/*void stringFormat() {
		@Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) double mLat = 2.0;
		@Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) double mLon = 2.0;
		int ZOOM_LEVEL = 1;
		@Source({}) String input = "geo:%f,%f?z=%d";
		
		@Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) String result = String.format(input, mLat, mLon, ZOOM_LEVEL);
		
	}*/
	
	/*void testMerge() {
		@Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) String a = "sdf";
		String b = "jkl";
		
		
		@Source(CoarseFlowPermission.ACCESS_FINE_LOCATION) double d = 2.0;
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
		@Source({CoarseFlowPermission.ACCESS_FINE_LOCATION, CoarseFlowPermission.LITERAL}) double a = 1.0;
		double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_onePrimitive_FirstQualifiers() {
		@Source({CoarseFlowPermission.ACCESS_FINE_LOCATION, CoarseFlowPermission.LITERAL}) Double a = 1.0;
		double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_FirstQualifiers() {
		@Source({CoarseFlowPermission.ACCESS_FINE_LOCATION, CoarseFlowPermission.LITERAL}) Double a = 1.0;
		Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	void test_allPrimitive_SecondQualifiers() {
		double a = 1.0;
		@Source({CoarseFlowPermission.ACCESS_FINE_LOCATION, CoarseFlowPermission.LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_onePrimitive_SecondQualifiers() {
		Double a = 1.0;
		@Source({CoarseFlowPermission.ACCESS_FINE_LOCATION, CoarseFlowPermission.LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_SecondQualifiers() {
		Double a = 1.0;
		@Source({CoarseFlowPermission.ACCESS_FINE_LOCATION, CoarseFlowPermission.LITERAL}) Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	void test_allPrimitive_AllQualifiers() {
		@Source({CoarseFlowPermission.ACCESS_FINE_LOCATION, CoarseFlowPermission.LITERAL}) double a = 1.0;
		@Source({CoarseFlowPermission.ACCESS_FINE_LOCATION, CoarseFlowPermission.LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_onePrimitive_AllQualifiers() {
		@Source({CoarseFlowPermission.ACCESS_FINE_LOCATION, CoarseFlowPermission.LITERAL}) Double a = 1.0;
		@Source({CoarseFlowPermission.ACCESS_FINE_LOCATION, CoarseFlowPermission.LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_AllQualifiers() {
		@Source({CoarseFlowPermission.ACCESS_FINE_LOCATION, CoarseFlowPermission.LITERAL}) Double a = 1.0;
		@Source({CoarseFlowPermission.ACCESS_FINE_LOCATION, CoarseFlowPermission.LITERAL}) Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	static @PolySource @PolySink String merge(@PolySource @PolySink String one,
			@PolySource @PolySink  Object two, @PolySource @PolySink Object three) {
		return one;
	}
}
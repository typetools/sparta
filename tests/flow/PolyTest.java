
import android.content.ContentValues;
import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;

class PolyTest {
    
//ContentValues.put(..) were not annotated correctly before.	
//	void polyPut() {
//		ContentValues values = new ContentValues();
//		@Sources({SpartaPermission.LOCATION, SpartaPermission.LITERAL}) double input = 2.0;
//		values.put("test", input);
//	}
	
	/*void stringFormat() {
		@Sources(SpartaPermission.LOCATION) double mLat = 2.0;
		@Sources(SpartaPermission.LOCATION) double mLon = 2.0;
		int ZOOM_LEVEL = 1;
		@Sources({}) String input = "geo:%f,%f?z=%d";
		
		@Sources(SpartaPermission.LOCATION) String result = String.format(input, mLat, mLon, ZOOM_LEVEL);
		
	}*/
	
	/*void testMerge() {
		@Sources(SpartaPermission.LOCATION) String a = "sdf";
		String b = "jkl";
		
		
		@Sources(SpartaPermission.LOCATION) double d = 2.0;
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
		@Sources({SpartaPermission.LOCATION, SpartaPermission.LITERAL}) double a = 1.0;
		double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_onePrimitive_FirstQualifiers() {
		@Sources({SpartaPermission.LOCATION, SpartaPermission.LITERAL}) Double a = 1.0;
		double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_FirstQualifiers() {
		@Sources({SpartaPermission.LOCATION, SpartaPermission.LITERAL}) Double a = 1.0;
		Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	void test_allPrimitive_SecondQualifiers() {
		double a = 1.0;
		@Sources({SpartaPermission.LOCATION, SpartaPermission.LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_onePrimitive_SecondQualifiers() {
		Double a = 1.0;
		@Sources({SpartaPermission.LOCATION, SpartaPermission.LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_SecondQualifiers() {
		Double a = 1.0;
		@Sources({SpartaPermission.LOCATION, SpartaPermission.LITERAL}) Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	void test_allPrimitive_AllQualifiers() {
		@Sources({SpartaPermission.LOCATION, SpartaPermission.LITERAL}) double a = 1.0;
		@Sources({SpartaPermission.LOCATION, SpartaPermission.LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_onePrimitive_AllQualifiers() {
		@Sources({SpartaPermission.LOCATION, SpartaPermission.LITERAL}) Double a = 1.0;
		@Sources({SpartaPermission.LOCATION, SpartaPermission.LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_AllQualifiers() {
		@Sources({SpartaPermission.LOCATION, SpartaPermission.LITERAL}) Double a = 1.0;
		@Sources({SpartaPermission.LOCATION, SpartaPermission.LITERAL}) Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	static @PolySources @PolySinks String merge(@PolySources @PolySinks String one,
			@PolySources @PolySinks  Object two, @PolySources @PolySinks Object three) {
		return one;
	}
}
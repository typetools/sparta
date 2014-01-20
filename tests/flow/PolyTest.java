import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;

class PolyTest {
    
//ContentValues.put(..) were not annotated correctly before.	
//	void polyPut() {
//		ContentValues values = new ContentValues();
//		@Source({ACCESS_FINE_LOCATION, LITERAL}) double input = 2.0;
//		values.put("test", input);
//	}
	
	/*void stringFormat() {
		@Source(ACCESS_FINE_LOCATION) double mLat = 2.0;
		@Source(ACCESS_FINE_LOCATION) double mLon = 2.0;
		int ZOOM_LEVEL = 1;
		@Source({}) String input = "geo:%f,%f?z=%d";
		
		@Source(ACCESS_FINE_LOCATION) String result = String.format(input, mLat, mLon, ZOOM_LEVEL);
		
	}*/
	
	/*void testMerge() {
		@Source(ACCESS_FINE_LOCATION) String a = "sdf";
		String b = "jkl";
		
		
		@Source(ACCESS_FINE_LOCATION) double d = 2.0;
		double e = 3.9;
		
		
		
		String c = PolyTest.merge("abc", d, e, 3);
	}*/
	
	void test_allPrimitive_NoQualifiers() {
//	    //FIXME  The validiator changes an issue with auto boxing more apparent
//		double a = 1.0;
//		double b = 2.0;
//		String c = PolyTest.merge("abc", a, b);
	}
	void test_onePrimitive_NoQualifiers() {
//	     //FIXME  The validiator changes an issue with auto boxing more apparent
//		Double a = 1.0;
//		double b = 2.0;
//		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_NoQualifiers() {
		Double a = 1.0;
		Double b = 2.0;
		@Source(LITERAL) String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	void test_allPrimitive_FirstQualifiers(@Source({ACCESS_FINE_LOCATION, LITERAL}) Double a) {
		
		Double b = 2.0;
		@Source({ACCESS_FINE_LOCATION, LITERAL}) @Sink(CONDITIONAL) String c = PolyTest.merge("abc", a, b);
	}
	       void test_allPrimitive_FirstQualifiers2(@Source({ACCESS_FINE_LOCATION, LITERAL}) Double a) {
	                
	                Double b = 2.0;
	                @Source({ACCESS_FINE_LOCATION, LITERAL}) @Sink(CONDITIONAL) String c = PolyTest.merge2("abc", a, b);
	        }
	void test_onePrimitive_FirstQualifiers(@Source({ACCESS_FINE_LOCATION, LITERAL}) Double a ) {
		Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_FirstQualifiers(@Source({ACCESS_FINE_LOCATION, LITERAL}) Double a) {
		Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	void test_allPrimitive_SecondQualifiers(@Source({ACCESS_FINE_LOCATION, LITERAL}) Double b) {
		Double a = 1.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_onePrimitive_SecondQualifiers() {
		Double a = 1.0;
		@Source({ACCESS_FINE_LOCATION, LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_SecondQualifiers() {
		Double a = 1.0;
		@Source({ACCESS_FINE_LOCATION, LITERAL}) Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	void test_allPrimitive_AllQualifiers() {
		@Source({ACCESS_FINE_LOCATION, LITERAL}) double a = 1.0;
		@Source({ACCESS_FINE_LOCATION, LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_onePrimitive_AllQualifiers() {
		@Source({ACCESS_FINE_LOCATION, LITERAL}) Double a = 1.0;
		@Source({ACCESS_FINE_LOCATION, LITERAL}) double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	void test_noPrimitive_AllQualifiers() {
		@Source({ACCESS_FINE_LOCATION, LITERAL}) Double a = 1.0;
		@Source({ACCESS_FINE_LOCATION, LITERAL}) Double b = 2.0;
		String c = PolyTest.merge("abc", a, b);
	}
	
	/*--------*/
	
	static @PolySource @PolySink String merge(@PolySource @PolySink String one,
			@PolySource @PolySink  Object two, @PolySource @PolySink Object three) {
		return one;
	}
	@PolyFlow
	       static  String merge2(String one,
                        Object two, Object three) {
               return one;
       }
	

}
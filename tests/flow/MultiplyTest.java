
import android.content.ContentValues;
import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import android.util.FloatMath;

class MultiplyTest {

    @FlowSources(FlowSource.LOCATION) float raRadians;

    float x;
    @FlowSources(FlowSource.LOCATION) float y; 
    @FlowSources(FlowSource.ANY) float z;

    void test_Multiply() {
        @FlowSources(FlowSource.LOCATION) float ra = (float) 1.0;
        @FlowSources(FlowSource.LOCATION) float dec = (float) 2.0;

        float dtr = (float) 0.5;

        raRadians = ra * dtr;
        float decRadians = dec * dtr;

        //:: error: (assignment.type.incompatible)
        x = FloatMath.cos(raRadians) * FloatMath.cos(decRadians);
        y = FloatMath.sin(raRadians) * FloatMath.cos(decRadians);
        y = FloatMath.sin(y);
        z = FloatMath.sin(decRadians) * FloatMath.cos(decRadians);
    }

    void test_Cast() {
        @FlowSources(FlowSource.LOCATION) float ra = (float) 1.0;

        //:: warning: (cast.unsafe)
        raRadians = (float) ra;
    }

    void test_Unary() {
        @FlowSources(FlowSource.LOCATION) float ra = (float) 1.0;

        //:: error: (assignment.type.incompatible)
        x = -ra;

        raRadians = -ra;
    }
}
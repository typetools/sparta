
import android.content.ContentValues;
import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import android.util.FloatMath;

class MultiplyTest {

    float raRadians;
    void test_Multiply() {
        @FlowSources(FlowSource.LOCATION) float ra = (float) 1.0;
        @FlowSources(FlowSource.LOCATION) float dec = (float) 2.0;

        float dtr = (float) 0.5;

        //:: error: (assignment.type.incompatible)
        raRadians = ra * dtr;
        float decRadians = dec * dtr;

        float x = FloatMath.cos(raRadians) * FloatMath.cos(decRadians);
        float y = FloatMath.sin(raRadians) * FloatMath.cos(decRadians);
        float z = FloatMath.sin(decRadians);
    }

    void test_Cast() {
        @FlowSources(FlowSource.LOCATION) float ra = (float) 1.0;

        //:: warning: (cast.unsafe)
        raRadians = (float) ra;
    }

    void test_Unary() {
        @FlowSources(FlowSource.LOCATION) float ra = (float) 1.0;

        //:: error: (assignment.type.incompatible)
        raRadians = -ra;
    }
}
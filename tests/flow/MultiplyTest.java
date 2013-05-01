
import android.content.ContentValues;
import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.FlowSource;
import android.util.FloatMath;

class MultiplyTest {

    @Sources(FlowSource.LOCATION) float raRadians;
    @Sources({FlowSource.LITERAL, FlowSource.LOCATION}) float raLitLocRadians;

    float x;
    @Sources({FlowSource.LITERAL, FlowSource.LOCATION}) float y;
    @Sources({FlowSource.ANY}) float z;

    void test_Multiply() {
        @Sources({FlowSource.LITERAL, FlowSource.LOCATION}) float ra = (float) 1.0;
        @Sources({FlowSource.LITERAL, FlowSource.LOCATION}) float dec = 2.0f;

        float dtr = (float) 0.5;

        //:: error: (assignment.type.incompatible)
        raRadians = ra * dtr;
        raLitLocRadians = ra * dtr;

        float decRadians = dec * dtr;

        //:: error: (assignment.type.incompatible)
        x = FloatMath.cos(raRadians) * FloatMath.cos(decRadians);
        y = FloatMath.sin(raRadians) * FloatMath.cos(decRadians);
        y = FloatMath.sin(y);
        z = FloatMath.sin(decRadians) * FloatMath.cos(decRadians);
    }

    void test_Cast() {
        @Sources({FlowSource.LITERAL, FlowSource.LOCATION}) float ra = (float) 1.0;

        //:: error: (assignment.type.incompatible)
        raRadians       = (float) ra;
        raLitLocRadians = (float) ra;
    }

    void test_Unary() {
        @Sources({FlowSource.LITERAL, FlowSource.LOCATION}) float ra = (float) 1.0;

        //:: error: (assignment.type.incompatible)
        x = -ra;

        //:: error: (assignment.type.incompatible)
        raRadians = -ra;
        raLitLocRadians = -ra;
    }
}
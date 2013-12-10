
import android.content.ContentValues;
import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import android.util.FloatMath;

class MultiplyTest {

    @Source(FlowPermission.ACCESS_FINE_LOCATION) float raRadians;
    @Source({FlowPermission.LITERAL, FlowPermission.ACCESS_FINE_LOCATION}) float raLitLocRadians;

    float x;
    @Source({FlowPermission.LITERAL, FlowPermission.ACCESS_FINE_LOCATION}) float y;
    @Source({FlowPermission.ANY}) float z;

    void test_Multiply() {
        @Source({FlowPermission.LITERAL, FlowPermission.ACCESS_FINE_LOCATION}) float ra = (float) 1.0;
        @Source({FlowPermission.LITERAL, FlowPermission.ACCESS_FINE_LOCATION}) float dec = 2.0f;

        float dtr = (float) 0.5;

        //:: error: (assignment.type.incompatible)
        raRadians = ra * dtr;
        raLitLocRadians = ra * dtr;

        float decRadians = dec * dtr;

        //MASKED//:: error: (assignment.type.incompatible)
        //:: error: (forbidden.flow) :: error: (forbidden.flow)
        x = FloatMath.cos(raRadians) * FloatMath.cos(decRadians);
        y = FloatMath.sin(raRadians) * FloatMath.cos(decRadians);
        y = FloatMath.sin(y);
        z = FloatMath.sin(decRadians) * FloatMath.cos(decRadians);
    }

    void test_Cast() {
        @Source({FlowPermission.LITERAL, FlowPermission.ACCESS_FINE_LOCATION}) float ra = (float) 1.0;

        //:: error: (assignment.type.incompatible)
        raRadians       = (float) ra;
        raLitLocRadians = (float) ra;
    }

    void test_Unary() {
        @Source({FlowPermission.LITERAL, FlowPermission.ACCESS_FINE_LOCATION}) float ra = (float) 1.0;

        x = -ra;

        //:: error: (assignment.type.incompatible)
        raRadians = -ra;
        raLitLocRadians = -ra;
    }
}
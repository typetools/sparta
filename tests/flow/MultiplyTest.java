
import android.content.ContentValues;
import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;
import android.util.FloatMath;

class MultiplyTest {

    @Sources(SpartaPermission.ACCESS_FINE_LOCATION) float raRadians;
    @Sources({SpartaPermission.LITERAL, SpartaPermission.ACCESS_FINE_LOCATION}) float raLitLocRadians;

    float x;
    @Sources({SpartaPermission.LITERAL, SpartaPermission.ACCESS_FINE_LOCATION}) float y;
    @Sources({SpartaPermission.ANY}) float z;

    void test_Multiply() {
        @Sources({SpartaPermission.LITERAL, SpartaPermission.ACCESS_FINE_LOCATION}) float ra = (float) 1.0;
        @Sources({SpartaPermission.LITERAL, SpartaPermission.ACCESS_FINE_LOCATION}) float dec = 2.0f;

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
        @Sources({SpartaPermission.LITERAL, SpartaPermission.ACCESS_FINE_LOCATION}) float ra = (float) 1.0;

        //:: error: (assignment.type.incompatible)
        raRadians       = (float) ra;
        raLitLocRadians = (float) ra;
    }

    void test_Unary() {
        @Sources({SpartaPermission.LITERAL, SpartaPermission.ACCESS_FINE_LOCATION}) float ra = (float) 1.0;

        //:: error: (assignment.type.incompatible)
        x = -ra;

        //:: error: (assignment.type.incompatible)
        raRadians = -ra;
        raLitLocRadians = -ra;
    }
}
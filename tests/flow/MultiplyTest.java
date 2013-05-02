
import android.content.ContentValues;
import sparta.checkers.quals.*;
import sparta.checkers.quals.SPARTA_Permission;
import android.util.FloatMath;

class MultiplyTest {

    @Sources(SPARTA_Permission.LOCATION) float raRadians;
    @Sources({SPARTA_Permission.LITERAL, SPARTA_Permission.LOCATION}) float raLitLocRadians;

    float x;
    @Sources({SPARTA_Permission.LITERAL, SPARTA_Permission.LOCATION}) float y;
    @Sources({SPARTA_Permission.ANY}) float z;

    void test_Multiply() {
        @Sources({SPARTA_Permission.LITERAL, SPARTA_Permission.LOCATION}) float ra = (float) 1.0;
        @Sources({SPARTA_Permission.LITERAL, SPARTA_Permission.LOCATION}) float dec = 2.0f;

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
        @Sources({SPARTA_Permission.LITERAL, SPARTA_Permission.LOCATION}) float ra = (float) 1.0;

        //:: error: (assignment.type.incompatible)
        raRadians       = (float) ra;
        raLitLocRadians = (float) ra;
    }

    void test_Unary() {
        @Sources({SPARTA_Permission.LITERAL, SPARTA_Permission.LOCATION}) float ra = (float) 1.0;

        //:: error: (assignment.type.incompatible)
        x = -ra;

        //:: error: (assignment.type.incompatible)
        raRadians = -ra;
        raLitLocRadians = -ra;
    }
}
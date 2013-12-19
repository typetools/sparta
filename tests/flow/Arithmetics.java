import sparta.checkers.quals.Source;
import  sparta.checkers.quals.FlowPermission;

import sparta.checkers.quals.Sink;
import static sparta.checkers.quals.FlowPermission.*;

class Arithmetics {
    @Source({ACCELEROMETER}) @Sink({FILESYSTEM, CONDITIONAL}) int accel;

    int clean;

    public void saveAccelData(final @Source({ACCELEROMETER}) int accelFs) {

    }

    void m() {
        
        int i = 5;
        if(i > 0) {
        }

        if(i > accel) {
        }

        if(i < clean) {

        }

        clean = i;
        
        int j = accel + 2;

        //Tests LUB
        @Source({ACCELEROMETER, LITERAL}) @Sink({FILESYSTEM, CONDITIONAL})
        int x = j;
        x += 3;

        saveAccelData(accel);

        //:: error: (assignment.type.incompatible)
        clean = x;
    }
}

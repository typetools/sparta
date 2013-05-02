import sparta.checkers.quals.Sources;
import  sparta.checkers.quals.SPARTA_Permission;

import sparta.checkers.quals.Sinks;
import static sparta.checkers.quals.SPARTA_Permission.*;

class Arithmetics {
    @Sources({ACCELEROMETER}) @Sinks({FILESYSTEM, CONDITIONAL}) int accel;

    int clean;

    public void saveAccelData(final @Sources({ACCELEROMETER}) int accelFs) {

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
        @Sources({ACCELEROMETER, LITERAL}) @Sinks({FILESYSTEM, CONDITIONAL})
        int x = j;
        x += 3;

        saveAccelData(accel);

        //:: error: (assignment.type.incompatible)
        clean = x;
    }
}

import sparta.checkers.quals.Sources;
import static sparta.checkers.quals.Sources.SPARTA_Permission;

import sparta.checkers.quals.Sinks;
import static sparta.checkers.quals.Sinks.SPARTA_Permission;

class Arithmetics {
    @Sources({SPARTA_Permission.ACCELEROMETER}) @Sinks({SPARTA_Permission.FILESYSTEM, SPARTA_Permission.CONDITIONAL}) int accel;

    int clean;

    public void saveAccelData(final @Sources({SPARTA_Permission.ACCELEROMETER}) int accelFs) {

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
        @Sources({SPARTA_Permission.ACCELEROMETER, SPARTA_Permission.LITERAL}) @Sinks({SPARTA_Permission.FILESYSTEM, SPARTA_Permission.CONDITIONAL})
        int x = j;
        x += 3;

        saveAccelData(accel);

        //:: error: (assignment.type.incompatible)
        clean = x;
    }
}

import sparta.checkers.quals.Sources;
import static sparta.checkers.quals.Sources.FlowSource;

import sparta.checkers.quals.Sinks;
import static sparta.checkers.quals.Sinks.FlowSink;

class Arithmetics {
    @Sources({FlowSource.ACCELEROMETER}) @Sinks({FlowSink.FILESYSTEM, FlowSink.CONDITIONAL}) int accel;

    int clean;

    public void saveAccelData(final @Sources({FlowSource.ACCELEROMETER}) int accelFs) {

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
        @Sources({FlowSource.ACCELEROMETER, FlowSource.LITERAL}) @Sinks({FlowSink.FILESYSTEM, FlowSink.CONDITIONAL})
        int x = j;
        x += 3;

        saveAccelData(accel);

        //:: error: (assignment.type.incompatible)
        clean = x;
    }
}

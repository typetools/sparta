import sparta.checkers.quals.FlowSources;
import static sparta.checkers.quals.FlowSources.FlowSource;

import sparta.checkers.quals.FlowSinks;
import static sparta.checkers.quals.FlowSinks.FlowSink;

class Bad {
    @FlowSources({FlowSource.ACCELEROMETER}) @FlowSinks({FlowSink.FILESYSTEM, FlowSink.CONDITIONAL}) int accel;

    //:: error: (forbidden.flow)
    int clean;

    public void saveAccelData(final @FlowSources({FlowSource.ACCELEROMETER}) @FlowSinks({FlowSink.FILESYSTEM}) int accelFs) {

    }

    void m() {

        int i = 5;
        if(i > 0) {
        }

        if(i > accel) {
        }

        //:: error: (condition.flow)
        if(i < clean) {

        }

        //:: error: (assignment.type.incompatible)
        clean = i;

        int j = accel + 2;

        @FlowSources({FlowSource.ACCELEROMETER, FlowSource.LITERAL}) @FlowSinks({FlowSink.FILESYSTEM, FlowSink.CONDITIONAL})
        int x = j;
        x += 3;

        saveAccelData(accel);

        //:: error: (assignment.type.incompatible)
        clean = x;
    }
}

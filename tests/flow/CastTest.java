import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import static sparta.checkers.quals.FlowPermissionString.*;

import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;

class CastTest {
    /**
     * Only upcast are allowed
     */
    @Source(ANY) @Sink({}) int intTop;
    @Source({}) @Sink(ANY) int intBottom;

    @Source(ANY) @Sink({})  double doubleTop;
    @Source({}) @Sink(ANY) double doubleBottom;

    @Source(ACCESS_FINE_LOCATION) @Sink(INTERNET) double doubleLoc;
    @Source(ACCESS_FINE_LOCATION) @Sink(INTERNET) int intLoc;
    @Source(ANY) @Sink({}) double x;

    void castingIntsToDoubles() {
        x = (double) intTop;
        x = (double) intLoc;
        x = (double) intBottom;

        x = (@Source(ANY) @Sink({}) double) intTop;
        x = (@Source(ANY) @Sink({}) double) intLoc;
        x = (@Source(ANY) @Sink({}) double) intBottom;

        // :: warning: (cast.unsafe)
        x = (@Source({}) @Sink(ANY) double) intTop;
        // :: warning: (cast.unsafe)
        x = (@Source({}) @Sink(ANY) double) intLoc;
        x = (@Source({}) @Sink(ANY) double) intBottom;

        // :: warning: (cast.unsafe)
        x = (@Source(ACCESS_FINE_LOCATION) @Sink(INTERNET)  double) intTop;
        x = (@Source(ACCESS_FINE_LOCATION) @Sink(INTERNET)  double) intLoc;
        x = (@Source(ACCESS_FINE_LOCATION) @Sink(INTERNET)  double) intBottom;

        x = (@Source({ACCESS_FINE_LOCATION, READ_SMS}) @Sink({}) int) intLoc;
        // :: warning: (cast.unsafe)
        x = (@Source({ACCESS_FINE_LOCATION, READ_SMS}) @Sink({INTERNET, FILESYSTEM}) int) intLoc;
        // :: warning: (cast.unsafe)
        x = (@Source({}) @Sink({}) int) intLoc;
    }

    void castingIntsToInts() {
        x = (int) intTop;
        x = (int) intLoc;
        x = (int) intBottom;

        x = (@Source(ANY) @Sink({}) int) intTop;
        x = (@Source(ANY) @Sink({}) int) intLoc;
        x = (@Source(ANY) @Sink({}) int) intBottom;

        // :: warning: (cast.unsafe)
        x = (@Source({}) @Sink(ANY) int) intTop;
        // :: warning: (cast.unsafe)
        x = (@Source({}) @Sink(ANY) int) intLoc;
        x = (@Source({}) @Sink(ANY) int) intBottom;

        // :: warning: (cast.unsafe)
        x = (@Source(ACCESS_FINE_LOCATION) @Sink(INTERNET)  int) intTop;
        x = (@Source(ACCESS_FINE_LOCATION) @Sink(INTERNET)  int) intLoc;
        x = (@Source(ACCESS_FINE_LOCATION) @Sink(INTERNET)  int) intBottom;
    }

    void castingDoublesToDoubles() {
        x = (double) doubleTop;
        x = (double) doubleLoc;
        x = (double) doubleBottom;

        x = (@Source(ANY) @Sink({}) double) doubleTop;
        x = (@Source(ANY) @Sink({}) double) doubleLoc;
        x = (@Source(ANY) @Sink({}) double) doubleBottom;

        // :: warning: (cast.unsafe)
        x = (@Source({}) @Sink(ANY) double) doubleTop;
        // :: warning: (cast.unsafe)
        x = (@Source({}) @Sink(ANY) double) doubleLoc;
        x = (@Source({}) @Sink(ANY) double) doubleBottom;

        // :: warning: (cast.unsafe)
        x = (@Source(ACCESS_FINE_LOCATION) @Sink(INTERNET)  double) doubleTop;
        x = (@Source(ACCESS_FINE_LOCATION) @Sink(INTERNET)  double) doubleLoc;
        x = (@Source(ACCESS_FINE_LOCATION) @Sink(INTERNET)  double) doubleBottom;
    }

    void castingDoublesToInts() {
        x = (int) doubleTop;
        x = (int) doubleLoc;
        x = (int) doubleBottom;

        x = (@Source(ANY) @Sink({}) int) doubleTop;
        x = (@Source(ANY) @Sink({}) int) doubleLoc;
        x = (@Source(ANY) @Sink({}) int) doubleBottom;

        // :: warning: (cast.unsafe)
        x = (@Source({}) @Sink(ANY) int) doubleTop;
        // :: warning: (cast.unsafe)
        x = (@Source({}) @Sink(ANY) int) doubleLoc;
        x = (@Source({}) @Sink(ANY) int) doubleBottom;

        // :: warning: (cast.unsafe)
        x = (@Source(ACCESS_FINE_LOCATION) @Sink(INTERNET)  int) doubleTop;
        x = (@Source(ACCESS_FINE_LOCATION) @Sink(INTERNET)  int) doubleLoc;
        x = (@Source(ACCESS_FINE_LOCATION) @Sink(INTERNET)  int) doubleBottom;
    }
}

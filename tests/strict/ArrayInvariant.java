import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;

class ArrayInvariant {

    @Source(CAMERA) byte @Source(CAMERA) @Sink(INTERNET) [] flowCompleted;
    @Source(CAMERA) @Sink({FILESYSTEM, INTERNET}) byte @Source(CAMERA) @Sink(INTERNET) [] notCompleted;

    void test() {
        // General invariance test
        //:: error: (assignment.type.incompatible)
        @Source(CAMERA) @Sink(INTERNET) byte @Source(CAMERA) @Sink(INTERNET) [] invariant = notCompleted;

       // This line used to issue an error but was fixed when transitioning to better type variables
       // check version history for more information if this line is failing
       flowCompleted = notCompleted;

       // This will fail if order matters in annotation values.
       @Source(CAMERA) @Sink({FILESYSTEM, INTERNET}) byte @Source(CAMERA) @Sink({FILESYSTEM, INTERNET})[] a = null;
       @Source(CAMERA) @Sink({INTERNET, FILESYSTEM}) byte @Source(CAMERA) @Sink({FILESYSTEM, INTERNET})[] b = a;
       
       @Source(CAMERA) @Sink({INTERNET, FILESYSTEM}) byte @Source(CAMERA) @Sink({FILESYSTEM, INTERNET})[] aCorrect = null;
       @Source(CAMERA) @Sink({INTERNET, FILESYSTEM}) byte @Source(CAMERA) @Sink({FILESYSTEM, INTERNET})[] bCorrect = aCorrect;
    }
}

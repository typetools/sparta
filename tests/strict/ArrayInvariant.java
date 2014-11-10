import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermissionString.*;

class ArrayInvariant {

    @Source(CAMERA) byte @Source(CAMERA) @Sink(INTERNET) [] flowCompleted;
    @Source(CAMERA) @Sink({FILESYSTEM, INTERNET}) byte @Source(CAMERA) @Sink(INTERNET) [] notCompleted;

    void test() {
        // General invariance test
        //:: error: (assignment.type.incompatible)
        @Source(CAMERA) @Sink(INTERNET) byte @Source(CAMERA) @Sink(INTERNET) [] invariant = notCompleted;

       // This line should not error.
       // There was a bug where sparta was creating AnnotationValues with underlying+       
       // enum values instead of VariableElement, which caused the array equals check to fail.
       // There was also an annotation values equals bug in AnnotationUtil.areSame when
       // the values of an annotation had a different order.
       flowCompleted = notCompleted;

       @Source(CAMERA) @Sink({FILESYSTEM, INTERNET}) byte @Source(CAMERA) @Sink({FILESYSTEM, INTERNET})[] a = null;
       //TODO: BUG!
       // This will fail if order matters in annotation values.
       //:: error: (assignment.type.incompatible)
       @Source(CAMERA) @Sink({INTERNET, FILESYSTEM}) byte @Source(CAMERA) @Sink({FILESYSTEM, INTERNET})[] b = a;
       
       @Source(CAMERA) @Sink({INTERNET, FILESYSTEM}) byte @Source(CAMERA) @Sink({FILESYSTEM, INTERNET})[] aCorrect = null;
       @Source(CAMERA) @Sink({INTERNET, FILESYSTEM}) byte @Source(CAMERA) @Sink({FILESYSTEM, INTERNET})[] bCorrect = aCorrect;
    }
}

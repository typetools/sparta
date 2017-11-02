import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermissionString.*;

class ParameterizedSourceTest {
    @Source(RECORD_AUDIO+"(no_param_match)") Object nomic;
    @Source(ANY) Object any;
    @Source(RECORD_AUDIO+"(rec*)") Object anyRecStar;
    @Source(RECORD_AUDIO+"(rec1,rec2)") Object missingRec;
    @Source(RECORD_AUDIO+"(rec1,rec3,rec2)") Object getSound() { return null; }

    void test1() {
        // The type of the local variable is inferred to include RECORD_AUDIO
        Object o = getSound();
        // :: error: (assignment.type.incompatible)
        nomic = o;
        anyRecStar = o;
        any = o;
        // :: error: (assignment.type.incompatible)
        missingRec = getSound();
    }

    @Source({RECORD_AUDIO, CAMERA}) Object getSoundOrCam() { return getSound(); }

    @Source(ANY) Object test2() {
        Object o = getSound();
        o = getSoundOrCam();
        return o;
    }
}
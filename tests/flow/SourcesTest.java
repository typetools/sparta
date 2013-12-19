import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;

class SourceTest {
    Object nomic;
    @Source(FlowPermission.ANY) Object any;

    @Source(FlowPermission.RECORD_AUDIO) Object getSound() { return null; }

    void test1() {
        // The type of the local variable is inferred to include RECORD_AUDIO
        Object o = getSound();
        //:: error: (assignment.type.incompatible)
        nomic = o;
        any = o;
    }

    @Source({FlowPermission.RECORD_AUDIO, FlowPermission.CAMERA}) Object getSoundOrCam() { return getSound(); }

    @Source(FlowPermission.ANY) Object test2() {
        Object o = getSound();
        o = getSoundOrCam();
        return o;
    }
}
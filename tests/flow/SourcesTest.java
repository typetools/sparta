import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;

class SourcesTest {
    Object nomic;
    @Sources(SpartaPermission.ANY) Object any;

    @Sources(SpartaPermission.RECORD_AUDIO) Object getSound() { return null; }

    void test1() {
        // The type of the local variable is inferred to include RECORD_AUDIO
        Object o = getSound();
        //:: error: (assignment.type.incompatible)
        nomic = o;
        any = o;
    }

    @Sources({SpartaPermission.RECORD_AUDIO, SpartaPermission.CAMERA}) Object getSoundOrCam() { return getSound(); }

    @Sources(SpartaPermission.ANY) Object test2() {
        Object o = getSound();
        o = getSoundOrCam();
        return o;
    }
}
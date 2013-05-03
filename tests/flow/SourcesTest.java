import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;

class SourcesTest {
    Object nomic;
    @Sources(SpartaPermission.ANY) Object any;

    @Sources(SpartaPermission.MICROPHONE) Object getSound() { return null; }

    void test1() {
        // The type of the local variable is inferred to include MICROPHONE
        Object o = getSound();
        //:: error: (assignment.type.incompatible)
        nomic = o;
        any = o;
    }

    @Sources({SpartaPermission.MICROPHONE, SpartaPermission.CAMERA}) Object getSoundOrCam() { return getSound(); }

    @Sources(SpartaPermission.ANY) Object test2() {
        Object o = getSound();
        o = getSoundOrCam();
        return o;
    }
}
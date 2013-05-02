import sparta.checkers.quals.*;
import sparta.checkers.quals.SPARTA_Permission;

class SourcesTest {
    Object nomic;
    @Sources(SPARTA_Permission.ANY) Object any;

    @Sources(SPARTA_Permission.MICROPHONE) Object getSound() { return null; }

    void test1() {
        // The type of the local variable is inferred to include MICROPHONE
        Object o = getSound();
        //:: error: (assignment.type.incompatible)
        nomic = o;
        any = o;
    }

    @Sources({SPARTA_Permission.MICROPHONE, SPARTA_Permission.CAMERA}) Object getSoundOrCam() { return getSound(); }

    @Sources(SPARTA_Permission.ANY) Object test2() {
        Object o = getSound();
        o = getSoundOrCam();
        return o;
    }
}
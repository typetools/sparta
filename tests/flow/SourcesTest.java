import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.FlowSource;

class SourcesTest {
    Object nomic;
    @Sources(FlowSource.ANY) Object any;

    @Sources(FlowSource.MICROPHONE) Object getSound() { return null; }

    void test1() {
        // The type of the local variable is inferred to include MICROPHONE
        Object o = getSound();
        //:: error: (assignment.type.incompatible)
        nomic = o;
        any = o;
    }

    @Sources({FlowSource.MICROPHONE, FlowSource.CAMERA}) Object getSoundOrCam() { return getSound(); }

    @Sources(FlowSource.ANY) Object test2() {
        Object o = getSound();
        o = getSoundOrCam();
        return o;
    }
}
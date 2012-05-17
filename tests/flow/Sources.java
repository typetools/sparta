import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;

class Sources {
    Object nomic;
    @FlowSources(FlowSource.ANY) Object any;

    @FlowSources(FlowSource.MICROPHONE) Object getSound() { return null; }

    void test1() {
        // The type of the local variable is inferred to include MICROPHONE
        Object o = getSound();
        //:: error: (assignment.type.incompatible)
        nomic = o;
        any = o;
    }

    @FlowSources({FlowSource.MICROPHONE, FlowSource.CAMERA}) Object getSoundOrCam() { return getSound(); }

    @FlowSources(FlowSource.ANY) Object test2() {
        Object o = getSound();
        o = getSoundOrCam();
        return o;
    }
}
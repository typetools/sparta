import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;

class ParameterizedSourceTest {
    @Source(value={}, finesources={@FineSource(value={RECORD_AUDIO}, params={"no_param_match"})}) Object nomic;
    @Source(ANY) Object any;
    @Source(value={}, finesources={@FineSource(value={RECORD_AUDIO}, params={"rec*"})}) Object anyRecStar;
    @Source(value={}, finesources={@FineSource(value={RECORD_AUDIO}, params={"rec1", "rec2"})}) Object missingRec;
    @Source(value={}, finesources={@FineSource(value={RECORD_AUDIO}, params={"rec1", "rec3", "rec2"})}) Object getSound() { return null; }

    void test1() {
        // The type of the local variable is inferred to include RECORD_AUDIO
        Object o = getSound();
        //:: error: (assignment.type.incompatible)
        nomic = o;
        anyRecStar = o;
        any = o;
        //:: error: (assignment.type.incompatible)
        missingRec = getSound();
    }

    @Source({RECORD_AUDIO, CAMERA}) Object getSoundOrCam() { return getSound(); }

    @Source(ANY) Object test2() {
        Object o = getSound();
        o = getSoundOrCam();
        return o;
    }
}
import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;

/**
 * Example assumes a policy file that allows flows
 * from the microphone to the network and
 * {} to email, but nothing else.
 */
class PolicyTest {

    //ANY -> {}
    private @FlowSources({FlowSource.ANY}) Object anySource = new @FlowSources({FlowSource.ANY}) Object();


    private @FlowSources({FlowSource.EXTERNAL_STORAGE, FlowSource.FILESYSTEM, FlowSource.LOCATION})
    @FlowSinks({FlowSink.LOGCAT})
    Object esFsLocSource = null;

    private void anyToLogcatMeth(@FlowSources({FlowSource.ANY}) @FlowSinks({FlowSink.LOGCAT}) Object logcatSink) {
    }

    private void locToLogcatMeth(@FlowSources({FlowSource.LOCATION}) @FlowSinks({FlowSink.LOGCAT}) Object logcatSink) {
    }

    private @FlowSources({FlowSource.EXTERNAL_STORAGE, FlowSource.FILESYSTEM, FlowSource.LOCATION})
    @FlowSinks({})
    Object eflSrc = null;

    private Object noneToNone = null;

    private @FlowSources({FlowSource.PHONE_NUMBER}) Object phCaSource = null;

    private @FlowSources({FlowSource.TIME}) Object timeSrcMeth() {
        return null;
    }

    private @FlowSources({FlowSource.ANY}) @FlowSinks({FlowSink.LOGCAT}) Object anyToLogcat = null;
    private @FlowSources({FlowSource.PHONE_NUMBER, FlowSource.CAMERA}) @FlowSinks({FlowSink.LOGCAT}) Object phToLogcat = null;

    private @FlowSources({FlowSource.ACCELEROMETER, FlowSource.CAMERA, FlowSource.ACCOUNTS})
    @FlowSinks({FlowSink.DISPLAY, FlowSink.EMAIL, FlowSink.NETWORK})
    Object acaToDen = null;

    private @FlowSources({FlowSource.ACCELEROMETER, FlowSource.CAMERA, FlowSource.LOCATION})
    @FlowSinks({FlowSink.DISPLAY, FlowSink.EMAIL, FlowSink.RANDOM})
    Object aclToDer = null;

    private @FlowSources({FlowSource.ACCELEROMETER, FlowSource.CAMERA, FlowSource.LOCATION})
    @FlowSinks({FlowSink.DISPLAY, FlowSink.EMAIL, FlowSink.RANDOM})
    Object aclToDer2 = null;


    protected @FlowSources({FlowSource.ACCOUNTS}) Object accSrc = null;

    //:: error: (assignment.type.incompatible)
    protected @FlowSinks({FlowSink.DISPLAY, FlowSink.EMAIL}) Object diEmSink = accSrc;

    //:: error: (assignment.type.incompatible)
    protected @FlowSinks({FlowSink.EMAIL}) Object emSink = accSrc;

    //:: error: (assignment.type.incompatible)
    protected @FlowSinks({FlowSink.DISPLAY}) Object diSink = accSrc;


    protected String @FlowSources(FlowSource.MICROPHONE) [] getMicroData() {
        return null;
    }

    protected void sendNetworkEmail(final @FlowSinks({FlowSink.NETWORK, FlowSink.EMAIL}) Object netEmSink) {
    }

    protected void sendNetwork(final @FlowSinks({FlowSink.NETWORK}) Object netSink) {
    }

    protected void sendNetLogSink(final @FlowSinks({FlowSink.NETWORK, FlowSink.LOGCAT}) Object netLogSink) {
    }

    protected void sendNetExtSink(final @FlowSinks({FlowSink.NETWORK, FlowSink.EXTERNAL_STORAGE}) Object netExtSink) {
    }

    protected void sendNetLogTextSink(final @FlowSinks({FlowSink.NETWORK, FlowSink.LOGCAT, FlowSink.TEXTMESSAGE}) Object netExtSink) {
    }

    void test() {
        //:: error: (assignment.type.incompatible)
        aclToDer  = acaToDen;
        aclToDer2 = aclToDer;

        //:: error: (assignment.type.incompatible)
        anyToLogcat = eflSrc;
        //:: error: (assignment.type.incompatible)
        anyToLogcat = timeSrcMeth();
        //:: error: (argument.type.incompatible)
        anyToLogcatMeth(anySource);
        //:: error: (argument.type.incompatible)
        anyToLogcatMeth(noneToNone);
        //:: error: (argument.type.incompatible)
        locToLogcatMeth(anySource);

        //:: error: (assignment.type.incompatible)
        phToLogcat = phCaSource;
        //:: error: (assignment.type.incompatible)
        anyToLogcat = anySource;
        anyToLogcat = esFsLocSource;

        //:: error: (assignment.type.incompatible)
        @FlowSinks({FlowSink.FILESYSTEM}) Object fsSink = accSrc;
        //:: error: (assignment.type.incompatible)
        @FlowSinks({FlowSink.FILESYSTEM, FlowSink.DISPLAY}) Object fsDiSink = accSrc;

        //:: error: (argument.type.incompatible)
        sendNetworkEmail(getMicroData());
        //:: error: (argument.type.incompatible)
        sendNetwork(getMicroData());
        //:: error: (argument.type.incompatible)
        sendNetLogSink(getMicroData());
        //:: error: (argument.type.incompatible)
        sendNetExtSink(getMicroData());
        //:: error: (argument.type.incompatible)
        sendNetLogTextSink(getMicroData());
    }
}
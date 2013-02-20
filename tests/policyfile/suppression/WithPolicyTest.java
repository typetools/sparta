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
            @FlowSinks({FlowSink.LOG})
            Object esFsLocSource = null;

    private void anyToLogcatMeth(@FlowSources({FlowSource.ANY}) @FlowSinks({FlowSink.LOG}) Object logcatSink) {
    }

    private void locToLogcatMeth(@FlowSources({FlowSource.LOCATION}) @FlowSinks({FlowSink.LOG}) Object logcatSink) {
    }

    private @FlowSources({FlowSource.EXTERNAL_STORAGE, FlowSource.FILESYSTEM, FlowSource.LOCATION})
            @FlowSinks({})
            Object eflSrc = null;

    private Object noneToNone = null;

    private @FlowSources({FlowSource.PHONE_NUMBER}) Object phCaSource = null;

    private @FlowSources({FlowSource.TIME}) Object timeSrcMeth() {
        return null;
    }

    private @FlowSources({FlowSource.ANY}) @FlowSinks({FlowSink.LOG}) Object anyToLogcat = null;
    private @FlowSources({FlowSource.PHONE_NUMBER, FlowSource.CAMERA}) @FlowSinks({FlowSink.LOG}) Object phToLogcat = null;

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
    protected @FlowSinks({FlowSink.DISPLAY, FlowSink.EMAIL}) Object diEmSink = accSrc;       //TODO: WRONG HERE!
    protected @FlowSinks({FlowSink.EMAIL}) Object emSink = accSrc;
    protected @FlowSinks({FlowSink.DISPLAY}) Object diSink = accSrc;


    protected String @FlowSources(FlowSource.MICROPHONE) [] getMicroData() {
        return null;
    }

    protected void sendNetworkEmail(final @FlowSinks({FlowSink.NETWORK, FlowSink.EMAIL}) Object netEmSink) {
    }

    protected void sendNetwork(final @FlowSinks({FlowSink.NETWORK}) Object netSink) {
    }

    protected void sendNetLogSink(final @FlowSinks({FlowSink.NETWORK, FlowSink.LOG}) Object netLogSink) {
    }

    protected void sendNetExtSink(final @FlowSinks({FlowSink.NETWORK, FlowSink.EXTERNAL_STORAGE}) Object netExtSink) {
    }

    protected void sendNetLogTextSink(final @FlowSinks({FlowSink.NETWORK, FlowSink.LOG, FlowSink.SMS}) Object netExtSink) {
    }

    void test() {
        aclToDer  = acaToDen;
        aclToDer2 = aclToDer;

        anyToLogcat = eflSrc;
        anyToLogcat = timeSrcMeth();

        anyToLogcatMeth(anySource);
        anyToLogcatMeth(noneToNone);
        locToLogcatMeth(anySource);

        phToLogcat = phCaSource;

        //OK
        anyToLogcat = anySource;

        //OK
        anyToLogcat = esFsLocSource;

        //:: error: (forbidden.flow)
        @FlowSinks({FlowSink.FILESYSTEM}) Object fsSink = accSrc;
        //:: error: (forbidden.flow)
        @FlowSinks({FlowSink.FILESYSTEM, FlowSink.DISPLAY}) Object fsDiSink = accSrc;

        sendNetworkEmail(getMicroData());
        sendNetwork(getMicroData());
        sendNetLogSink(getMicroData());

        //:: error: (argument.type.incompatible)
        sendNetExtSink(getMicroData());
        //:: error: (argument.type.incompatible)
        sendNetLogTextSink(getMicroData());
    }
}
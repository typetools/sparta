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


//    private @FlowSources({FlowSource.EXTERNAL_STORAGE, FlowSource.FILESYSTEM, FlowSource.LOCATION})
//    @FlowSinks({FlowSink.LOG})
//    Object esFsLocSource = null;
    //:: error: (forbidden.flow) 
    private void anyToLogcatMeth(@FlowSources({FlowSource.ANY}) @FlowSinks({FlowSink.LOG}) Object logcatSink) {
    }
    //:: error: (forbidden.flow) 
    private void locToLogcatMeth(@FlowSources({FlowSource.LOCATION}) @FlowSinks({FlowSink.LOG}) Object logcatSink) {
    }

    private @FlowSources({FlowSource.EXTERNAL_STORAGE, FlowSource.FILESYSTEM, FlowSource.LOCATION})
    
    @FlowSinks({})
  //:: error: (forbidden.flow) 
    Object eflSrc = null;

    private Object noneToNone = null;

    private @FlowSources({FlowSource.PHONE_NUMBER}) Object phCaSource = null;

    private @FlowSources({FlowSource.TIME}) Object timeSrcMeth() {
        return null;
    }
    //:: error: (forbidden.flow) 
    private @FlowSources({FlowSource.ANY}) @FlowSinks({FlowSink.LOG}) Object anyToLogcat = null;
    
  //:: error: (forbidden.flow) 
    private @FlowSources({FlowSource.PHONE_NUMBER, FlowSource.CAMERA}) @FlowSinks({FlowSink.LOG}) Object phToLogcat = null;

    private @FlowSources({FlowSource.ACCELEROMETER, FlowSource.CAMERA, FlowSource.ACCOUNTS})
    @FlowSinks({FlowSink.DISPLAY, FlowSink.EMAIL, FlowSink.NETWORK})
  //:: error: (forbidden.flow) 
    Object acaToDen = null;

    private @FlowSources({FlowSource.ACCELEROMETER, FlowSource.CAMERA, FlowSource.LOCATION})
    @FlowSinks({FlowSink.DISPLAY, FlowSink.EMAIL, FlowSink.RANDOM})
  //:: error: (forbidden.flow) 
    Object aclToDer = null;

    private @FlowSources({FlowSource.ACCELEROMETER, FlowSource.CAMERA, FlowSource.LOCATION})
    @FlowSinks({FlowSink.DISPLAY, FlowSink.EMAIL, FlowSink.RANDOM})
  //:: error: (forbidden.flow) 
    Object aclToDer2 = null;

 
    protected @FlowSources({FlowSource.ACCOUNTS}) Object accSrc = null;

    //:: error: (forbidden.flow) 
    protected @FlowSinks({FlowSink.DISPLAY, FlowSink.EMAIL}) Object diEmSink = accSrc;

    //:: error: (forbidden.flow)
    protected @FlowSinks({FlowSink.EMAIL}) Object emSink = accSrc;

    //:: error: (forbidden.flow)
    protected @FlowSinks({FlowSink.DISPLAY}) Object diSink = accSrc;


    protected String @FlowSources(FlowSource.MICROPHONE) [] getMicroData() {
        return null;
    }
    //:: error: (forbidden.flow) 
    protected void sendNetworkEmail(final @FlowSinks({FlowSink.NETWORK, FlowSink.EMAIL}) Object netEmSink) {
    }
    //:: error: (forbidden.flow) 
    protected void sendNetwork(final @FlowSinks({FlowSink.NETWORK}) Object netSink) {
    }
    //:: error: (forbidden.flow) 
    protected void sendNetLogSink(final @FlowSinks({FlowSink.NETWORK, FlowSink.LOG}) Object netLogSink) {
    }
    //:: error: (forbidden.flow) 
    protected void sendNetExtSink(final @FlowSinks({FlowSink.NETWORK, FlowSink.EXTERNAL_STORAGE}) Object netExtSink) {
    }
    //:: error: (forbidden.flow) 
    protected void sendNetLogTextSink(final @FlowSinks({FlowSink.NETWORK, FlowSink.LOG, FlowSink.SMS}) Object netExtSink) {
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
        anyToLogcat = null;

        //:: error: (forbidden.flow)
        @FlowSinks({FlowSink.FILESYSTEM}) Object fsSink = accSrc;
        //:: error: (forbidden.flow)
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
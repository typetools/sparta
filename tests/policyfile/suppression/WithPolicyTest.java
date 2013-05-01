import sparta.checkers.quals.*;
import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.Sources.FlowSource;
import sparta.checkers.quals.Sinks.FlowSink;

import java.util.List;
import java.io.File;

/**
 * Example assumes a policy file that allows flows
 * from the microphone to the network and
 * {} to email, but nothing else.
 */



@Sources({FlowSource.PHONE_NUMBER}) @Sinks({FlowSink.NETWORK})
class PolicyTest {

    private @Sources({FlowSource.ANY}) Object anySource = new @Sources({FlowSource.ANY}) Object();
    private @Sources({FlowSource.EXTERNAL_STORAGE, FlowSource.FILESYSTEM, FlowSource.LOCATION})
    @Sinks({FlowSink.LOG})
//	//:: error: (forbidden.flow)
    Object esFsLocSource = null;
  //  //:: error: (forbidden.flow)
    private @Sinks({FlowSink.LOG}) Object logcatSink = null;
    ////:: error: (forbidden.flow)
    private @Sources({FlowSource.ANY}) @Sinks({FlowSink.LOG}) Object fsAny() {
        return null;
    }
    ////:: error: (forbidden.flow)
    public void timeToLogCat(final @Sources({FlowSource.TIME}) @Sinks({FlowSink.LOG}) Object obj) {
    }
 //   //:: error: (forbidden.flow)
    public void micToExt(final @Sources({FlowSource.MICROPHONE}) @Sinks({FlowSink.EXTERNAL_STORAGE}) Object obj) {
    }
 //   //:: error: (forbidden.flow)
    public @Sources({FlowSource.MICROPHONE}) @Sinks({FlowSink.EXTERNAL_STORAGE}) Object micToExt2(final @Sources({FlowSource.MICROPHONE}) @Sinks({}) Object obj) {
        return null;
    }
   //  //:: error: (forbidden.flow)
    public @Sources(FlowSource.ANY) @Sinks(FlowSink.EXTERNAL_STORAGE) double fromAny(@Sources(FlowSource.LOCATION)  @Sinks(FlowSink.EXTERNAL_STORAGE) int x) {
      //  //:: error: (forbidden.flow)
    	return (@Sources(FlowSource.ANY) @Sinks(FlowSink.EXTERNAL_STORAGE)  double) x;
    }

  //  //:: error: (forbidden.flow)
    public <T extends @Sources({FlowSource.ACCOUNTS}) @Sinks({FlowSink.NETWORK}) Object> T accToNet() { return null; }
    // //:: error: (forbidden.flow)
    public List<? extends @Sources({FlowSource.ACCOUNTS}) @Sinks({FlowSink.FILESYSTEM}) File> accFileToNet() { return null; }

    void test() {
       //  //:: error: (forbidden.flow)
        final @Sources({FlowSource.PHONE_NUMBER}) @Sinks({FlowSink.LOG}) Object obj = null;
       //  //:: error: (forbidden.flow)
        final @Sources({FlowSource.TIME}) @Sinks({FlowSink.LOG}) Object anyObj = null;
        // //:: error: (forbidden.flow)
        final @Sources({FlowSource.MICROPHONE}) @Sinks({FlowSink.LOG}) Object micToLc = null;

       // //:: error: (forbidden.flow)
        final @Sources({FlowSource.ACCOUNTS}) @Sinks({FlowSink.EMAIL}) Object accToEm = null;
       //  //:: error: (forbidden.flow)
        final @Sources({FlowSource.ACCOUNTS}) @Sinks({FlowSink.DISPLAY}) Object accToDi = null;
        // //:: error: (forbidden.flow)
        final String [] arrayOfAccToDEm = new String @Sources({FlowSource.ACCOUNTS}) @Sinks({FlowSink.DISPLAY, FlowSink.EMAIL}) []{};

        // //:: error: (forbidden.flow)
        final String [] arrayOfAccToDEmFs = new String @Sources({FlowSource.ACCOUNTS}) @Sinks({FlowSink.DISPLAY, FlowSink.EMAIL, FlowSink.FILESYSTEM}) []{};
        // //:: error: (forbidden.flow)
        final @Sources({FlowSource.ACCOUNTS}) @Sinks({FlowSink.FILESYSTEM}) Object accToFs = null;
        // //:: error: (forbidden.flow)
        final @Sources({FlowSource.ACCOUNTS}) @Sinks({FlowSink.FILESYSTEM, FlowSink.DISPLAY}) Object accToFsDi = null;


        // //:: error: (forbidden.flow)
        List<@Sources({FlowSource.MICROPHONE, FlowSource.TIME}) @Sinks({FlowSink.EMAIL, FlowSink.FILESYSTEM}) File> maTiFile = null;
        // //:: error: (forbidden.flow)
        List<@Sources({FlowSource.MICROPHONE, FlowSource.TIME}) @Sinks({FlowSink.EMAIL, FlowSink.NETWORK}) File> maTiFile2 = null;
        // //:: error: (forbidden.flow)
        List<@Sources({FlowSource.MICROPHONE}) @Sinks({FlowSink.EMAIL, FlowSink.NETWORK}) File> maFile2 = null;

        
        @Sources({FlowSource.PHONE_NUMBER}) @Sinks({FlowSink.EMAIL, FlowSink.LOG})
        class Whatever {
//:: error: receiver parameter not applicable for constructor of top-level class: @Sources(value = {FlowSource.PHONE_NUMBER}) 
            public Whatever(@Sources({FlowSource.PHONE_NUMBER}) @Sinks({FlowSink.NETWORK}) Whatever this) {

            }
        }

      /// /:: error: (forbidden.flow)
        final Object whatever = new @Sources({FlowSource.PHONE_NUMBER}) @Sinks({FlowSink.NETWORK}) Whatever();
      //// :: error: (forbidden.flow)
        final @Sources({FlowSource.MICROPHONE}) @Sinks({FlowSink.RANDOM}) Object micToRandom = null;

      ////:: error: (forbidden.flow)
        final @Sources({FlowSource.MICROPHONE}) @Sinks({FlowSink.NETWORK, FlowSink.EXTERNAL_STORAGE}) Object micToNExt = null;
      ////:: error: (forbidden.flow) 
        final @Sources({FlowSource.MICROPHONE}) @Sinks({FlowSink.NETWORK, FlowSink.LOG, FlowSink.SMS}) Object micToNetLogMsg = null;
    }

    public static void testInstantiate() {


        final Object obj = new PolicyTest();
    }
}

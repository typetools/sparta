import sparta.checkers.quals.*;
import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.Sources.SPARTA_Permission;
import sparta.checkers.quals.Sinks.SPARTA_Permission;

import java.util.List;
import java.io.File;

/**
 * Example assumes a policy file that allows flows
 * from the microphone to the network and
 * {} to email, but nothing else.
 */



@Sources({SPARTA_Permission.PHONE_NUMBER}) @Sinks({SPARTA_Permission.NETWORK})
class PolicyTest {

    private @Sources({SPARTA_Permission.ANY}) Object anySource = new @Sources({SPARTA_Permission.ANY}) Object();
    private @Sources({SPARTA_Permission.EXTERNAL_STORAGE, SPARTA_Permission.FILESYSTEM, SPARTA_Permission.LOCATION})
    @Sinks({SPARTA_Permission.LOG})
//	//:: error: (forbidden.flow)
    Object esFsLocSource = null;
  //  //:: error: (forbidden.flow)
    private @Sinks({SPARTA_Permission.LOG}) Object logcatSink = null;
    ////:: error: (forbidden.flow)
    private @Sources({SPARTA_Permission.ANY}) @Sinks({SPARTA_Permission.LOG}) Object fsAny() {
        return null;
    }
    ////:: error: (forbidden.flow)
    public void timeToLogCat(final @Sources({SPARTA_Permission.TIME}) @Sinks({SPARTA_Permission.LOG}) Object obj) {
    }
 //   //:: error: (forbidden.flow)
    public void micToExt(final @Sources({SPARTA_Permission.MICROPHONE}) @Sinks({SPARTA_Permission.EXTERNAL_STORAGE}) Object obj) {
    }
 //   //:: error: (forbidden.flow)
    public @Sources({SPARTA_Permission.MICROPHONE}) @Sinks({SPARTA_Permission.EXTERNAL_STORAGE}) Object micToExt2(final @Sources({SPARTA_Permission.MICROPHONE}) @Sinks({}) Object obj) {
        return null;
    }
   //  //:: error: (forbidden.flow)
    public @Sources(SPARTA_Permission.ANY) @Sinks(SPARTA_Permission.EXTERNAL_STORAGE) double fromAny(@Sources(SPARTA_Permission.LOCATION)  @Sinks(SPARTA_Permission.EXTERNAL_STORAGE) int x) {
      //  //:: error: (forbidden.flow)
    	return (@Sources(SPARTA_Permission.ANY) @Sinks(SPARTA_Permission.EXTERNAL_STORAGE)  double) x;
    }

  //  //:: error: (forbidden.flow)
    public <T extends @Sources({SPARTA_Permission.ACCOUNTS}) @Sinks({SPARTA_Permission.NETWORK}) Object> T accToNet() { return null; }
    // //:: error: (forbidden.flow)
    public List<? extends @Sources({SPARTA_Permission.ACCOUNTS}) @Sinks({SPARTA_Permission.FILESYSTEM}) File> accFileToNet() { return null; }

    void test() {
       //  //:: error: (forbidden.flow)
        final @Sources({SPARTA_Permission.PHONE_NUMBER}) @Sinks({SPARTA_Permission.LOG}) Object obj = null;
       //  //:: error: (forbidden.flow)
        final @Sources({SPARTA_Permission.TIME}) @Sinks({SPARTA_Permission.LOG}) Object anyObj = null;
        // //:: error: (forbidden.flow)
        final @Sources({SPARTA_Permission.MICROPHONE}) @Sinks({SPARTA_Permission.LOG}) Object micToLc = null;

       // //:: error: (forbidden.flow)
        final @Sources({SPARTA_Permission.ACCOUNTS}) @Sinks({SPARTA_Permission.EMAIL}) Object accToEm = null;
       //  //:: error: (forbidden.flow)
        final @Sources({SPARTA_Permission.ACCOUNTS}) @Sinks({SPARTA_Permission.DISPLAY}) Object accToDi = null;
        // //:: error: (forbidden.flow)
        final String [] arrayOfAccToDEm = new String @Sources({SPARTA_Permission.ACCOUNTS}) @Sinks({SPARTA_Permission.DISPLAY, SPARTA_Permission.EMAIL}) []{};

        // //:: error: (forbidden.flow)
        final String [] arrayOfAccToDEmFs = new String @Sources({SPARTA_Permission.ACCOUNTS}) @Sinks({SPARTA_Permission.DISPLAY, SPARTA_Permission.EMAIL, SPARTA_Permission.FILESYSTEM}) []{};
        // //:: error: (forbidden.flow)
        final @Sources({SPARTA_Permission.ACCOUNTS}) @Sinks({SPARTA_Permission.FILESYSTEM}) Object accToFs = null;
        // //:: error: (forbidden.flow)
        final @Sources({SPARTA_Permission.ACCOUNTS}) @Sinks({SPARTA_Permission.FILESYSTEM, SPARTA_Permission.DISPLAY}) Object accToFsDi = null;


        // //:: error: (forbidden.flow)
        List<@Sources({SPARTA_Permission.MICROPHONE, SPARTA_Permission.TIME}) @Sinks({SPARTA_Permission.EMAIL, SPARTA_Permission.FILESYSTEM}) File> maTiFile = null;
        // //:: error: (forbidden.flow)
        List<@Sources({SPARTA_Permission.MICROPHONE, SPARTA_Permission.TIME}) @Sinks({SPARTA_Permission.EMAIL, SPARTA_Permission.NETWORK}) File> maTiFile2 = null;
        // //:: error: (forbidden.flow)
        List<@Sources({SPARTA_Permission.MICROPHONE}) @Sinks({SPARTA_Permission.EMAIL, SPARTA_Permission.NETWORK}) File> maFile2 = null;

        
        @Sources({SPARTA_Permission.PHONE_NUMBER}) @Sinks({SPARTA_Permission.EMAIL, SPARTA_Permission.LOG})
        class Whatever {
//:: error: receiver parameter not applicable for constructor of top-level class: @Sources(value = {SPARTA_Permission.PHONE_NUMBER}) 
            public Whatever(@Sources({SPARTA_Permission.PHONE_NUMBER}) @Sinks({SPARTA_Permission.NETWORK}) Whatever this) {

            }
        }

      /// /:: error: (forbidden.flow)
        final Object whatever = new @Sources({SPARTA_Permission.PHONE_NUMBER}) @Sinks({SPARTA_Permission.NETWORK}) Whatever();
      //// :: error: (forbidden.flow)
        final @Sources({SPARTA_Permission.MICROPHONE}) @Sinks({SPARTA_Permission.RANDOM}) Object micToRandom = null;

      ////:: error: (forbidden.flow)
        final @Sources({SPARTA_Permission.MICROPHONE}) @Sinks({SPARTA_Permission.NETWORK, SPARTA_Permission.EXTERNAL_STORAGE}) Object micToNExt = null;
      ////:: error: (forbidden.flow) 
        final @Sources({SPARTA_Permission.MICROPHONE}) @Sinks({SPARTA_Permission.NETWORK, SPARTA_Permission.LOG, SPARTA_Permission.SMS}) Object micToNetLogMsg = null;
    }

    public static void testInstantiate() {


        final Object obj = new PolicyTest();
    }
}

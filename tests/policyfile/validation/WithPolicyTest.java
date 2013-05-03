import sparta.checkers.quals.*;
import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.SpartaPermission;
import sparta.checkers.quals.SpartaPermission;

import java.util.List;
import java.io.File;

/**
 * Example assumes a policy file that allows flows
 * from the microphone to the network and
 * {} to email, but nothing else.
 */



@Sources({SpartaPermission.PHONE_NUMBER}) @Sinks({SpartaPermission.INTERNET})
class PolicyTest {

    private @Sources({SpartaPermission.ANY}) Object anySource = new @Sources({SpartaPermission.ANY}) Object();
    private @Sources({SpartaPermission.EXTERNAL_STORAGE, SpartaPermission.FILESYSTEM, SpartaPermission.LOCATION})
    @Sinks({SpartaPermission.LOG})
//	//:: error: (forbidden.flow)
    Object esFsLocSource = null;
  //  //:: error: (forbidden.flow)
    private @Sinks({SpartaPermission.LOG}) Object logcatSink = null;
    ////:: error: (forbidden.flow)
    private @Sources({SpartaPermission.ANY}) @Sinks({SpartaPermission.LOG}) Object fsAny() {
        return null;
    }
    ////:: error: (forbidden.flow)
    public void timeToLogCat(final @Sources({SpartaPermission.TIME}) @Sinks({SpartaPermission.LOG}) Object obj) {
    }
 //   //:: error: (forbidden.flow)
    public void micToExt(final @Sources({SpartaPermission.MICROPHONE}) @Sinks({SpartaPermission.EXTERNAL_STORAGE}) Object obj) {
    }
 //   //:: error: (forbidden.flow)
    public @Sources({SpartaPermission.MICROPHONE}) @Sinks({SpartaPermission.EXTERNAL_STORAGE}) Object micToExt2(final @Sources({SpartaPermission.MICROPHONE}) @Sinks({}) Object obj) {
        return null;
    }
   //  //:: error: (forbidden.flow)
    public @Sources(SpartaPermission.ANY) @Sinks(SpartaPermission.EXTERNAL_STORAGE) double fromAny(@Sources(SpartaPermission.LOCATION)  @Sinks(SpartaPermission.EXTERNAL_STORAGE) int x) {
      //  //:: error: (forbidden.flow)
    	return (@Sources(SpartaPermission.ANY) @Sinks(SpartaPermission.EXTERNAL_STORAGE)  double) x;
    }

  //  //:: error: (forbidden.flow)
    public <T extends @Sources({SpartaPermission.ACCOUNTS}) @Sinks({SpartaPermission.INTERNET}) Object> T accToNet() { return null; }
    // //:: error: (forbidden.flow)
    public List<? extends @Sources({SpartaPermission.ACCOUNTS}) @Sinks({SpartaPermission.FILESYSTEM}) File> accFileToNet() { return null; }

    void test() {
       //  //:: error: (forbidden.flow)
        final @Sources({SpartaPermission.PHONE_NUMBER}) @Sinks({SpartaPermission.LOG}) Object obj = null;
       //  //:: error: (forbidden.flow)
        final @Sources({SpartaPermission.TIME}) @Sinks({SpartaPermission.LOG}) Object anyObj = null;
        // //:: error: (forbidden.flow)
        final @Sources({SpartaPermission.MICROPHONE}) @Sinks({SpartaPermission.LOG}) Object micToLc = null;

       // //:: error: (forbidden.flow)
        final @Sources({SpartaPermission.ACCOUNTS}) @Sinks({SpartaPermission.EMAIL}) Object accToEm = null;
       //  //:: error: (forbidden.flow)
        final @Sources({SpartaPermission.ACCOUNTS}) @Sinks({SpartaPermission.DISPLAY}) Object accToDi = null;
        // //:: error: (forbidden.flow)
        final String [] arrayOfAccToDEm = new String @Sources({SpartaPermission.ACCOUNTS}) @Sinks({SpartaPermission.DISPLAY, SpartaPermission.EMAIL}) []{};

        // //:: error: (forbidden.flow)
        final String [] arrayOfAccToDEmFs = new String @Sources({SpartaPermission.ACCOUNTS}) @Sinks({SpartaPermission.DISPLAY, SpartaPermission.EMAIL, SpartaPermission.FILESYSTEM}) []{};
        // //:: error: (forbidden.flow)
        final @Sources({SpartaPermission.ACCOUNTS}) @Sinks({SpartaPermission.FILESYSTEM}) Object accToFs = null;
        // //:: error: (forbidden.flow)
        final @Sources({SpartaPermission.ACCOUNTS}) @Sinks({SpartaPermission.FILESYSTEM, SpartaPermission.DISPLAY}) Object accToFsDi = null;


        // //:: error: (forbidden.flow)
        List<@Sources({SpartaPermission.MICROPHONE, SpartaPermission.TIME}) @Sinks({SpartaPermission.EMAIL, SpartaPermission.FILESYSTEM}) File> maTiFile = null;
        // //:: error: (forbidden.flow)
        List<@Sources({SpartaPermission.MICROPHONE, SpartaPermission.TIME}) @Sinks({SpartaPermission.EMAIL, SpartaPermission.INTERNET}) File> maTiFile2 = null;
        // //:: error: (forbidden.flow)
        List<@Sources({SpartaPermission.MICROPHONE}) @Sinks({SpartaPermission.EMAIL, SpartaPermission.INTERNET}) File> maFile2 = null;

        
        @Sources({SpartaPermission.PHONE_NUMBER}) @Sinks({SpartaPermission.EMAIL, SpartaPermission.LOG})
        class Whatever {
//:: error: receiver parameter not applicable for constructor of top-level class: @Sources(value = {SpartaPermission.PHONE_NUMBER}) 
            public Whatever(@Sources({SpartaPermission.PHONE_NUMBER}) @Sinks({SpartaPermission.INTERNET}) Whatever this) {

            }
        }

      /// /:: error: (forbidden.flow)
        final Object whatever = new @Sources({SpartaPermission.PHONE_NUMBER}) @Sinks({SpartaPermission.INTERNET}) Whatever();
      //// :: error: (forbidden.flow)
        final @Sources({SpartaPermission.MICROPHONE}) @Sinks({SpartaPermission.RANDOM}) Object micToRandom = null;

      ////:: error: (forbidden.flow)
        final @Sources({SpartaPermission.MICROPHONE}) @Sinks({SpartaPermission.INTERNET, SpartaPermission.EXTERNAL_STORAGE}) Object micToNExt = null;
      ////:: error: (forbidden.flow) 
        final @Sources({SpartaPermission.MICROPHONE}) @Sinks({SpartaPermission.INTERNET, SpartaPermission.LOG, SpartaPermission.SMS}) Object micToNetLogMsg = null;
    }

    public static void testInstantiate() {


        final Object obj = new PolicyTest();
    }
}

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
    private @Sources({SpartaPermission.READ_EXTERNAL_STORAGE, SpartaPermission.FILESYSTEM, SpartaPermission.ACCESS_FINE_LOCATION})
    @Sinks({SpartaPermission.WRITE_LOGS})
//	//:: error: (forbidden.flow)
    Object esFsLocSource = null;
  //  //:: error: (forbidden.flow)
    private @Sinks({SpartaPermission.WRITE_LOGS}) Object logcatSink = null;
    ////:: error: (forbidden.flow)
    private @Sources({SpartaPermission.ANY}) @Sinks({SpartaPermission.WRITE_LOGS}) Object fsAny() {
        return null;
    }
    ////:: error: (forbidden.flow)
    public void timeToLogCat(final @Sources({SpartaPermission.READ_TIME}) @Sinks({SpartaPermission.WRITE_LOGS}) Object obj) {
    }
 //   //:: error: (forbidden.flow)
    public void micToExt(final @Sources({SpartaPermission.RECORD_AUDIO}) @Sinks({SpartaPermission.WRITE_EXTERNAL_STORAGE}) Object obj) {
    }
 //   //:: error: (forbidden.flow)
    public @Sources({SpartaPermission.RECORD_AUDIO}) @Sinks({SpartaPermission.WRITE_EXTERNAL_STORAGE}) Object micToExt2(final @Sources({SpartaPermission.RECORD_AUDIO}) @Sinks({}) Object obj) {
        return null;
    }
   //  //:: error: (forbidden.flow)
    public @Sources(SpartaPermission.ANY) @Sinks(SpartaPermission.WRITE_EXTERNAL_STORAGE) double fromAny(@Sources(SpartaPermission.ACCESS_FINE_LOCATION)  @Sinks(SpartaPermission.WRITE_EXTERNAL_STORAGE) int x) {
      //  //:: error: (forbidden.flow)
    	return (@Sources(SpartaPermission.ANY) @Sinks(SpartaPermission.WRITE_EXTERNAL_STORAGE)  double) x;
    }

  //  //:: error: (forbidden.flow)
    public <T extends @Sources({SpartaPermission.MANAGE_ACCOUNTS}) @Sinks({SpartaPermission.INTERNET}) Object> T accToNet() { return null; }
    // //:: error: (forbidden.flow)
    public List<? extends @Sources({SpartaPermission.MANAGE_ACCOUNTS}) @Sinks({SpartaPermission.FILESYSTEM}) File> accFileToNet() { return null; }

    void test() {
       //  //:: error: (forbidden.flow)
        final @Sources({SpartaPermission.PHONE_NUMBER}) @Sinks({SpartaPermission.WRITE_LOGS}) Object obj = null;
       //  //:: error: (forbidden.flow)
        final @Sources({SpartaPermission.READ_TIME}) @Sinks({SpartaPermission.WRITE_LOGS}) Object anyObj = null;
        // //:: error: (forbidden.flow)
        final @Sources({SpartaPermission.RECORD_AUDIO}) @Sinks({SpartaPermission.WRITE_LOGS}) Object micToLc = null;

       // //:: error: (forbidden.flow)
        final @Sources({SpartaPermission.MANAGE_ACCOUNTS}) @Sinks({SpartaPermission.EMAIL}) Object accToEm = null;
       //  //:: error: (forbidden.flow)
        final @Sources({SpartaPermission.MANAGE_ACCOUNTS}) @Sinks({SpartaPermission.DISPLAY}) Object accToDi = null;
        // //:: error: (forbidden.flow)
        final String [] arrayOfAccToDEm = new String @Sources({SpartaPermission.MANAGE_ACCOUNTS}) @Sinks({SpartaPermission.DISPLAY, SpartaPermission.EMAIL}) []{};

        // //:: error: (forbidden.flow)
        final String [] arrayOfAccToDEmFs = new String @Sources({SpartaPermission.MANAGE_ACCOUNTS}) @Sinks({SpartaPermission.DISPLAY, SpartaPermission.EMAIL, SpartaPermission.FILESYSTEM}) []{};
        // //:: error: (forbidden.flow)
        final @Sources({SpartaPermission.MANAGE_ACCOUNTS}) @Sinks({SpartaPermission.FILESYSTEM}) Object accToFs = null;
        // //:: error: (forbidden.flow)
        final @Sources({SpartaPermission.MANAGE_ACCOUNTS}) @Sinks({SpartaPermission.FILESYSTEM, SpartaPermission.DISPLAY}) Object accToFsDi = null;


        // //:: error: (forbidden.flow)
        List<@Sources({SpartaPermission.RECORD_AUDIO, SpartaPermission.READ_TIME}) @Sinks({SpartaPermission.EMAIL, SpartaPermission.FILESYSTEM}) File> maTiFile = null;
        // //:: error: (forbidden.flow)
        List<@Sources({SpartaPermission.RECORD_AUDIO, SpartaPermission.READ_TIME}) @Sinks({SpartaPermission.EMAIL, SpartaPermission.INTERNET}) File> maTiFile2 = null;
        // //:: error: (forbidden.flow)
        List<@Sources({SpartaPermission.RECORD_AUDIO}) @Sinks({SpartaPermission.EMAIL, SpartaPermission.INTERNET}) File> maFile2 = null;

        
        @Sources({SpartaPermission.PHONE_NUMBER}) @Sinks({SpartaPermission.EMAIL, SpartaPermission.WRITE_LOGS})
        class Whatever {
//:: error: receiver parameter not applicable for constructor of top-level class: @Sources(value = {SpartaPermission.PHONE_NUMBER}) 
            public Whatever(@Sources({SpartaPermission.PHONE_NUMBER}) @Sinks({SpartaPermission.INTERNET}) Whatever this) {

            }
        }

      /// /:: error: (forbidden.flow)
        final Object whatever = new @Sources({SpartaPermission.PHONE_NUMBER}) @Sinks({SpartaPermission.INTERNET}) Whatever();
      //// :: error: (forbidden.flow)
        final @Sources({SpartaPermission.RECORD_AUDIO}) @Sinks({SpartaPermission.RANDOM}) Object micToRandom = null;

      ////:: error: (forbidden.flow)
        final @Sources({SpartaPermission.RECORD_AUDIO}) @Sinks({SpartaPermission.INTERNET, SpartaPermission.WRITE_EXTERNAL_STORAGE}) Object micToNExt = null;
      ////:: error: (forbidden.flow) 
        final @Sources({SpartaPermission.RECORD_AUDIO}) @Sinks({SpartaPermission.INTERNET, SpartaPermission.WRITE_LOGS, SpartaPermission.WRITE_SMS}) Object micToNetLogMsg = null;
    }

    public static void testInstantiate() {


        final Object obj = new PolicyTest();
    }
}

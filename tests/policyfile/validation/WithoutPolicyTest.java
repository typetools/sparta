import sparta.checkers.quals.*;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.FlowPermission;

import java.util.List;
import java.io.File;

/**
 * Example assumes a policy file that allows flows
 * from the microphone to the network and
 * {} to email, but nothing else.
 */



@Source({FlowPermission.PHONE_NUMBER}) @Sink({FlowPermission.INTERNET})
class PolicyTest {

    private @Source({FlowPermission.ANY}) Object anySource = new @Source({FlowPermission.ANY}) Object();
    private @Source({FlowPermission.READ_EXTERNAL_STORAGE, FlowPermission.FILESYSTEM, FlowPermission.ACCESS_FINE_LOCATION})
    @Sink({FlowPermission.WRITE_LOGS})
//	//:: error: (forbidden.flow)
    Object esFsLocSource = null;
  //  //:: error: (forbidden.flow)
    private @Sink({FlowPermission.WRITE_LOGS}) Object logcatSink = null;
    ////:: error: (forbidden.flow)
    private @Source({FlowPermission.ANY}) @Sink({FlowPermission.WRITE_LOGS}) Object fsAny() {
        return null;
    }
    ////:: error: (forbidden.flow)
    public void timeToLogCat(final @Source({FlowPermission.READ_TIME}) @Sink({FlowPermission.WRITE_LOGS}) Object obj) {
    }
 //   //:: error: (forbidden.flow)
    public void micToExt(final @Source({FlowPermission.RECORD_AUDIO}) @Sink({FlowPermission.WRITE_EXTERNAL_STORAGE}) Object obj) {
    }
 //   //:: error: (forbidden.flow)
    public @Source({FlowPermission.RECORD_AUDIO}) @Sink({FlowPermission.WRITE_EXTERNAL_STORAGE}) Object micToExt2(final @Source({FlowPermission.RECORD_AUDIO}) @Sink({}) Object obj) {
        return null;
    }
   //  //:: error: (forbidden.flow)
    public @Source(FlowPermission.ANY) @Sink(FlowPermission.WRITE_EXTERNAL_STORAGE) double fromAny(@Source(FlowPermission.ACCESS_FINE_LOCATION)  @Sink(FlowPermission.WRITE_EXTERNAL_STORAGE) int x) {
      //  //:: error: (forbidden.flow)
    	return (@Source(FlowPermission.ANY) @Sink(FlowPermission.WRITE_EXTERNAL_STORAGE)  double) x;
    }

  //  //:: error: (forbidden.flow)
    public <T extends @Source({FlowPermission.MANAGE_ACCOUNTS}) @Sink({FlowPermission.INTERNET}) Object> T accToNet() { return null; }
    // //:: error: (forbidden.flow)
    public List<? extends @Source({FlowPermission.MANAGE_ACCOUNTS}) @Sink({FlowPermission.FILESYSTEM}) File> accFileToNet() { return null; }

    void test() {
       //  //:: error: (forbidden.flow)
        final @Source({FlowPermission.PHONE_NUMBER}) @Sink({FlowPermission.WRITE_LOGS}) Object obj = null;
       //  //:: error: (forbidden.flow)
        final @Source({FlowPermission.READ_TIME}) @Sink({FlowPermission.WRITE_LOGS}) Object anyObj = null;
        // //:: error: (forbidden.flow)
        final @Source({FlowPermission.RECORD_AUDIO}) @Sink({FlowPermission.WRITE_LOGS}) Object micToLc = null;

       // //:: error: (forbidden.flow)
        final @Source({FlowPermission.MANAGE_ACCOUNTS}) @Sink({FlowPermission.EMAIL}) Object accToEm = null;
       //  //:: error: (forbidden.flow)
        final @Source({FlowPermission.MANAGE_ACCOUNTS}) @Sink({FlowPermission.DISPLAY}) Object accToDi = null;
        // //:: error: (forbidden.flow)
        final String [] arrayOfAccToDEm = new String @Source({FlowPermission.MANAGE_ACCOUNTS}) @Sink({FlowPermission.DISPLAY, FlowPermission.EMAIL}) []{};

        // //:: error: (forbidden.flow)
        final String [] arrayOfAccToDEmFs = new String @Source({FlowPermission.MANAGE_ACCOUNTS}) @Sink({FlowPermission.DISPLAY, FlowPermission.EMAIL, FlowPermission.FILESYSTEM}) []{};
        // //:: error: (forbidden.flow)
        final @Source({FlowPermission.MANAGE_ACCOUNTS}) @Sink({FlowPermission.FILESYSTEM}) Object accToFs = null;
        // //:: error: (forbidden.flow)
        final @Source({FlowPermission.MANAGE_ACCOUNTS}) @Sink({FlowPermission.FILESYSTEM, FlowPermission.DISPLAY}) Object accToFsDi = null;


        // //:: error: (forbidden.flow)
        List<@Source({FlowPermission.RECORD_AUDIO, FlowPermission.READ_TIME}) @Sink({FlowPermission.EMAIL, FlowPermission.FILESYSTEM}) File> maTiFile = null;
        // //:: error: (forbidden.flow)
        List<@Source({FlowPermission.RECORD_AUDIO, FlowPermission.READ_TIME}) @Sink({FlowPermission.EMAIL, FlowPermission.INTERNET}) File> maTiFile2 = null;
        // //:: error: (forbidden.flow)
        List<@Source({FlowPermission.RECORD_AUDIO}) @Sink({FlowPermission.EMAIL, FlowPermission.INTERNET}) File> maFile2 = null;

        
        @Source({FlowPermission.PHONE_NUMBER}) @Sink({FlowPermission.EMAIL, FlowPermission.WRITE_LOGS})
        class Whatever {
//:: error: receiver parameter not applicable for constructor of top-level class
            public Whatever(@Source({FlowPermission.PHONE_NUMBER}) @Sink({FlowPermission.INTERNET}) Whatever this) {

            }
        }

      /// /:: error: (forbidden.flow)
        final Object whatever = new @Source({FlowPermission.PHONE_NUMBER}) @Sink({FlowPermission.INTERNET}) Whatever();
      //// :: error: (forbidden.flow)
        final @Source({FlowPermission.RECORD_AUDIO}) @Sink({FlowPermission.RANDOM}) Object micToRandom = null;

      ////:: error: (forbidden.flow)
        final @Source({FlowPermission.RECORD_AUDIO}) @Sink({FlowPermission.INTERNET, FlowPermission.WRITE_EXTERNAL_STORAGE}) Object micToNExt = null;
      ////:: error: (forbidden.flow) 
        final @Source({FlowPermission.RECORD_AUDIO}) @Sink({FlowPermission.INTERNET, FlowPermission.WRITE_LOGS, FlowPermission.WRITE_SMS}) Object micToNetLogMsg = null;
    }

    public static void testInstantiate() {


        final Object obj = new PolicyTest();
    }
}

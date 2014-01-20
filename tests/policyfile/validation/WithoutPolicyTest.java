import sparta.checkers.quals.*;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.ParameterizedFlowPermission;
import static sparta.checkers.quals.FlowPermission.*;

import java.util.List;
import java.io.File;

/**
 * Example assumes a policy file that allows flows
 * from the microphone to the network and
 * {} to email, but nothing else.
 */



@Source({PHONE_NUMBER}) @Sink({INTERNET})
class PolicyTest {

    private @Source({ANY}) Object anySource = new @Source({ANY}) Object();
    private @Source({READ_EXTERNAL_STORAGE, FILESYSTEM, ACCESS_FINE_LOCATION})
    @Sink({WRITE_LOGS})
//	//:: error: (forbidden.flow)
    Object esFsLocSource = null;
  //  //:: error: (forbidden.flow)
    private @Sink({WRITE_LOGS}) Object logcatSink = null;
    ////:: error: (forbidden.flow)
    private @Source({ANY}) @Sink({WRITE_LOGS}) Object fsAny() {
        return null;
    }
    ////:: error: (forbidden.flow)
    public void timeToLogCat(final @Source({READ_TIME}) @Sink({WRITE_LOGS}) Object obj) {
    }
 //   //:: error: (forbidden.flow)
    public void micToExt(final @Source({RECORD_AUDIO}) @Sink({WRITE_EXTERNAL_STORAGE}) Object obj) {
    }
 //   //:: error: (forbidden.flow)
    public @Source({RECORD_AUDIO}) @Sink({WRITE_EXTERNAL_STORAGE}) Object micToExt2(final @Source({RECORD_AUDIO}) @Sink({}) Object obj) {
        return null;
    }
   //  //:: error: (forbidden.flow)
    public @Source(ANY) @Sink(WRITE_EXTERNAL_STORAGE) double fromAny(@Source(ACCESS_FINE_LOCATION)  @Sink(WRITE_EXTERNAL_STORAGE) int x) {
      //  //:: error: (forbidden.flow)
    	return (@Source(ANY) @Sink(WRITE_EXTERNAL_STORAGE)  double) x;
    }

  //  //:: error: (forbidden.flow)
    public <T extends @Source({MANAGE_ACCOUNTS}) @Sink({INTERNET}) Object> T accToNet() { return null; }
    // //:: error: (forbidden.flow)
    public List<? extends @Source({MANAGE_ACCOUNTS}) @Sink({FILESYSTEM}) File> accFileToNet() { return null; }

    void test() {
       //  //:: error: (forbidden.flow)
        final @Source({PHONE_NUMBER}) @Sink({WRITE_LOGS}) Object obj = null;
       //  //:: error: (forbidden.flow)
        final @Source({READ_TIME}) @Sink({WRITE_LOGS}) Object anyObj = null;
        // //:: error: (forbidden.flow)
        final @Source({RECORD_AUDIO}) @Sink({WRITE_LOGS}) Object micToLc = null;

       // //:: error: (forbidden.flow)
        final @Source({MANAGE_ACCOUNTS}) @Sink({WRITE_EMAIL}) Object accToEm = null;
       //  //:: error: (forbidden.flow)
        final @Source({MANAGE_ACCOUNTS}) @Sink({DISPLAY}) Object accToDi = null;
        // //:: error: (forbidden.flow)
        final String [] arrayOfAccToDEm = new String @Source({MANAGE_ACCOUNTS}) @Sink({DISPLAY, WRITE_EMAIL}) []{};

        // //:: error: (forbidden.flow)
        final String [] arrayOfAccToDEmFs = new String @Source({MANAGE_ACCOUNTS}) @Sink({DISPLAY, WRITE_EMAIL, FILESYSTEM}) []{};
        // //:: error: (forbidden.flow)
        final @Source({MANAGE_ACCOUNTS}) @Sink({FILESYSTEM}) Object accToFs = null;
        // //:: error: (forbidden.flow)
        final @Source({MANAGE_ACCOUNTS}) @Sink({FILESYSTEM, DISPLAY}) Object accToFsDi = null;


        // //:: error: (forbidden.flow)
        List<@Source({RECORD_AUDIO, READ_TIME}) @Sink({WRITE_EMAIL, FILESYSTEM}) File> maTiFile = null;
        // //:: error: (forbidden.flow)
        List<@Source({RECORD_AUDIO, READ_TIME}) @Sink({WRITE_EMAIL, INTERNET}) File> maTiFile2 = null;
        // //:: error: (forbidden.flow)
        List<@Source({RECORD_AUDIO}) @Sink({WRITE_EMAIL, INTERNET}) File> maFile2 = null;

        
        @Source({PHONE_NUMBER}) @Sink({WRITE_EMAIL, WRITE_LOGS})
        class Whatever {
//:: error: receiver parameter not applicable for constructor of top-level class
            public Whatever(@Source({PHONE_NUMBER}) @Sink({INTERNET}) Whatever this) {

            }
        }

      /// /:: error: (forbidden.flow)
        final Object whatever = new @Source({PHONE_NUMBER}) @Sink({INTERNET}) Whatever();
      //// :: error: (forbidden.flow)
        final @Source({RECORD_AUDIO}) @Sink({RANDOM}) Object micToRandom = null;

      ////:: error: (forbidden.flow)
        final @Source({RECORD_AUDIO}) @Sink({INTERNET, WRITE_EXTERNAL_STORAGE}) Object micToNExt = null;
      ////:: error: (forbidden.flow) 
        final @Source({RECORD_AUDIO}) @Sink({INTERNET, WRITE_LOGS, WRITE_SMS}) Object micToNetLogMsg = null;
    }

    public static void testInstantiate() {


        final Object obj = new PolicyTest();
    }
}

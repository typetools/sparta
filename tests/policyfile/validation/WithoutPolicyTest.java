import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSinks;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;

import java.util.List;
import java.io.File;

/**
 * Example assumes a policy file that allows flows
 * from the microphone to the network and
 * {} to email, but nothing else.
 */



@FlowSources({FlowSource.PHONE_NUMBER}) @FlowSinks({FlowSink.NETWORK})
class PolicyTest {

    private @FlowSources({FlowSource.ANY}) Object anySource = new @FlowSources({FlowSource.ANY}) Object();
    private @FlowSources({FlowSource.EXTERNAL_STORAGE, FlowSource.FILESYSTEM, FlowSource.LOCATION})
    @FlowSinks({FlowSink.LOGCAT})
    Object esFsLocSource = null;

    private @FlowSinks({FlowSink.LOGCAT}) Object logcatSink = null;

    private @FlowSources({FlowSource.ANY}) @FlowSinks({FlowSink.LOGCAT}) Object fsAny() {
        return null;
    }

    public void timeToLogCat(final @FlowSources({FlowSource.TIME}) @FlowSinks({FlowSink.LOGCAT}) Object obj) {
    }

    public void micToExt(final @FlowSources({FlowSource.MICROPHONE}) @FlowSinks({FlowSink.EXTERNAL_STORAGE}) Object obj) {
    }

    public @FlowSources({FlowSource.MICROPHONE}) @FlowSinks({FlowSink.EXTERNAL_STORAGE}) Object micToExt2(final @FlowSources({FlowSource.MICROPHONE}) @FlowSinks({}) Object obj) {
        return null;
    }

    public @FlowSources(FlowSource.ANY) @FlowSinks(FlowSink.EXTERNAL_STORAGE) double fromAny(@FlowSources(FlowSource.LOCATION)  @FlowSinks(FlowSink.EXTERNAL_STORAGE) int x) {
        return (@FlowSources(FlowSource.ANY) @FlowSinks(FlowSink.EXTERNAL_STORAGE)  double) x;
    }


    public <T extends @FlowSources({FlowSource.ACCOUNTS}) @FlowSinks({FlowSink.NETWORK}) Object> T accToNet() { return null; }

    public List<? extends @FlowSources({FlowSource.ACCOUNTS}) @FlowSinks({FlowSink.FILESYSTEM}) File> accFileToNet() { return null; }

    void test() {
        final @FlowSources({FlowSource.PHONE_NUMBER}) @FlowSinks({FlowSink.LOGCAT}) Object obj = null;
        final @FlowSources({FlowSource.TIME}) @FlowSinks({FlowSink.LOGCAT}) Object anyObj = null;
        final @FlowSources({FlowSource.MICROPHONE}) @FlowSinks({FlowSink.LOGCAT}) Object micToLc = null;


        final @FlowSources({FlowSource.ACCOUNTS}) @FlowSinks({FlowSink.EMAIL}) Object accToEm = null;
        final @FlowSources({FlowSource.ACCOUNTS}) @FlowSinks({FlowSink.DISPLAY}) Object accToDi = null;
        final String [] arrayOfAccToDEm = new String @FlowSources({FlowSource.ACCOUNTS}) @FlowSinks({FlowSink.DISPLAY, FlowSink.EMAIL}) []{};


        final String [] arrayOfAccToDEmFs = new String @FlowSources({FlowSource.ACCOUNTS}) @FlowSinks({FlowSink.DISPLAY, FlowSink.EMAIL, FlowSink.FILESYSTEM}) []{};

        final @FlowSources({FlowSource.ACCOUNTS}) @FlowSinks({FlowSink.FILESYSTEM}) Object accToFs = null;

        final @FlowSources({FlowSource.ACCOUNTS}) @FlowSinks({FlowSink.FILESYSTEM, FlowSink.DISPLAY}) Object accToFsDi = null;



        List<@FlowSources({FlowSource.MICROPHONE, FlowSource.TIME}) @FlowSinks({FlowSink.EMAIL, FlowSink.FILESYSTEM}) File> maTiFile = null;

        List<@FlowSources({FlowSource.MICROPHONE, FlowSource.TIME}) @FlowSinks({FlowSink.EMAIL, FlowSink.NETWORK}) File> maTiFile2 = null;

        List<@FlowSources({FlowSource.MICROPHONE}) @FlowSinks({FlowSink.EMAIL, FlowSink.NETWORK}) File> maFile2 = null;


        @FlowSources({FlowSource.PHONE_NUMBER}) @FlowSinks({FlowSink.EMAIL, FlowSink.LOGCAT})
        class Whatever {

            public Whatever(@FlowSources({FlowSource.PHONE_NUMBER}) @FlowSinks({FlowSink.NETWORK}) Whatever this) {

            }
        }


        final Object whatever = new @FlowSources({FlowSource.PHONE_NUMBER}) @FlowSinks({FlowSink.NETWORK}) Whatever();

        final @FlowSources({FlowSource.MICROPHONE}) @FlowSinks({FlowSink.RANDOM}) Object micToRandom = null;


        final @FlowSources({FlowSource.MICROPHONE}) @FlowSinks({FlowSink.NETWORK, FlowSink.EXTERNAL_STORAGE}) Object micToNExt = null;

        final @FlowSources({FlowSource.MICROPHONE}) @FlowSinks({FlowSink.NETWORK, FlowSink.LOGCAT, FlowSink.TEXTMESSAGE}) Object micToNetLogMsg = null;
    }

    public static void testInstantiate() {


        final Object obj = new PolicyTest();
    }
}
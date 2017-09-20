import sparta.checkers.quals.Sink;

import  sparta.checkers.quals.FlowPermission;
import static sparta.checkers.quals.FlowPermissionString.*;

public class ParameterizedSinksSubtypingTest {
    @Sink(FILESYSTEM+"(/usr/bin)") String wildcardSrc1;
    @Sink(FILESYSTEM+"(/usr/*)") String wildcardSrc2;
    @Sink(FILESYSTEM+"(/dev/null/,/usr/*)") String wildcardSrc3;

    @Sink(FILESYSTEM+"(myfile.txt)") String singleSrc1;
    @Sink(FILESYSTEM+"(myfile.txt)") String singleSrc2;


    @Sink(FILESYSTEM+"(myfile2.txt, myfile.txt)") String multipleSrc1;

    // Testing the subtyping heirarchy from the parameterized permissions documentation
    @Sink({INTERNET}) String sourceTop;
    @Sink(INTERNET+"(*.google.com)") String googleTopSource;
    @Sink(INTERNET+"(maps.google.com)") String googleMapsSource;
    @Sink(INTERNET+"(voice.google.com)") String googleVoiceSource;


    void testSameParameters() {
        singleSrc2 = singleSrc1;
    }

    void testMultipleParametersPass() {
        singleSrc1 = multipleSrc1;
    }

    void testMultipleParametersFail() {
        //:: error: (assignment.type.incompatible)
        multipleSrc1 = singleSrc1;
    }

    void testWildcardParametersPass() {
        wildcardSrc1 = wildcardSrc2;
        wildcardSrc2 = wildcardSrc3;
        wildcardSrc1 = wildcardSrc3;
    }

    void testWildcardParametersFail() {
        //:: error: (assignment.type.incompatible)
        wildcardSrc2 = wildcardSrc1;

        //:: error: (assignment.type.incompatible)
        wildcardSrc3 = wildcardSrc2;
    }

    void testGoogleTop() {
        // TODO: FIX
        // sourceTop = googleTopSource;
    }

    void testGoogleMaps() {
        googleMapsSource = googleTopSource;
    }

    void testGoogleVoice() {
        googleVoiceSource = googleTopSource;
    }

    void testGoogleReverseFailure() {
        //:: error: (assignment.type.incompatible)
        googleTopSource = googleVoiceSource;

        //:: error: (assignment.type.incompatible)
        googleTopSource = googleMapsSource;
    }
}

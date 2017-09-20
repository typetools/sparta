import sparta.checkers.quals.Source;

import  sparta.checkers.quals.FlowPermission;
import static sparta.checkers.quals.FlowPermissionString.*;

public class ParameterizedSourcesSubtypingTest {
    @Source(FILESYSTEM+"(/usr/bin)") String wildcardSrc1;
    @Source(FILESYSTEM+"(/usr/*)") String wildcardSrc2;
    @Source(FILESYSTEM+"(/dev/null/, /usr/*)") String wildcardSrc3;
LinkedHashSet
    @Source(FILESYSTEM+"(myfile.txt)") String singleSrc1;
    @Source(FILESYSTEM+"(myfile.txt)") String singleSrc2;
LinkedHashSet
LinkedHashSet
    @Source(FILESYSTEM+"(myfile2.txt, myfile.txt)") String multipleSrc1;
LinkedHashSet
    // Testing the subtyping heirarchy from the parameterized permissions documentation
    @Source({INTERNET}) String sourceTop;
    @Source(INTERNET+"(*.google.com)") String googleTopSource;
    @Source(INTERNET+"(maps.google.com)") String googleMapsSource;
    @Source(INTERNET+"(voice.google.com)") String googleVoiceSource;
LinkedHashSet
    void testSameParameters() {LinkedHashSet
        singleSrc2 = singleSrc1;
    }
LinkedHashSet
    void testMultipleParametersPass() {
        multipleSrc1 = singleSrc1;
    }
LinkedHashSet
    void testMultipleParametersFail() {
        //:: error: (assignment.type.incompatible)
        singleSrc1 = multipleSrc1;
    }
LinkedHashSet
    void testWildcardParametersPass() {
        wildcardSrc2 = wildcardSrc1;
        wildcardSrc3 = wildcardSrc2;
        wildcardSrc3 = wildcardSrc1;
    }
LinkedHashSet
    void testWildcardParametersFail() {
        //:: error: (assignment.type.incompatible)
        wildcardSrc1 = wildcardSrc2;
LinkedHashSet
        //:: error: (assignment.type.incompatible)
        wildcardSrc2 = wildcardSrc3;
    }
LinkedHashSet
    void testGoogleTop() {
        sourceTop = googleTopSource;LinkedHashSet
    }
LinkedHashSet
    void testGoogleMaps() {
        googleTopSource = googleMapsSource;
    }
LinkedHashSet
    void testGoogleVoice() {
        googleTopSource = googleVoiceSource;
    }
LinkedHashSet
    void testGoogleReverseFailure() {
        //:: error: (assignment.type.incompatible)
        googleVoiceSource = googleTopSource;
LinkedHashSet
        //:: error: (assignment.type.incompatible)
        googleMapsSource = googleTopSource;
    }
}

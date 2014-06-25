import sparta.checkers.quals.Source;
import sparta.checkers.quals.FineSource;

import  sparta.checkers.quals.FlowPermission;
import static sparta.checkers.quals.FlowPermission.*;

public class ParameterizedSourcesSubtypingTest {
    @Source(value={}, finesources={@FineSource(value=FILESYSTEM, params={"/usr/bin"})}) String wildcardSrc1;
    @Source(value={}, finesources={@FineSource(value=FILESYSTEM, params={"/usr/*"})}) String wildcardSrc2;
    @Source(value={}, finesources={@FineSource(value=FILESYSTEM, params={"/dev/null/", "/usr/*"})}) String wildcardSrc3;
    
    @Source(value={}, finesources={@FineSource(value=FILESYSTEM, params={"myfile.txt"})}) String singleSrc1;
    @Source(value={}, finesources={@FineSource(value=FILESYSTEM, params={"myfile.txt"})}) String singleSrc2;
  
    
    @Source(value={}, finesources={@FineSource(value=FILESYSTEM, params={"myfile2.txt", "myfile.txt"})}) String multipleSrc1;
    
    // Testing the subtyping heirarchy from the parameterized permissions documentation
    @Source({INTERNET}) String sourceTop;
    @Source(value={}, finesources={@FineSource(value=INTERNET, params={"*.google.com"})}) String googleTopSource;
    @Source(value={}, finesources={@FineSource(value=INTERNET, params={"maps.google.com"})}) String googleMapsSource;
    @Source(value={}, finesources={@FineSource(value=INTERNET, params={"voice.google.com"})}) String googleVoiceSource;
    
    void testSameParameters() {        
        singleSrc2 = singleSrc1;
    }
    
    void testMultipleParametersPass() {
        multipleSrc1 = singleSrc1;
    }
    
    void testMultipleParametersFail() {
        //:: error: (assignment.type.incompatible)
        singleSrc1 = multipleSrc1;
    }
    
    void testWildcardParametersPass() {
        wildcardSrc2 = wildcardSrc1;
        wildcardSrc3 = wildcardSrc2;
        wildcardSrc3 = wildcardSrc1;
    }
    
    void testWildcardParametersFail() {
        //:: error: (assignment.type.incompatible)
        wildcardSrc1 = wildcardSrc2;
        
        //:: error: (assignment.type.incompatible)
        wildcardSrc2 = wildcardSrc3;
    }
    
    void testGoogleTop() {
        sourceTop = googleTopSource; 
    }
    
    void testGoogleMaps() {
        googleTopSource = googleMapsSource;
    }
    
    void testGoogleVoice() {
        googleTopSource = googleVoiceSource;
    }
    
    void testGoogleReverseFailure() {
        //:: error: (assignment.type.incompatible)
        googleVoiceSource = googleTopSource;
        
        //:: error: (assignment.type.incompatible)
        googleMapsSource = googleTopSource;
    }
}

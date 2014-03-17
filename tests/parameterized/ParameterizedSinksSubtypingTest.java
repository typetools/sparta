import sparta.checkers.quals.Sink;
import sparta.checkers.quals.FineSink;

import  sparta.checkers.quals.FlowPermission;
import static sparta.checkers.quals.FlowPermission.*;

public class ParameterizedSinksSubtypingTest {
    @Sink(value={}, finesinks={@FineSink(value=CONDITIONAL, params={"/usr/bin"})}) String wildcardSrc1;
    @Sink(value={}, finesinks={@FineSink(value=CONDITIONAL, params={"/usr/*"})}) String wildcardSrc2;
    @Sink(value={}, finesinks={@FineSink(value=CONDITIONAL, params={"/dev/null/", "/usr/*"})}) String wildcardSrc3;
    
    @Sink(value={}, finesinks={@FineSink(value=CONDITIONAL, params={"myfile.txt"})}) String singleSrc1;
    @Sink(value={}, finesinks={@FineSink(value=CONDITIONAL, params={"myfile.txt"})}) String singleSrc2;
  
    
    @Sink(value={}, finesinks={@FineSink(value=CONDITIONAL, params={"myfile2.txt", "myfile.txt"})}) String multipleSrc1;
    
    // Testing the subtyping heirarchy from the parameterized permissions documentation
    @Sink({INTERNET}) String sourceTop;
    @Sink(value={}, finesinks={@FineSink(value=INTERNET, params={"*.google.com"})}) String googleTopSource;
    @Sink(value={}, finesinks={@FineSink(value=INTERNET, params={"maps.google.com"})}) String googleMapsSource;
    @Sink(value={}, finesinks={@FineSink(value=INTERNET, params={"voice.google.com"})}) String googleVoiceSource;
    
  
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

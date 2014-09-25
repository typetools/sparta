import sparta.checkers.quals.AddsSourceData;
import sparta.checkers.quals.PolyFlowReceiver;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import static sparta.checkers.quals.FlowPermission.*;


public class AddsSourceDataTest {
    @AddsSourceData(1)
    void addsSource(@Source(READ_SMS) @Sink({}) Object o) {
    }

    @AddsSourceData
    void addsSourceArray(
            @Source(READ_SMS) @Sink({}) byte @Source( {}) @Sink( {}) [] o) {
    }

    @AddsSourceData(2)
    void addsSourceSecondParam(@Source({}) @Sink({}) Object o,
            @Source(READ_SMS) @Sink({}) Object o2) {
    }

    @AddsSourceData(2)
    void addsSourceArraySecondParam(
            @Source({}) @Sink({}) byte @Source( {}) @Sink( {}) [] o,
            @Source(READ_SMS) @Sink({}) byte @Source( {}) @Sink( {}) [] o2) {
    }

    @AddsSourceData(0)
    public void  addsSourceReceiver(
            @Source(READ_SMS) @Sink( {}) AddsSourceDataTest this) {
    }


   
    @Source({}) @Sink({}) Object emptyObject = null;
    @Source({READ_TIME}) @Sink({}) Object timeObject = null;
    @Source({READ_SMS}) @Sink({}) Object smsObject = null;

    void testObjects(){
        //:: error: (missing.source.argument)
        addsSource(emptyObject);
        //:: error: (missing.source.argument)
        addsSourceSecondParam(emptyObject, emptyObject);
        
        //:: error: (missing.source.argument) 
        addsSource(timeObject);
        //:: error: (missing.source.argument) 
        addsSourceSecondParam(emptyObject, timeObject);
        
        addsSource(smsObject);
        addsSourceSecondParam(emptyObject, smsObject);
    }
    
    @Source({}) @Sink({}) byte @Source({}) @Sink({})[] emptyByteArray = null;
    @Source({READ_TIME}) @Sink({}) byte @Source({}) @Sink({})[] timeByteArray = null;
    @Source({READ_SMS}) @Sink({}) byte @Source({}) @Sink({})[] smsByteArray = null;

    void testByteArray(){
        //:: error: (missing.source.argument)
        addsSourceArray(emptyByteArray);
        //:: error: (missing.source.argument)
        addsSourceArraySecondParam(emptyByteArray, emptyByteArray);
        
        //:: error: (missing.source.argument) 
        addsSourceArray(timeByteArray);
        //:: error: (missing.source.argument) 
        addsSourceArraySecondParam(emptyByteArray, timeByteArray);
        
        addsSourceArray(smsByteArray);
        addsSourceArraySecondParam(emptyByteArray, smsByteArray);
    }
    
    @Source({}) @Sink({}) AddsSourceDataTest emptyASDT = null;
    @Source({READ_TIME}) @Sink({}) AddsSourceDataTest timeASDT = null;
    @Source({READ_SMS}) @Sink({}) AddsSourceDataTest smsASDT = null;
    
    void testReceiver(){
        //:: error: (missing.source.receiver)
        emptyASDT.addsSourceReceiver();
        //:: error: (method.invocation.invalid) :: error: (missing.source.receiver)
        timeASDT.addsSourceReceiver();
        smsASDT.addsSourceReceiver();
    }
    
    @AddsSourceData
    public AddsSourceDataTest( @Source({READ_SMS}) @Sink({}) Object o){
        
    }
    
    @AddsSourceData
    public AddsSourceDataTest( @Source({READ_SMS}) @Sink({}) byte  @Source({}) @Sink({}) [] o){
        
    }
    void testConstructors(){
        //:: error: (missing.source.argument)
        new AddsSourceDataTest(emptyByteArray);
        //:: error: (missing.source.argument) 
        new AddsSourceDataTest(timeByteArray);
        new AddsSourceDataTest(smsByteArray);
        
        //:: error: (missing.source.argument)
        new AddsSourceDataTest(emptyObject);
        //:: error: (missing.source.argument) 
        new AddsSourceDataTest(timeObject);
        new AddsSourceDataTest(smsObject);
    }
    
    @AddsSourceData(1)  
    @PolyFlowReceiver
    Object poly(Object o){ return o;}
    
    void testPoly(){
        smsASDT.poly(smsObject);
        //:: error: (missing.source.argument)
        smsASDT.poly(emptyObject);
        //:: error: (missing.source.argument)
        smsASDT.poly(timeObject);
    }
    
    //******Test malformed @AddsSourceData annotations*********
    @AddsSourceData(2)
    //:: warning: (addssource.index.outofbounds)
    void m1(){}
    @AddsSourceData(0)
    //:: warning: (addssource.no.receiver)
    public AddsSourceDataTest(){}
    @AddsSourceData(3)
    //:: warning: (addssource.index.outofbounds)
    public AddsSourceDataTest(String s1, String s2){}
    
    void testMalformed(){
        m1();
        new AddsSourceDataTest();
        new AddsSourceDataTest("1","2");
    }
    
    
}

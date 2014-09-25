import sparta.checkers.quals.PolySinkR;
import sparta.checkers.quals.PolySourceR;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import static sparta.checkers.quals.FlowPermission.*;


public class ReceiverPolyTest {
    //Because the construtor does not have a receiver, the result will always be top
    public @PolySinkR @PolySourceR ReceiverPolyTest(@PolySinkR @PolySourceR String param){}
    
    void useConstructor(@Source(READ_SMS) @Sink({})  String sourceReadSms){
        @Source(ANY) @Sink({}) ReceiverPolyTest test = new ReceiverPolyTest(sourceReadSms);
    }
    
    void voidMethod(@PolySourceR @PolySinkR ReceiverPolyTest this,
            @PolySourceR @PolySinkR String s) {
    }

    @PolySourceR @PolySinkR  String returnTypeMethod(
            @PolySourceR @PolySinkR ReceiverPolyTest this,
            @PolySourceR @PolySinkR String s) {
        return s;
    }

    void useMethod(@Source(READ_SMS) @Sink({}) String sourceReadSms,
            @Source({}) @Sink({WRITE_SMS, WRITE_CALENDAR}) String sinkSmsCal,
            @Source({}) @Sink(WRITE_SMS) ReceiverPolyTest sinkWriteSms) {
        //:: error: (argument.type.incompatible)
        sinkWriteSms.voidMethod(sourceReadSms);
        
        @Source({}) @Sink(WRITE_SMS) String r = 
       //:: error: (argument.type.incompatible)
        sinkWriteSms.returnTypeMethod(sourceReadSms);
        
        sinkWriteSms.voidMethod(sinkSmsCal);
        
        @Source({}) @Sink(WRITE_SMS)
        String r2 =  sinkWriteSms.returnTypeMethod(sinkSmsCal);   
        
        //Although the parameter is allowed to go WRITE_CALENDAR,
        //the receiver (and therefor the return type is not).
        //In other words, check that the LUB of the receiver 
        //and parameter(s) is not returned.
        @Source({}) @Sink({WRITE_SMS,WRITE_CALENDAR}) String r3 =  
                //:: error: (assignment.type.incompatible)
                sinkWriteSms.returnTypeMethod(sinkSmsCal);   
    }
    class Inner{
        public @PolySinkR @PolySourceR Inner(@PolySinkR @PolySourceR ReceiverPolyTest ReceiverPolyTest.this ){}
        public @PolySinkR Inner(@PolySinkR @PolySourceR ReceiverPolyTest ReceiverPolyTest.this, @PolySinkR @PolySourceR String s ){}
    }
    
    //Receivers on inner class to not work.
    void useInnerClass(@Source(READ_SMS) @Sink({}) String sourceReadSms,
            @Source({}) @Sink({WRITE_SMS, WRITE_CALENDAR}) String sinkSmsCal,
            @Source({}) @Sink(WRITE_SMS) ReceiverPolyTest sinkWriteSms){
        ////:: error: (argument.type.incompatible)
        sinkWriteSms.new Inner(sourceReadSms);
        //The following error should not occur.
        //:: error: (assignment.type.incompatible)
        @Source({}) @Sink(WRITE_SMS) ReceiverPolyTest.Inner r = sinkWriteSms.new Inner();
        
        sinkWriteSms.new Inner(sinkSmsCal);
        
        @Source({}) @Sink(WRITE_SMS)
        ReceiverPolyTest.Inner r2 =  sinkWriteSms.new Inner(sinkSmsCal);   
        
        //Although the parameter is allowed to go WRITE_CALENDAR,
        //the receiver (and therefor the return type is not).
        //In other words, check that the LUB of the receiver 
        //and parameter(s) is not returned.
        @Source({}) @Sink({WRITE_SMS,WRITE_CALENDAR}) ReceiverPolyTest.Inner r3 =  
                ////:: error: (assignment.type.incompatible)
                sinkWriteSms.new Inner(sinkSmsCal);   
    }

}

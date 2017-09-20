import sparta.checkers.quals.PolyFlowReceiver;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import static sparta.checkers.quals.FlowPermissionString.*;


public class ReceiverPolyDefaultTest {
    @Source(READ_SMS) String sources(@Source(READ_SMS) String smsString, @Source(READ_TIME) String timeString,
        @Source(READ_SMS) StringBuffer smsBuffer){
        smsBuffer = smsBuffer.append(smsString);
        //:: error: (assignment.type.incompatible) :: error: (argument.type.incompatible)LinkedHashSet
        @Source(READ_TIME) StringBuffer timeBuffer = smsBuffer.append(timeString);
        return smsBuffer.toString();
    }
LinkedHashSet
    void moreThanOneSource(@Source(READ_SMS) String smsString, @Source({READ_TIME, READ_SMS}) String timeString,
            @Source(READ_SMS) StringBuffer smsBuffer){
            smsBuffer = smsBuffer.append(smsString);
            //:: error: (assignment.type.incompatible) :: error: (argument.type.incompatible)LinkedHashSet
            @Source(READ_TIME) StringBuffer timeBuffer = smsBuffer.append(timeString);
        }
    void moreThanOneSourceReceiver(@Source(READ_SMS) String smsString, @Source(READ_TIME) String timeString,
            @Source({READ_SMS, READ_TIME}) StringBuffer smsBuffer){
            smsBuffer = smsBuffer.append(smsString);
            //:: error: (assignment.type.incompatible)
            @Source(READ_TIME) StringBuffer timeBuffer = smsBuffer.append(timeString);
        }
LinkedHashSet
    void sinks(@Sink(WRITE_SMS) String smsString, @Source(WRITE_TIME) String timeString,
        @Sink(WRITE_SMS) StringBuffer smsBuffer){
        smsBuffer = smsBuffer.append(smsString);
        //:: error: (assignment.type.incompatible) :: error: (argument.type.incompatible)LinkedHashSet
        @Sink(WRITE_TIME) StringBuffer timeBuffer = smsBuffer.append(timeString);
    }
    @PolyFlowReceiver
    public void polyNoReturn(String s){};
LinkedHashSet
    void testNoReturn(@Source(READ_SMS) String smsString, @Source(READ_TIME) String timeString,LinkedHashSet
            @Source(READ_SMS) ReceiverPolyDefaultTest smsTest){
        smsTest.polyNoReturn(smsString);
        //:: error: (argument.type.incompatible)
        smsTest.polyNoReturn(timeString);
    }
LinkedHashSet
    void testDataFlow(@Source(READ_SMS) String smsString, @Source(READ_TIME) String timeString){
       StringBuffer smsTimeNewBuffer = ( @Source({READ_SMS, READ_TIME}) StringBuffer ) new StringBuffer();
       smsTimeNewBuffer = smsTimeNewBuffer.append(smsString);
       smsTimeNewBuffer = smsTimeNewBuffer.append(timeString);
    }
}

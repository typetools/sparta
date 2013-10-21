import sparta.checkers.quals.Source;
import  sparta.checkers.quals.FlowPermission;

import sparta.checkers.quals.Sink;
import static sparta.checkers.quals.FlowPermission.*;

class FlowCompletion {
    
    void test(@Source(READ_SMS) FlowCompletion this){
        writeToFile(this);
        
        @Source(READ_SMS) Object mysms = null;
        writeToFile(mysms);
        
        Object local = readSms();
        writeToFile(local);
        //:: warning: (cast.unsafe)
        Object literal = (/*@Source(READ_SMS)*/ String) "hello";
        writeToFile(literal);
    }
    void testReceiver(){
        FlowCompletion c = (/*@Source(READ_SMS)*/ FlowCompletion) new FlowCompletion();
        c.test();
    }
    
    void writeToFile(@Source(READ_SMS) @Sink(FILESYSTEM) Object o){}
    @Source(READ_SMS) Object readSms(){ return null;}
}


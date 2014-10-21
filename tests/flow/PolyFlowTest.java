import org.checkerframework.dataflow.qual.Pure;

import sparta.checkers.quals.*;
import sparta.checkers.quals.Source.*;
import sparta.checkers.quals.Sink.*;
import static sparta.checkers.quals.FlowPermissionString.*;

@PolyFlow
class Cons {

    String defaultReceiver(String s) {
        return s;
    }

    String noParms() {
        return null;
    }

    @PolyFlowReceiver
    String polyFlowReceiver(String s) {
        return s;
    }

    @PolyFlowReceiver
    @Pure
    @Override
    public String toString() {
        return "hello";
    }
}

class Use {
    @Source({}) @Sink(INTERNET) String emptyInet = "";
    @Source(READ_SMS) @Sink(INTERNET) String readSMSInet;
    @Source({}) @Sink(ANY) String bottom;
    @Source(ANY) @Sink({}) String top;

    void polyFlowMethod(@Source({}) @Sink(ANY) Cons bottom) {
        @Source({}) @Sink(INTERNET) String a = bottom.defaultReceiver(this.emptyInet);
        @Source(READ_SMS) @Sink(INTERNET) String b = bottom.defaultReceiver(this.readSMSInet);
        @Source({}) @Sink(ANY) String c =  bottom.defaultReceiver(this.bottom);
        @Source(ANY) @Sink({}) String d = bottom.defaultReceiver(this.top);    
    }
    
    void polyFlowMethodFail(@Source({}) @Sink(ANY) Cons bottom) {
        //:: error: (assignment.type.incompatible)
        @Source({}) @Sink(ANY) String a = bottom.defaultReceiver(this.emptyInet);
        //:: error: (assignment.type.incompatible)
        @Source({}) @Sink(ANY) String b = bottom.defaultReceiver(this.readSMSInet);
        //:: error: (assignment.type.incompatible)
        @Source({}) @Sink(ANY) String d = bottom.defaultReceiver(this.top);   
    }
    void polyFlowReceiver(@Source({}) @Sink(INTERNET) Cons emptyInet, 
            @Source(READ_SMS) @Sink(INTERNET) Cons readSMSInet,         
            @Source({}) @Sink(ANY) Cons bottom,
            @Source(ANY) @Sink({}) Cons top) {
        
        @Source({}) @Sink(INTERNET) String a = emptyInet.defaultReceiver(this.emptyInet);
        @Source(READ_SMS) @Sink(INTERNET) String b = readSMSInet.defaultReceiver(this.readSMSInet);
        @Source({}) @Sink(ANY) String c =  bottom.defaultReceiver(this.bottom);
        @Source(ANY) @Sink({}) String d = top.defaultReceiver(this.top);    
    }
    
    void polyFlowReceiverLubs(@Source({}) @Sink(INTERNET) Cons emptyInet, 
            @Source(READ_SMS) @Sink(INTERNET) Cons readSMSInet,         
            @Source({}) @Sink(ANY) Cons bottom,
            @Source(ANY) @Sink({}) Cons top) {
        @Source({READ_SMS}) @Sink(INTERNET) String a = readSMSInet.defaultReceiver(this.emptyInet);
        @Source(READ_SMS) @Sink(INTERNET) String b = emptyInet.defaultReceiver(this.readSMSInet);
        
        @Source({}) @Sink(ANY) String c =  bottom.defaultReceiver(this.bottom);
        @Source({}) @Sink(ANY) String d = bottom.defaultReceiver(this.bottom);    
    }
}

class GetterSetter {
    private @PolySource @PolySink String field;

    public @PolySource
    @PolySink
    GetterSetter(@PolySource @PolySink String field) {
        this.field = field;
    }

    @PolyFlowReceiver
    public String getField() {
        return field;
    }

    @PolyFlowReceiver
    public void setField(String field) {
        this.field = field;
    }

    @PolyFlowReceiver
    @Pure
    @Override
    public String toString() {
        return "field: " + this.field;
    }
}

class TestGetterSetter {
    public void method(@Source(READ_SMS) @Sink(INTERNET) String readsms,
            @Source(READ_TIME) @Sink(WRITE_TIME) String time) {
        
        @Source(READ_SMS) @Sink(INTERNET) GetterSetter gs = new GetterSetter(readsms);
        @Source(READ_SMS) @Sink(INTERNET) String test1 = gs.getField();
        gs.setField(readsms);
        //:: error: (argument.type.incompatible)
        gs.setField(time);
        @Source(READ_SMS) @Sink(INTERNET) String test2 = gs.toString();
        //:: error: (assignment.type.incompatible)
        @Source(READ_TIME) @Sink(WRITE_TIME) String test3 = gs.getField();
        //:: error: (assignment.type.incompatible)
        @Source(READ_TIME) @Sink(WRITE_TIME) String test4 = gs.toString();
    }
}
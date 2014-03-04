import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;
import  sparta.checkers.quals.FlowPermission;

class Conditions {
    void good(int p) {
        if (p < 11) {}

        @Sink({FlowPermission.CONDITIONAL}) boolean someBool = true;
        if(someBool) {
            // good
        }
    }

    void bad(@Sink(FlowPermission.MANAGE_ACCOUNTS) int p) {
        //:: error: (condition.flow)
        if (p > 9) {
            // boom.
        }

        //:: error: (condition.flow)
        while ((p % 5) > 2) {}

        // Flow propagates source from p to b.
        boolean b = p < 9;
        //:: error: (condition.flow)
        int answer = b ? 42 : 33;
    }

    void bad(@Sink(FlowPermission.MANAGE_ACCOUNTS) boolean p) {
        //:: error: (condition.flow)
        if(p) {
            // bad.
        }
    }
    void lit (@Source({READ_SMS, LITERAL}) int s)
    {
       if(s == 1){
           //if we didn't turn of condtional refinements (FATF.createFlowTransferFunction(...)
           //Then s would be LITERAL->FILE
         // and the flowing code would not issue a warning, 
           //but SMS data is written to a file
           //:: error: (argument.type.incompatible)
           writeToFile(s); 
       }else{
         //s is SMS, LITERAL -> CONDITIONAL
           //:: error: (argument.type.incompatible)
           writeToFile(s);  //Warning
       }
    }
    void writeToFile(@Sink(FILESYSTEM) int s){
        
    }
}
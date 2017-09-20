import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermissionString.*;
import  sparta.checkers.quals.FlowPermission;

class Conditions {
    void good(int p) {
        if (p < 11) {}

        @Sink({}) boolean someBool = true;
        if(someBool) {
            // good
        }
    }

    void bad(@Sink(FlowPermissionString.MANAGE_ACCOUNTS) int p) {
        //TODO: test condition mode//:: error: (condition.flow)
        if (p > 9) {
            // boom.
        }

        //TODO: test condition mode//:: error: (condition.flow)
        while ((p % 5) > 2) {}

        // Flow propagates source from p to b.
        boolean b = p < 9;
        //TODO: test condition mode//:: error: (condition.flow)
        int answer = b ? 42 : 33;
    }

    void bad(@Sink(FlowPermissionString.MANAGE_ACCOUNTS) boolean p) {
        //TODO: test condition mode//:: error: (condition.flow)
        if(p) {
            // bad.
        }
    }
    void lit (@Source({READ_SMS}) int s)
    {
       if(s == 1){
           //if we didn't turn of condtional refinements (FATF.createFlowTransferFunction(...)
           //Then s would be LITERAL->FILE
         // and the following code would not issue a warning, 
           //but SMS data is written to a file
           //:: error: (argument.type.incompatible)
           writeToFile(s); 
       }else{
         //s is SMS -> {}
           //:: error: (argument.type.incompatible)
           writeToFile(s);  //Warning
       }
    }
    void writeToFile(@Sink(FILESYSTEM) int s){
        
    }
    private @Source({}) @Sink({}) float mLastValues @Source({}) @Sink({}) [] = new float[3*2];
    void ternary(){
        int k = 0;
        float v = k / 3;
        float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k    ] ? -1 : 0));
        if(direction == 0){
            //TODO:This is a bug
            //TODO: test condition mode//:: error: (condition.flow)
            int extType = (direction > 0 ? 0 : 1);
        }
    }
}
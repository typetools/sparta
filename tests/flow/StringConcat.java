import java.io.File;

import android.content.Context;
import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.*;
import sparta.checkers.quals.FlowSinks.*;


class Use {

    void demo() {
       @FlowSources(FlowSource.LITERAL) Context ctx = null;
    	//:: error: (forbidden.flow)
       File file = ctx.getDir("log", 0);
      // String fileString = file.toString();
   	   //:: error: (forbidden.flow)
       String filename = file +  "test.jpg";    
                                   
    }

}

import java.io.File;

import android.content.Context;
import sparta.checkers.quals.*;
import sparta.checkers.quals.Source.*;
import sparta.checkers.quals.Sink.*;


class Use {

    void demo() {
       @Source(FlowPermission.LITERAL) Context ctx = null;
    	//:: error: (forbidden.flow)
       File file = ctx.getDir("log", 0);
      // String fileString = file.toString();
   	   //:: error: (forbidden.flow)
       String filename = file +  "test.jpg";    
                                   
    }

}

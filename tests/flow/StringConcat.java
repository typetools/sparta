import java.io.File;

import android.content.Context;
import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.*;
import sparta.checkers.quals.Sinks.*;


class Use {

    void demo() {
       @Sources(SPARTA_Permission.LITERAL) Context ctx = null;
    	//:: error: (forbidden.flow)
       File file = ctx.getDir("log", 0);
      // String fileString = file.toString();
   	   //:: error: (forbidden.flow)
       String filename = file +  "test.jpg";    
                                   
    }

}

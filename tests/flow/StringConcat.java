import java.io.File;

import android.content.Context;
import sparta.checkers.quals.*;
import sparta.checkers.quals.Source.*;
import sparta.checkers.quals.Sink.*;
import static  sparta.checkers.quals.FlowPermission.*;


class Use {

    void demo() {
       @Source(FlowPermission.LITERAL) Context ctx = null;

       @Sink(FILESYSTEM) File file = ctx.getDir("log", 0);
       String fileString = file.toString();
       String s = fileString.toString();

     
       
       
       String filename = fileString +  "test.jpg";   
                                   
    }

}

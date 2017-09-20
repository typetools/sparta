import java.io.File;

import android.content.Context;
import sparta.checkers.quals.*;
import sparta.checkers.quals.Source.*;
import sparta.checkers.quals.Sink.*;
import static  sparta.checkers.quals.FlowPermissionString.*;


class Use {

    void demo() {
       @Source({}) Context ctx = null;
       @Source({FILESYSTEM,READ_EXTERNAL_STORAGE}) File file = ctx.getDir("log", 0);
       String fileString = file.toString();
       String s = fileString.toString();
       String filename = fileString +  "test.jpg";

    }

}

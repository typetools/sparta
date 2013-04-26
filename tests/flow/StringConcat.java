import android.content.Context;
import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.*;
import sparta.checkers.quals.FlowSinks.*;


class Use {

    void demo() {
       @FlowSources(FlowSource.LITERAL) Context ctx = null;
    	/*@FlowSources(FlowSource.FILESYSTEM) @FlowSinks(FlowSink.ANY) File getDir(
         *                           @FlowSources(FlowSource.ANY) @FlowSinks(FlowSink.FILESYSTEM) String name, 
         *                           @FlowSources(FlowSource.ANY) @FlowSinks(FlowSink.CONDITIONAL) int mode
         *                           );
         */
    	// class File {	@PolyFlowSources @PolyFlowSinks String toString() @PolyFlowSources @PolyFlowSinks;}

    	//ctx.getDir("log", Context.MODE_PRIVATE).toString() is FILESYSTEM->ANY
    	//test.jpg 
    	//I'm not sure if this error is a bug or as expected?
    	String filename = ctx.getDir("log", 0).toString() +  "test.jpg";    
                                   
    }

}

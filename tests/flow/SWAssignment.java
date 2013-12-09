
import static sparta.checkers.quals.CoarseFlowPermission.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.PolyFlowReceiver;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;


class SupressWarningsAssignment {
    void declNoSW(){
        @SuppressWarnings("flow")
        @Source(CAMERA) @Sink({CONDITIONAL, DISPLAY, FILESYSTEM}) ByteArrayOutputStream bytes = getBAOS();
        
       /* Because @Source(LITERAL) is not a subtype of @Source(CAMERA), assigning getBAOS 
          to bytes produces an assignment warning. (The waring is supressed.)
          @Sink(CONDITIONAL, DISPLAY,WRITE_LOGS, FILESYSTEM) is a subtype of
          @Sink({CONDITIONAL, DISPLAY, FILESYSTEM}), so the sink of bytes is refined to 
          @Sink(CONDITIONAL, DISPLAY,WRITE_LOGS, FILESYSTEM). The full type of bytes is
          @Source(CAMERA) @Sink({CONDITIONAL, DISPLAY,WRITE_LOGS, FILESYSTEM}), but 
          this is not valid type because CAMERA->WRITE_LOGS is a forbidden flow. 
           So the following line produces a forbidden flow warning: */
        
        //:: error: (forbidden.flow)
        int i = bytes.size(); //CAMERA->WRITE_LOGS

     
        /*Casting does not have this problem:*/
        @SuppressWarnings("flow")
        ByteArrayOutputStream bytesCast = (@Source(CAMERA) @Sink({CONDITIONAL, DISPLAY, FILESYSTEM})  ByteArrayOutputStream) getBAOS();

        int icast = bytesCast.size();
        
        /*Casting does not have this problem:*/
        @SuppressWarnings("flow")
        ByteArrayOutputStream bytesCast2 = (@Source(CAMERA)  ByteArrayOutputStream) getBAOS();

        int icast2 = bytesCast.size();

    }
    @Source(READ_SMS) ByteArrayOutputStream getBAOS(){
        return null;
        }
  }
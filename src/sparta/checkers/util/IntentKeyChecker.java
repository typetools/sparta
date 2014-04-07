package sparta.checkers.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.tools.Diagnostic;

import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.common.value.ValueChecker;
import org.checkerframework.framework.qual.StubFiles;

/**
 * 
 * Class to output statistic about keys used in putExtra of getExtra class
 * 
 *  intentkey-summary.csv
 *  user directory, get or put extra, number of calls, number of unknown keys,
 *    number of keys that resolve to one value, number of keys that resolve to more than on value 
 * Example: 
/Users/smillst/src/engagements/3/a/Reveal/Reveal,getExtra,5,0,0,5
/Users/smillst/src/engagements/3/a/Reveal/Reveal,putExtra,7,0,0,7
 * 
 * intentkey.txt
 * Includes list of types in get and put extra class and a list of all used keys.
 * 
 * @author smillst
 *
 */
@StubFiles("receive-send-intent.astub")
public class IntentKeyChecker extends ValueChecker {
    public  FileOutputStream writer;
    //number of getExtra calls
    int numGetExtra = 0;

    //number of putExtra calls:
    int numPutExtra = 0;


    // For keys used in getExtra Calls:
    // number of keys whose string value is unknown:
    int getExtraUnknownKey = 0;
    // number of keys that resolve to multiple strings:
    int getExtraMultiKey = 0;
    // number of keys that resolve to one string
    int getExtraOneKey = 0;
    // number of keys that eppic's SA could resolve
    int getExtraKeyEpicc=0;
    
    // For keys used in putExtra Calls:
    // number of keys whose string value is unknown:
    int putExtraUnknownKey = 0;
    // number of keys that resolve to multiple strings:
    int putExtraMultiKey = 0;
    // number of keys that resolve to one string
    int putExtraOneKey = 0;
    // number of keys that eppic's SA could resolve
    int putExtraKeyEpicc=0;

    Map<String, Integer> keys = new HashMap<>();
    Map<String, Integer>  putExtraTypes = new HashMap<>();
    Map<String, Integer> getExtraTypes = new HashMap<String, Integer>();

    
    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new IntentKeyVisitor(this);
    }
    @Override
    public void typeProcessingOver() {
        try {
            writer = new FileOutputStream("intentkey-summary.csv");
            //program,get/put,#unknown,#multiple,#unique,#handled_by_epic,...
            String appdir = System.getProperty("user.dir");
            write(appdir);
            write("getExtra");
            write(numGetExtra);
            write(getExtraUnknownKey);
            write(getExtraMultiKey);
            writeNoComma(getExtraOneKey);
//            write(getExtraKeyEpicc);
            writenewline();

            write(appdir);
            write("putExtra");
            write(numPutExtra);
            write(putExtraUnknownKey);
            write(putExtraMultiKey);
            writeNoComma(putExtraOneKey);
//            write(putExtraKeyEpicc);
            writenewline();


            writer = new FileOutputStream("intentkey.txt");

            write("\n**************Found getExtra Types*******************");
            for(Entry<String, Integer>e :getExtraTypes.entrySet()){
                write(e.getKey()+" "+e.getValue());
            }
            write("\n**************Found putExtra Types*******************");
            for(Entry<String, Integer>e :putExtraTypes.entrySet()){
                write(e.getKey()+" "+e.getValue());
            }
            
            write("\n**************List of found keys*******************");
            for(Entry<String, Integer>e :keys.entrySet()){
                write(e.getKey()+" "+e.getValue());
            }
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
       
        super.typeProcessingOver();
    }

    private void writeNoComma(Object s) {
        try {
            writer.write(s.toString().getBytes());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
    }
    private void writenewline() {
        writeNoComma("\n");       
    }
    private void write(Object s){
           writeNoComma(s);
           writeNoComma(",");

    }
    
   
    @Override
    public void message(Diagnostic.Kind kind, Object source, /**/
            String msgKey, Object... args) {
        //Don't output any checker error messages.
//       if(msgKey.equals("intent.key.error"))
//       {
//           super.message(kind, source, msgKey, args);
//       }
    }
}

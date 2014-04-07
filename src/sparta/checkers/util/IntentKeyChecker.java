package sparta.checkers.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.tools.Diagnostic;

import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.common.value.ValueChecker;
import org.checkerframework.framework.qual.StubFiles;
import org.checkerframework.javacutil.Pair;

/**
 * 
 * For each app:
 * 
 * number of getExtra calls:
 * number of putExtra calls:
 * 
 * For keys used in getExtra Calls:
 * number of keys whose string value is unknown:
 * number of keys that resolve to multiple strings:
 * number of keys that resolve to one string:
 * 
 * For keys used in putExtra Calls:
 * number of keys whose string value is unknown:
 * number of keys that resolve to multiple strings:
 * number of keys that resolve to one string:
 * 
 * In a separate output file, list of strings used as keys and number of times
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
    
    // For keys used in putExtra Calls:
    // number of keys whose string value is unknown:
    int putExtraUnknownKey = 0;
    // number of keys that resolve to multiple strings:
    int putExtraMultiKey = 0;
    // number of keys that resolve to one string
    int putExtraOneKey = 0;
    
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
            writer = new FileOutputStream("intentkey.txt");
            write("number of getExtra calls: "+ numGetExtra);

            write("number of putExtra calls: " + numPutExtra);

            write(" For keys used in getExtra Calls: ");
            write(" number of keys whose string value is unknown: " + getExtraUnknownKey);
            write(" number of keys that resolve to multiple strings: " + getExtraMultiKey);
            write(" number of keys that resolve to one string: " + getExtraOneKey);

            write("For keys used in putExtra Calls:");
            write(" number of keys whose string value is unknown: " + putExtraUnknownKey);
            write(" number of keys that resolve to multiple strings: " + putExtraMultiKey);
            write(" number of keys that resolve to one string: " + putExtraOneKey);
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
    private void write(String s){
        try {
            writer.write(s.getBytes());
            writer.write("\n".getBytes());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
   
    @Override
    public void message(Diagnostic.Kind kind, Object source, /**/
            String msgKey, Object... args) {
       if(msgKey.equals("intent.key.error"))
       {
           super.message(kind, source, msgKey, args);
       }
    }
}

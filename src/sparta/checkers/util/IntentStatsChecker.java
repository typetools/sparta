package sparta.checkers.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.processing.SupportedOptions;
import javax.tools.Diagnostic;

import org.checkerframework.checker.linear.qual.Linear;
import org.checkerframework.checker.linear.qual.Normal;
import org.checkerframework.checker.linear.qual.Unusable;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.qual.StubFiles;

import sparta.checkers.FlowChecker;
import sparta.checkers.FlowPolicy;
import sparta.checkers.FlowVisitor;
import sparta.checkers.intents.ComponentMap;

/**
 * 
 * Class to output statistic about intent assignments and putExtras called in 
 * an app.
 * It outputs the number of assignments involving intents, the number of 
 * those assignments which creates an alias, the number of putExtras found in the 
 * app, and the number of consecutive putExtra calls where the receiver intent is sent without
 * any other method calls in between (other than putExtra).
 * 
 * The output is named intent-stats-summary.csv.
 * Format: app directory, # intent assignments, # intent assignments creating alises, 
 * # putExtra calls, # putExtra calls without sideEffect free calls after it.
 * Example: 
/Users/pbsf/src/projects/MyProject/MyProject,2,1,2,1
 * 
 * Call "ant check-intentstats" to use it.
 * 
 * @author pbsf
 *
 */

@SupportedOptions({ FlowPolicy.POLICY_FILE_OPTION, ComponentMap.COMPONENT_MAP_FILE_OPTION,
    FlowChecker.MSG_FILTER_OPTION, FlowVisitor.CHECK_CONDITIONALS_OPTION })
@StubFiles({"receive-send-intent.astub", "put-get-extra.astub", "intent-map.astub"})
public class IntentStatsChecker extends BaseTypeChecker {
    public  FileOutputStream writer;

    private final String OUTPUT_NAME = "intent-stats-summary2.csv";
    //number of putExtra calls:
    int numPutExtra = 0;
    //number of intent assignments
    int numIntentAssignments = 0;
    int putExtraSideEffectFree=0;
    int intentAliasing = 0;

    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new IntentStatsVisitor(this);
    }
    @Override
    public void typeProcessingOver() {
        try {
            if (numPutExtra != 0) {
                writer = new FileOutputStream(OUTPUT_NAME);
                //program,#putExtra,#putExtraSideEffectFree
                String appdir = System.getProperty("user.dir");
                write(appdir);
                write(numIntentAssignments);
                write(intentAliasing);
                write(numPutExtra);
                writeNoComma(putExtraSideEffectFree);
                writenewline();
            }

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
       
        super.typeProcessingOver();
    }

    private void writeNoComma(Object s) {
        try {
            writer.write(s.toString().getBytes());
        } catch (IOException e) {
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
}

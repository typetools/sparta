package sparta.checkers;

/*>>>
import checkers.compilermsgs.quals.CompilerMessageKey;
*/


import checkers.basetype.BaseTypeChecker;
import checkers.quals.PolyAll;
import checkers.quals.StubFiles;
import checkers.quals.TypeQualifiers;
import checkers.source.SupportedLintOptions;
import checkers.util.stub.StubGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;


@TypeQualifiers({ Source.class, Sink.class, PolySource.class, PolySink.class, PolyAll.class })
@StubFiles("information_flow.astub")
@SupportedOptions({ FlowPolicy.POLICY_FILE_OPTION, FlowChecker.MSG_FILTER_OPTION,
        FlowChecker.IGNORE_NOT_REVIEWED })
@SupportedLintOptions({ FlowPolicy.STRICT_CONDITIONALS_OPTION })

public class FlowChecker extends BaseTypeChecker {
    public static final String MSG_FILTER_OPTION = "msgFilter";
    public static final String IGNORE_NOT_REVIEWED = "ignorenr";

    protected Set<String> unfilteredMessages;
    // Methods that are not in a stub file
    protected final Map<String, Map<String, Map<Element, Integer>>> notInStubFile;

    public FlowChecker() {
        super();
        this.notInStubFile = new HashMap<>();
    }

    @Override
    public void initChecker() {
        super.initChecker();

        String unfilteredStr = getOption(MSG_FILTER_OPTION);
        if (unfilteredStr == null) {
            unfilteredMessages = null;
        } else {
            final String[] unfilteredMsgs = unfilteredStr.split(":");
            unfilteredMessages = new HashSet<String>();
            for (final String unfilteredMsg : unfilteredMsgs) {
                if (!unfilteredMsg.trim().isEmpty()) {
                    unfilteredMessages.add(unfilteredMsg.trim());
                }
            }

            if (unfilteredMessages.isEmpty()) {
                unfilteredMessages = null;
            }
        }
    }
    @Override
public void typeProcessingOver() {
        printMethods();
        FlowAnnotatedTypeFactory factory = ((FlowVisitor) visitor).getTypeFactory();
        factory.flowAnalizer.printImpliedFlowsVerbose();
        factory.flowAnalizer.printImpliedFlowsForbidden();
        factory.flowAnalizer.printAllFlows();
    }

    // TODO: would be nice if you could pass a file name
    private final String printMissMethod = "missingAPI.astub";
    // TODO: would be nice if there was a command line argument to turn this on
    // and off
    private final boolean printFrequency = true;

    private void printMethods() {
        if (notInStubFile.isEmpty())
            return;
       
        int methodCount = 0;
        try( PrintStream out = new PrintStream(new File(printMissMethod))) {
            for (String pack : notInStubFile.keySet()) {
                out.println("package " + pack + ";");
                for (String clss : notInStubFile.get(pack).keySet()) {
                    out.println("class " + clss + "{");
                    Map<Element, Integer> map = notInStubFile.get(pack).get(clss);
                    for (Element element : map.keySet()) {
                        StubGenerator stubGen = new StubGenerator(out);
                        if (printFrequency)
                            out.println("    //" + map.get(element) + " (" + element.getSimpleName()
                                    + ")");
                        stubGen.skeletonFromMethod(element);
                        methodCount++;
                    }
                    out.println("}");
                }
            }
            System.err.println(methodCount + " methods to annotate.");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
       
    }

    @Override
    public void message(Diagnostic.Kind kind, Object source, /*@CompilerMessageKey*/
            String msgKey, Object... args) {
        if (unfilteredMessages == null || unfilteredMessages.contains(msgKey)) {
            super.message(kind, source, msgKey, args);
        }
    }
}

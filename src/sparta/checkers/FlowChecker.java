package sparta.checkers;

/*>>>
import org.checkerframework.checker.compilermsgs.qual.CompilerMessageKey;
*/

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.qual.PolyAll;
import org.checkerframework.framework.qual.StubFiles;
import org.checkerframework.framework.qual.TypeQualifiers;
import org.checkerframework.framework.source.SupportedLintOptions;
import org.checkerframework.framework.stub.StubGenerator;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import sparta.checkers.quals.FineSink;
import sparta.checkers.quals.FineSource;


@TypeQualifiers({ Source.class, Sink.class, PolySource.class, PolySink.class, PolyAll.class, FineSource.class, FineSink.class })
@StubFiles("information_flow.astub")
@SupportedOptions({ FlowPolicy.POLICY_FILE_OPTION, FlowChecker.MSG_FILTER_OPTION,
        FlowChecker.IGNORE_NOT_REVIEWED, FlowVisitor.CHECK_CONDITIONALS_OPTION })
@SupportedLintOptions({ FlowPolicy.STRICT_CONDITIONALS_OPTION })

public class FlowChecker extends BaseTypeChecker {
    public static final String SPARTA_OUTPUT_DIR = System.getProperty("user.dir")+File.separator+"sparta-out"+File.separator;
    public static final String MSG_FILTER_OPTION = "msgFilter";
    public static final String IGNORE_NOT_REVIEWED = "ignorenr";
    //Set to true to turn on "pretty" error messages
    private static final boolean PRETTY_PRINT = false;

    protected Set<String> unfilteredMessages;
    // Methods that are not in a stub file
    protected final Map<String, Map<String, Map<Element, Integer>>> notInStubFile;

    public FlowChecker() {
        super();
        this.notInStubFile = new HashMap<>();
    }
    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new FlowVisitor(this);
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
        File outputDir = new File(SPARTA_OUTPUT_DIR);
        if(!outputDir.exists()){
            outputDir.mkdir();
        }
        if (outputDir.exists() && outputDir.isDirectory()) {
            printMethods();
            FlowAnnotatedTypeFactory factory = ((FlowVisitor) visitor).getTypeFactory();
            factory.flowAnalizer.printImpliedFlowsVerbose();
            factory.flowAnalizer.printImpliedFlowsForbidden();
            factory.flowAnalizer.printAllFlows();
            factory.flowAnalizer.printIntentFlowsByComponent();
        }
    }

    // TODO: would be nice if you could pass a file name
    private final String printMissMethod = SPARTA_OUTPUT_DIR+"missingAPI.astub";
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
        if (PRETTY_PRINT) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof String || args[i] instanceof AnnotatedTypeMirror) {
                    args[i] = prettyPrint(args[i].toString());
                }
            }
        }
        if (unfilteredMessages == null || unfilteredMessages.contains(msgKey)) {
            super.message(kind, source, msgKey, args);
        }
       
    }
    /**
     * Converts the toString() representation of AnnotatedTypeMirrors to a flow, ie
     * FlowPermission("param","param"), FlowPermission -> FlowPermission
     * If the annotation mirror has annotations other than Source or Sink, 
     * then this method will return toString() representation.
     */
    private static String prettyPrint(String atmString){
        try {
            char[] atmChars = atmString.toCharArray();
            int parens = 0;
            List<StringBuilder> tokens = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            for (char c : atmChars) {
                builder.append(c);
                if (c == '@') {
                    String trimed = builder.toString().trim();
                    builder = new StringBuilder();
                    builder.append(trimed);
                    if(trimed.length()>1 &&parens == 0){
                        tokens.add(builder.deleteCharAt(builder.length()-1));
                        builder = new StringBuilder("@");
                    }
                } else if (c == '(') {
                    parens++;
                } else if (c == ')') {
                    parens--;
                    if (parens == 0) {
                        int lastI = tokens.size() - 1;
                        if (lastI > -1 && tokens.get(lastI).length()>0 &&tokens.get(lastI).charAt(0)=='@'
                                && builder.length()>0&& builder.charAt(0) == '@') {
                            tokens.get(lastI).append(builder);
                        } else {
                            tokens.add(builder);
                        }
                        builder = new StringBuilder();
                    }
                }
            }
            tokens.add(builder);
            builder = new StringBuilder();

            for (StringBuilder s : tokens) {
                builder.append(convertToFlow(s.toString()));
            }
            return builder.toString();
        } catch (Exception e) {
             return atmString;
        }
    }
 
    private static String convertToFlow(String annoToString) {
        if (!annoToString.contains("@")) return annoToString;

        String[] annos = annoToString.split("@");
        List<String> sinks = new ArrayList<>();
        List<String> sources = new ArrayList<>();
        boolean foundSinkOnce = false;
        boolean foundSourceOnce = false;

        for (String anno : annos) {
            if (anno.contains("FineSink")) {
                sinks.add( prettyPrintFineSourceOrSink(anno));
            } else if (anno.contains("FineSource")) {
                sources.add( prettyPrintFineSourceOrSink(anno));
            }else if (anno.contains("Sink")) {
                if (!foundSinkOnce) {
                    sinks.addAll(prettyPrintSourceOrSink(anno));
                    foundSinkOnce=true;
                } else {
                    // Found @Sink more than once, probably dealing with AET, so give up
                    return annoToString;
                }
            } else if (anno.contains("Source")) {
                if (!foundSourceOnce) {
                    sources.addAll(prettyPrintSourceOrSink(anno));
                    foundSourceOnce = true;
                } else {
                    // Found @Source more than once, probably dealing with AET, so
                    // give up
                    return annoToString;
                }
            }  
        }
        String sourcesString = sources.toString().trim();
        if (sourcesString.equals(""))
            sourcesString = "{}";
        String sinksString = sinks.toString().trim();
        if (sinksString.equals(""))
            sinksString = "{}";
        String returnS = sourcesString + " -> " + sinksString;
        return " {"+returnS.replace('[', ' ').replace(']', ' ')+"} ";
    }

    /**
     * Returns the list of FlowPermissions in the Source or Sink annotation.
     * 
     * @param anno
     * @return
     */
    private static List<String> prettyPrintSourceOrSink(String anno) {
        String[] possiblesinks = anno.split(",");
        List<String> sinks = new ArrayList<>();
        for (String sink : possiblesinks) {
            FlowPermission f = getFlowPermission(sink);
            if (f != null) {
                sinks.add(f.name());
            }
        }
        return sinks;
    }

    /**
     * Returns a string that contains the flow permission in the
     * FineSource or FineSink annotation and the parameters.
     * (If the parameter is only *, it is not printed)
     * If the FlowPermisison or parameters are not found the input 
     * string is returned. 
     * @param input
     * @return
     */
    private static String prettyPrintFineSourceOrSink(String input) {
        Pattern fin = Pattern.compile(".*params=\\{(.*)\\}.*");
        Matcher mat = fin.matcher(input);
        if (mat.matches()) {
            String param = mat.group(1);
            param = param.substring(0, param.lastIndexOf('\"') + 1);
            FlowPermission f = getFlowPermission(input);
            if(f == null){
                return input;
            }
            if(param.equals("\"*\""))
                return f.name();
            return f+"(" + param + ")";       
        }
        return input;
    }
    /**
     * If the input string contains a FlowPermission, that FlowPermission is returned.
     * Otherwise null is return
     * @param input String to convert to FlowPermission
     * @return FlowPermission that the input string contains, or null.
     */
    private static FlowPermission getFlowPermission(String input) {
        for (FlowPermission f : FlowPermission.values()) {
            if (input.contains(f.name())) {
                return f;
            }
        }
        return null;
    }
}

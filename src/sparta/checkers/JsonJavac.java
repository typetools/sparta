package sparta.checkers;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.google.gson.Gson;


/* Data classes that contain the information that will
 * be output as JSON.
 * For each report kind we introduce a separate Data subtype.
 *
 * The "datakind" field should be defined in every class to
 * distinguish what kind of node it is.
 * The "datakind" field must not be static, as it would not be
 * shown in JSON then.
 */
abstract class Data {
    // Class information to distinguish sub-classes in JSON.
    final String datakind = this.getClass().getCanonicalName();
    // The file from which the message originated.
    String filename;
    // The line number within the file.
    long line;

    @Override
    public String toString() {
        return "file: " + filename + ":" + line;
    }
}
class ReadWriteData extends Data {
    String part;
}
class WriteData extends Data {
    String part;
}
class CallData extends Data {
    String part;
}
class NewData extends Data {
    String part;
}
class InheritData extends Data {
    String part;
}
class OverrideData extends Data {
    String part;
}
class UseData extends Data {
    // The name of the used Element.
    String useof;
    // The kind of the used Element.
    String useofkind;
    // The name of the Element that used "useof".
    String useby;
    // The kind of the Element that used "useof".
    String usebykind;

    @Override
    public String toString() {
    	return super.toString() + "\n    useof: " + useof + " (" + useofkind +
    			")\n    useby: " + useby + " (" + usebykind + ")";
    }
}
class FlowData extends Data {
    // The Tree at which the flow occurred.
    String tree;
    // The kind of Tree at which the flow occurred.
    String kind;
    // The flow sources. Either "NONE" or an array of
    // FlowSources.FlowSource enum constants, e.g.
    // [[sparta.checkers.quals.FlowSources.FlowSource.ANY]
    String sources;
    // The flow sinks. Either "NONE" or an array of
    // FlowSinks.FlowSink enum constants.
    String sinks;
}

/**
 * This tool converts the diagnostic messages of the {@link AndroidReportChecker}
 * into first JSON representation.
 * The Java source files to compile are given as only command-line arguments.
 *
 * @author wmdietl
 */
public abstract class JsonJavac {
	abstract String getProcessorName();
    public void run(String[] args) {
        List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(args);
        // System.out.println("Here: " + diagnostics);
        // Filter the output and print the JSON results.
        print(filter(diagnostics));
    }

    protected List<Diagnostic<? extends JavaFileObject>> compile(String[] args) {
        String cpath = System.getenv("CLASSPATH");

        String[] compArgs = new String[] {"-Xbootclasspath/p:" + cpath,
                "-processor", this.getProcessorName(),
                "-proc:only", // don't compile classes to save time
                "-encoding", "ISO8859-1", // TODO: needed for JabRef only, make optional
                "-Xmaxwarns", "100000",
                "-AprintErrorStack",
                "-Awarns"};

        // Non-diagnostic compiler output will end up here.
        StringWriter javacoutput = new StringWriter();
        // The nuggets: the diagnostic messages from the compiler.
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> files = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(args));

        JavaCompiler.CompilationTask task = compiler.getTask(javacoutput, fileManager,
                  diagnostics, Arrays.asList(compArgs), null, files);

        // Run the compiler.
        task.call();

        // Just in case there are other messages. Put into error stream to allow separation.
        if (!javacoutput.toString().isEmpty()) {
            System.err.println("javac output: " + javacoutput);
        }

        return diagnostics.getDiagnostics();
    }

    /**
     * Go through the provided diagnostics, parse the message, and create
     * the a list of Data objects.
     *
     * @param diagnostics
     */
    protected List<Data> filter(
            List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        List<Data> datas = new LinkedList<Data>();

        for (Diagnostic<? extends JavaFileObject> diag : diagnostics) {
            // System.out.println("Looking at: " + diag);

            String msg = diag.getMessage(null);
            Data data = null;
            String key = msg.substring(0, msg.indexOf(' '));

            switch (key) {
            case "READWRITE": {
                Matcher match = READWRITE_Pattern.matcher(msg);
                if (match.matches()) {
                    data = new ReadWriteData();
                    ((ReadWriteData)data).part = match.group(1);
                }
                break;
            }
            case "WRITE": {
                Matcher match = WRITE_Pattern.matcher(msg);
                if (match.matches()) {
                    data = new WriteData();
                    ((WriteData)data).part = match.group(1);
                }
                break;
            }
            case "CALL": {
                Matcher match = CALL_Pattern.matcher(msg);
                if (match.matches()) {
                    data = new CallData();
                    ((CallData)data).part = match.group(1);
                }
                break;
            }
            case "NEW": {
                Matcher match = NEW_Pattern.matcher(msg);
                if (match.matches()) {
                    data = new NewData();
                    ((NewData)data).part = match.group(1);
                }
                break;
            }
            case "INHERIT": {
                Matcher match = INHERIT_Pattern.matcher(msg);
                if (match.matches()) {
                    data = new InheritData();
                    ((InheritData)data).part = match.group(1);
                }
                break;
            }
            case "OVERRIDE": {
                Matcher match = OVERRIDE_Pattern.matcher(msg);
                if (match.matches()) {
                    data = new OverrideData();
                    ((OverrideData)data).part = match.group(1);
                }
                break;
            }
            case "USEOF": {
                Matcher match = USE_Pattern.matcher(msg);
                if (match.matches()) {
                    UseData usedata = new UseData();
                    usedata.useof = match.group(1);
                    usedata.useofkind = match.group(2);
                    usedata.useby = match.group(3);
                    usedata.usebykind = match.group(4);
                    data = usedata;
                }
                break;
            }
            case "FLOW": {
                Matcher match = FLOW_Pattern.matcher(msg);
                if (match.matches()) {
                    FlowData flowdata = new FlowData();
                    flowdata.tree = match.group(1);
                    flowdata.kind = match.group(2);
                    flowdata.sources = match.group(3);
                    flowdata.sinks = match.group(4);
                    data = flowdata;
                }
                break;
            }
            }

            if (data!=null) {
                data.filename = diag.getSource().getName();
                data.line = diag.getLineNumber();
                datas.add(data);
            }
        }

        return datas;
    }

    abstract protected void print(List<Data> datas);

    /*
     * The following fields are the regular expression strings and their respecitve
     * compiled patterns for the diagnostic messages.
     * Also see file json-report-messages.properties for the strings.
     */
    private final static String READWRITE_String = "READWRITE (.*)";
    private final static Pattern READWRITE_Pattern = Pattern.compile(READWRITE_String);

    private final static String WRITE_String = "WRITE (.*)";
    private final static Pattern WRITE_Pattern = Pattern.compile(WRITE_String);

    private final static String CALL_String = "CALL (.*)";
    private final static Pattern CALL_Pattern = Pattern.compile(CALL_String);

    private final static String NEW_String = "NEW (.*)";
    private final static Pattern NEW_Pattern = Pattern.compile(NEW_String);

    private final static String INHERIT_String = "INHERIT (.*)";
    private final static Pattern INHERIT_Pattern = Pattern.compile(INHERIT_String);

    private final static String OVERRIDE_String = "OVERRIDE (.*)";
    private final static Pattern OVERRIDE_Pattern = Pattern.compile(OVERRIDE_String);

    private final static String USE_String = "USEOF (.*) OFKIND (.*) USEBY (.*) BYKIND (.*)";
    private final static Pattern USE_Pattern = Pattern.compile(USE_String);

    private final static String FLOW_String = "FLOW TREE (.*) KIND (.*) SOURCES (.*) SINKS (.*)";
    private final static Pattern FLOW_Pattern = Pattern.compile(FLOW_String);

    public static abstract class JsonPrint extends JsonJavac {
        protected void print(List<Data> datas) {
            Gson gson = new Gson();
            String json = gson.toJson(datas);
            System.out.println(json);
        }
    }

    // TODO: move more of the classes/methods into the subclasses.
    public static class FlowShowJson extends JsonPrint {
        public static void main(String[] args) {
            new FlowShowJson().run(args);
        }

        @Override
        String getProcessorName() {
            return sparta.checkers.FlowShow.class.getCanonicalName();
        }
    }

    public static class ReportUsageJson extends JsonPrint {
        public static void main(String[] args) {
            new ReportUsageJson().run(args);
        }

        @Override
        String getProcessorName() {
            return sparta.checkers.AndroidReportChecker.class.getCanonicalName();
        }
    }

    public static class ReportUsageText extends JsonJavac {
        public static void main(String[] args) {
            new ReportUsageText().run(args);
        }

        @Override
        String getProcessorName() {
            return sparta.checkers.AndroidReportChecker.class.getCanonicalName();
        }

        // From API name to the set of Data
        Map<String, Set<UseData>> uses = new HashMap<>();

        protected void print(List<Data> datas) {
            for (Data d: datas) {
                if (d instanceof UseData) {
                    addMapping(uses, ((UseData) d).useby, (UseData) d);
                }
            }

            StringBuilder result = new StringBuilder();
            Set<String> keys = uses.keySet();
            List<String> keyl = new LinkedList<String>();
            keyl.addAll(keys);
            Collections.sort(keyl);

            for (String key : keyl) {
                result.append(key + ": " + uses.get(key).size() + "\n");
            }
            System.out.println(result);
        }
    }

    <S, T> void addMapping(Map<T, Set<S>> map, T key, S data) {
        if (map.containsKey(key)) {
            Set<S> prev = map.get(key);
            prev.add(data);
        } else {
            HashSet<S> newset = new HashSet<S>();
            newset.add(data);
            map.put(key, newset);
        }
    }

    public static class FlowShowText extends JsonJavac {
        public static void main(String[] args) {
            new ReportUsageText().run(args);
        }

        @Override
        String getProcessorName() {
            return sparta.checkers.AndroidReportChecker.class.getCanonicalName();
        }

        Map<String, Set<FlowData>> sources = new HashMap<>();
        Map<String, Set<FlowData>> sinks = new HashMap<>();

        protected void print(List<Data> datas) {
            StringBuilder result = new StringBuilder();
            System.out.println(result);
        }
    }

}
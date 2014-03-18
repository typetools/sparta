package sparta.checkers;

import static sparta.checkers.quals.FlowPermission.NOT_REVIEWED;

import org.checkerframework.framework.qual.PolyAll;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

import org.checkerframework.javacutil.Pair;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.AnnotationMirror;

import sparta.checkers.quals.ParameterizedFlowPermission;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;

/*>>>
 import org.checkerframework.checker.nullness.qual.Nullable;
 */

/**
 *
 */
public class FlowPolicy {

    // The name of the command line option to specify a policyFile
    // A -A will precede this name on the command line ( e.g.
    // -ApolicyFile=/path/to/file )
    public static final String POLICY_FILE_OPTION = "flowPolicy";
    public static final String STRICT_CONDITIONALS_OPTION = "strict-conditional";

    public static final String EMPTY = "{}";
    public static final String EMPTY_REGEX = "\\{\\}";

    private final Map<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> allowedSourceToSinks;
    private final Map<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> allowedSinkToSources;

    private final/*@Nullable*/Set<ParameterizedFlowPermission> sinksFromAnySource;

    // True: LITERAL->CONDITIONAL is added,
    // False: ANY->CONDITIONAL is added
    private final boolean strictConditionals;
    
    private final ParameterizedFlowPermission ANY;
    private final ParameterizedFlowPermission CONDITIONAL;
    private final ParameterizedFlowPermission LITERAL;
    
    public FlowPolicy(final Map<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> allowedFlows) {
        ANY = new ParameterizedFlowPermission(FlowPermission.ANY);
        CONDITIONAL = new ParameterizedFlowPermission(FlowPermission.CONDITIONAL);
        LITERAL = new ParameterizedFlowPermission(FlowPermission.LITERAL);
        
        this.allowedSourceToSinks = allowedFlows;
        this.allowedSinkToSources = reverse(allowedFlows);
        
        this.sinksFromAnySource = allowedFlows.get(ANY);
        this.strictConditionals = false;
    }

    /**
     *
     * @param flowPolicyFile
     * @param strictConditionals
     *            if true LITERAL->CONDITIONAL is added, otherwise
     *            ANY->CONDITIONAL is added
     */
    public FlowPolicy(final File flowPolicyFile, boolean strictConditionals) {
        ANY = new ParameterizedFlowPermission(FlowPermission.ANY);
        CONDITIONAL = new ParameterizedFlowPermission(FlowPermission.CONDITIONAL);
        LITERAL = new ParameterizedFlowPermission(FlowPermission.LITERAL);
        
        this.strictConditionals = strictConditionals;
        this.allowedSourceToSinks = getDefaultAllowedFlows();
        if (flowPolicyFile != null && flowPolicyFile.exists()) {
            readPolicyFile(flowPolicyFile);
        }

        this.sinksFromAnySource = allowedSourceToSinks.get(ANY);
        this.allowedSinkToSources = reverse(allowedSourceToSinks);
        checkForTransitivity();

    }

    public FlowPolicy(final File flowPolicyFile) {
        this(flowPolicyFile, false);
    }

    public FlowPolicy() {
        this(false);
    }

    /**
     *
     * @param strictConditionals
     *            if true LITERAL->CONDITIONAL is added, otherwise
     *            ANY->CONDITIONAL is added
     */
    public FlowPolicy(boolean strictConditionals) {
        this(null, strictConditionals);
    }

    private HashMap<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> getDefaultAllowedFlows() {
        HashMap<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> defaultAllowedFlows = new HashMap<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>>();
        TreeSet<ParameterizedFlowPermission> sinkSet = new TreeSet<ParameterizedFlowPermission>();
        sinkSet.add(CONDITIONAL);

        if (strictConditionals) {
            defaultAllowedFlows.put(LITERAL, sinkSet);
        } else {
            defaultAllowedFlows.put(ANY, sinkSet);
        }

        return defaultAllowedFlows;
    }

    public static Pair<Set<ParameterizedFlowPermission>, Set<ParameterizedFlowPermission>> annotatedTypeMirrorToFlows(
            final AnnotatedTypeMirror atm) {

        final Set<ParameterizedFlowPermission> sources = new TreeSet<>( Flow.getSources(atm));
        final Set<ParameterizedFlowPermission> sinks = new TreeSet<>( Flow.getSinks(atm));

        return Pair.of(sources, sinks);
    }

    public boolean areFlowsAllowed(final AnnotatedTypeMirror atm) {
        final AnnotationMirror polySourceAnno = atm.getAnnotation(PolySource.class);
        final AnnotationMirror polySinkAnno = atm.getAnnotation(PolySink.class);
        final AnnotationMirror polyAllAnno = atm.getAnnotation(PolyAll.class);
        // If the type is marked with poly flow source or poly flow sink,
        // Then the flow is allowed.
        if (polySinkAnno != null || polySourceAnno != null || polyAllAnno != null) {
            return true;
        }

        return areFlowsAllowed(annotatedTypeMirrorToFlows(atm));
    }

    public List<Flow> forbiddenFlows(final AnnotatedTypeMirror atm) {
        return forbiddenFlows(annotatedTypeMirrorToFlows(atm));
    }

    public List<Flow> forbiddenFlows(final Pair<Set<ParameterizedFlowPermission>, Set<ParameterizedFlowPermission>> flows) {
        final Set<ParameterizedFlowPermission> sources = flows.first;
        final Set<ParameterizedFlowPermission> sinks = flows.second;
        List<Flow> forflows = new ArrayList<Flow>();

        if (sources.isEmpty() || sinks.isEmpty()) {
            forflows.add(new Flow(sources, sinks));
            return forflows;
        }

        if (sinksFromAnySource != null) {
            sinks.removeAll(sinksFromAnySource);
        }

        for (final ParameterizedFlowPermission source : sources) {
            final Set<ParameterizedFlowPermission> allowedSink = allowedSourceToSinks.get(source);

            if (allowedSink == null) {
                forflows.add(new Flow(source, sinks));
            } else if (allowedSink.contains(ANY)) {
                // Then source->ANY is allowed
            } else if (!(allowedSink.containsAll(sinks))) {
                Flow flow = new Flow(source);
                for (ParameterizedFlowPermission sink : sinks) {
                    if (!allowedSink.contains(sink)) {
                        flow.addSink(sink);
                    }
                }
                if (flow.hasSink()) {
                    forflows.add(flow);
                }
            }
        }

        return forflows;
    }

    public boolean areFlowsAllowed(final Pair<Set<ParameterizedFlowPermission>, Set<ParameterizedFlowPermission>> flows) {
        final Set<ParameterizedFlowPermission> sources = new TreeSet<>(flows.first);
        final Set<ParameterizedFlowPermission> sinks =  new TreeSet<>(flows.second);

        if (sources.isEmpty() || sinks.isEmpty()) {
            return false;
        }

        if (sinksFromAnySource != null) {
            sinks.removeAll(sinksFromAnySource);
            if (sinks.isEmpty()) {
                return true;
            }
        }

        // TODO: Also check for parameters
        for (final ParameterizedFlowPermission source : sources) {
            final Set<ParameterizedFlowPermission> allowedSinks = allowedSourceToSinks.get(source);

            if (allowedSinks == null) {
                return false;
            }
            
            if (allowedSinks.contains(ANY)) {
                return true;
            }
            
            for (ParameterizedFlowPermission sink : sinks) {
                if (!FlowAnnotatedTypeFactory.isMatchInSet(sink, allowedSinks)) {
                    return false;
                }
            }
            
            // !(allowedSinks.contains(ANY) || allowedSinks.containsAll(sinks))) 
        }

        return true;
    }

    /**
     * If # appears in the line remove all text from # to the end of the string
     * trim the string (this is important in order to recognize empty lines)
     * else return the original line
     *
     * @param input
     *            Line that may contain a comment
     * @return Line that does not contain a comment
     */
    public static String stripComment(final String input) {
        int commentIndex = input.indexOf('#');

        final String out;
        if (commentIndex > -1) {
            if (commentIndex == 0) {
                out = "";
            } else {
                out = input.substring(0, commentIndex).trim();
            }
        } else {
            out = input;
        }

        return out;
    }

    public Set<ParameterizedFlowPermission> getIntersectionAllowedSinks(final Set<ParameterizedFlowPermission> sources) {
        //Start with all sinks and remove those that are not allowed
        Set<ParameterizedFlowPermission> sinks =  Flow.getSetOfAllSinks();

        for (ParameterizedFlowPermission source : sources) {
            final Set<ParameterizedFlowPermission> curSinks = allowedSourceToSinks.get(source);
            sinks = Flow.intersectSinks(sinks, curSinks);
        }
        sinks.addAll(getSinkFromSource(ANY, false));
        return sinks;
    }

    public Set<ParameterizedFlowPermission> getIntersectionAllowedSources(final /* Collection?? */ Collection<ParameterizedFlowPermission> sinks) {
        Set<ParameterizedFlowPermission> sources = Flow.getSetOfAllSources();

        for (ParameterizedFlowPermission sink : sinks) {
            final Set<ParameterizedFlowPermission> curSources = allowedSinkToSources.get(sink);
            sources = Flow.intersectSources(sources, curSources);
        }
        sources.addAll(getSourceFromSink(ANY, false));
        return sources;
    }

    /**
     * Read the given file return a one to many Map of FlowPermission -> Sink
     * where each entry indicates what sinks a source is given blanket access to
     * reach
     *
     *
     * Format: A flow policy file is read line by line where each line has the
     * following format FlowPermissionName -> FlowPermissionName,
     * FlowPermissionName, FlowPermissionName
     *
     * FlowPermissionName = One of the names of the enums in FlowPermission
     * FlowPermissionName<x> = One of the names of the enums in FlowPermission
     *
     * A source can appear twice, the output Sink for that given source will
     * contain the union of the two entries.
     *
     * E.g. MICROPHONE -> NETWORK, TEXT_MESSAGE MICROPHONE -> FILESYSTEM
     * TEXT_MESSAGE -> NETWORK
     *
     * In this case there would be the following entry in the Map
     * Key(MICROPHONE) => Entries(NETWORK, TEXT_MESSAGE, FILESYSTEM)
     * Key(TEXT_MESSAGE) => Entries(NETWORK)
     *
     * Comments are permitted and start with a # Blank lines are ignored (as is
     * white space between symbols) A runtime error wallowedSourceToSinksill be thrown if ANY line
     * is malformed (All errors will be reported via standard out before hand)
     *
     * @param policyFile
     *            A file formatted as above
     * @return
     */
    private void readPolicyFile(final File policyFile) {
        final Pattern linePattern = Pattern.compile("^\\s*((?:[^->]+))->\\s*((?:[^->]+)(?:,[^->]+)*)\\s*$");

        final List<String> errors = new ArrayList<String>();
        
        final List<FlowPermission> allCoarseSinkButAny = Arrays.asList(FlowPermission.values());
        final Set<ParameterizedFlowPermission> allSinkButAny = new TreeSet<ParameterizedFlowPermission>();

        for (FlowPermission sink : allCoarseSinkButAny) {
            if (sink != FlowPermission.ANY) {
                allSinkButAny.add(new ParameterizedFlowPermission(sink));
            }
        }
        

        BufferedReader bufferedReader = null;
        try {
            int lineNum = 1;
            bufferedReader = new BufferedReader(new FileReader(policyFile));
            String originalLine = bufferedReader.readLine().trim();

            while (originalLine != null) {

                // Remove anything from # on in the line
                final String line = stripComment(originalLine);

                if (!line.isEmpty() && !isWhiteSpaceLine(line)) {
                    final Matcher matcher = linePattern.matcher(line);

                    Set<ParameterizedFlowPermission> sinks;
                    if (matcher.matches()) {
                        final String parameterizedFlowRegex = "(.*)[(\"](.*)[\")]";
                        String sourceStr = matcher.group(1).trim();
                        String groupmatcher = matcher.group(2).trim();
                        final String[] sinkStrs = groupmatcher.split(",(?![^(]*\\))");
                        
                        List<String> sourceParams = new ArrayList<String>();
                        List<List<String>> sinkParams = new ArrayList<List<String>>();
                        
                        if (sourceStr.matches(parameterizedFlowRegex)) {
                            String sourceParameterString = sourceStr.substring(sourceStr.indexOf('(') + 1, sourceStr.indexOf(')')).trim();
                            sourceStr = sourceStr.substring(0, sourceStr.indexOf('(')).trim();
                            
                            // Save source parameters
                            String[] sourceParameterStrings = sourceParameterString.split(",");
                            for (String param : sourceParameterStrings) {
                                // Strip quotes and add to parameter list
                                param = param.substring(param.indexOf('\"') + 1);
                                param = param.substring(0, param.indexOf('\"'));
                                param = param.trim();
                                sourceParams.add(param);
                            }
                        }
                             
                        // Create source object
                        ParameterizedFlowPermission sourceObject = new ParameterizedFlowPermission(FlowPermission.valueOf(sourceStr.trim()), sourceParams);
                                             
                        for (int i = 0; i < sinkStrs.length; i++) {         
                            List<String> currentSinkParams = new ArrayList<String>();
                            sinkParams.add(currentSinkParams);
                            if (sinkStrs[i].matches(parameterizedFlowRegex)) { 
                                String currentSinkStr = sinkStrs[i].trim();
                                String sinkParameterString = currentSinkStr.substring(currentSinkStr.indexOf('(') + 1, currentSinkStr.indexOf(')'));
                                sinkStrs[i] = currentSinkStr.substring(0, currentSinkStr.indexOf('(')).trim();
                                
                                // Save sink parameters
                                String[] sinkParameterStrings = sinkParameterString.split(",");
                                for (String param: sinkParameterStrings) {
                                    // Strip quotes and add to parameter list
                                    param = param.substring(param.indexOf('\"') + 1);
                                    param = param.substring(0, param.indexOf('\"'));
                                    param = param.trim();
                                    currentSinkParams.add(param);
                                }
                            }
                        }
                        
                        // Create sink objects
                        List<ParameterizedFlowPermission> sinkObjects = new ArrayList<ParameterizedFlowPermission>();
                        for (int i = 0; i < sinkStrs.length; i++) {   
                            String sinkStr = sinkStrs[i];
                            List<String> sinkParameters = sinkParams.get(i);
                            sinkObjects.add(new ParameterizedFlowPermission(FlowPermission.valueOf(sinkStr.trim()), sinkParameters));
                        }
                      
                        FlowPermission source = null;
                        boolean skip = false;

                        if (sourceStr.equals(EMPTY) || sourceStr.equals(NOT_REVIEWED.toString())) {
                            errors.add(formatPolicyFileError(policyFile, lineNum, "FlowPermission "
                                    + sourceStr + " is not allowed in policy files", originalLine));
                            sinks = null;
                            skip = true;

                        } else {
                            try {
                                boolean foundInMap = false;
                                sinks = null;
                                
                                for (ParameterizedFlowPermission allowedSource : allowedSourceToSinks.keySet()) {
                                    if (allowedSource.getPermission() == sourceObject.getPermission()) { // already in map
                                        foundInMap = true;
                                        sinks = allowedSourceToSinks.get(allowedSource);
                                        break;
                                    }
                                }

                                if (!foundInMap) {
                                    sinks = new TreeSet<ParameterizedFlowPermission>();
                                    allowedSourceToSinks.put(sourceObject, sinks);
                                }
                            } catch (final IllegalArgumentException iaExc) {

                                errors.add(formatPolicyFileError(policyFile, lineNum,
                                        "Unrecognized source: " + sourceStr + " Known sources: "
                                                + enumValuesToString(FlowPermission.values()),
                                        originalLine));

                                sinks = null;
                                skip = true;
                            }

                        }
                        
                        for (final String sink : sinkStrs) {
                            if (sink.equals(EMPTY) || sink.equals(NOT_REVIEWED.toString())) {
                                errors.add(formatPolicyFileError(policyFile, lineNum,
                                        "FlowPermission " + sinkStrs
                                                + " is not allowed in policy files", originalLine));
                                continue;
                            }
                            try {
                                if (!skip) {
                                    // Get the front of the current sink objects list
                                    sinks.add(sinkObjects.remove(0));
                                }

                            } catch (final IllegalArgumentException iaExc) {
                                errors.add(formatPolicyFileError(policyFile, lineNum,
                                        "Unrecognized sink: " + sink + " Known sinks: "
                                                + enumValuesToString(FlowPermission.values()),
                                        originalLine));
                            }
                        }

                        if (!skip && sinks.containsAll(allSinkButAny)) {
                            sinks.clear();
                            sinks.add(ANY);
                        }

                    } else {
                        errors.add(formatPolicyFileError(
                                policyFile,
                                lineNum,
                                "Syntax error, Lines are of the form: flowSource -> sink1, sink2, ..., sinkN ",
                                originalLine));
                    }
                }

                ++lineNum;
                originalLine = bufferedReader.readLine();
            }
          //  checkForTransitivity();
        } catch (final IOException ioExc) {
            throw new RuntimeException(ioExc);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException ignoredCloseExc) {
            }
        }

        if (!errors.isEmpty()) {
            System.out.println("\nErrors parsing policy file:");
            for (final String error : errors) {
                System.out.println(error);
                System.out.println();
            }

            System.out.flush();
            throw new RuntimeException("Errors parsing policy file: "
                    + policyFile.getAbsolutePath());
        }
        
    }

    private void checkForTransitivity() {
      for(ParameterizedFlowPermission source : allowedSourceToSinks.keySet()){
          if(source.isSink()){
              //if the source can be a sink too, then there might be a transitive flow
              //TODO: handle what about WRITE_EXTERNAL_FILESYSTEM vs READ_EXTERNAL_FILESYSTEM
                if (allowedSinkToSources.containsKey(source)) {
                    Set<ParameterizedFlowPermission> sources = allowedSinkToSources.get(source);
                    Set<ParameterizedFlowPermission> sinks = allowedSourceToSinks.get(source);
                    Pair<Set<ParameterizedFlowPermission>, Set<ParameterizedFlowPermission>> flows = Pair.of(sources, sinks);
                    if (!areFlowsAllowed(flows)) {
                        System.err.flush();
                        System.err.println("Warning, flow policy has transtive flow\n"
                                + allowedSinkToSources.get(source) + "->"
                                + allowedSourceToSinks.get(source));
                        System.err.flush();
                    }
                }
          }
      }
    }

    private static final Pattern WHITE_SPACE_PATTERN = Pattern.compile("^\\s*$");

    public static boolean isWhiteSpaceLine(final String line) {
        return WHITE_SPACE_PATTERN.matcher(line).matches();
    }

    public static String enumValuesToString(final Enum<?>[] enumValues) {

        String str = "";
        boolean comma = false;
        for (final Enum<?> ev : enumValues) {
            if (comma) {
                str += ", ";
            } else {
                comma = true;
            }

            str += ev.name();
        }

        return str;
    }

    private static String formatPolicyFileError(final File file, final int lineNum,
            final String message, final String line) {
        return file.getAbsolutePath() + ":" + lineNum + ": " + message + "\n" + line;
    }

    public Set<ParameterizedFlowPermission> getSinkFromSource(final ParameterizedFlowPermission source, boolean includeAny) {
        return Flow.convertToAnySink(getSet(source, allowedSourceToSinks, includeAny), true);
    }

    public Set<ParameterizedFlowPermission> getSourceFromSink(final ParameterizedFlowPermission sink, boolean includeAny) {
        return Flow.convertToAnySource(getSet(sink, allowedSinkToSources, includeAny), true);

    }
    
    private Set<ParameterizedFlowPermission> getSet(
            final ParameterizedFlowPermission key, final Map<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> data, boolean includeAny) {
        Set<ParameterizedFlowPermission> values = data.get(key);
        if (includeAny) {
            final ParameterizedFlowPermission any = ANY;
            Set<ParameterizedFlowPermission> results = new TreeSet<ParameterizedFlowPermission>();
        if (values != null) {
            results.addAll(values);
        }
        results.addAll(getSet( any, data, false));
        return results;
        } else {
            if (values == null) {
                values = new TreeSet<ParameterizedFlowPermission>();
            }
            return Collections.unmodifiableSet(values);
        }
    }

    private Map<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> reverse (final Map<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> mapToReverse) {
        final Map<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> reversed = new HashMap<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>>();
        for (final Map.Entry<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> keyToValues : mapToReverse.entrySet()) {
            ParameterizedFlowPermission valueInRevese = keyToValues.getKey();
            
            for (ParameterizedFlowPermission keyInReverse : keyToValues.getValue()) {
                Set<ParameterizedFlowPermission> newValues = reversed.get(keyInReverse);
                if (newValues == null) {
                    newValues = new TreeSet<ParameterizedFlowPermission>();
                    reversed.put(keyInReverse, newValues);
                }
                newValues.add(valueInRevese);
            }
        }
        return reversed;
    }
}

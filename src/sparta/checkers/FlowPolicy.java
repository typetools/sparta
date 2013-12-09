package sparta.checkers;

import static sparta.checkers.quals.CoarseFlowPermission.NOT_REVIEWED;

import checkers.quals.PolyAll;
import checkers.types.AnnotatedTypeMirror;

import javacutils.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.AnnotationMirror;

import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.CoarseFlowPermission;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;

/*>>>
 import checkers.nullness.quals.Nullable;
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

    private final Map<FlowPermission, Set<FlowPermission>> allowedSourceToSinks;
    private final Map<FlowPermission, Set<FlowPermission>> allowedSinkToSources;

    private final/*@Nullable*/Set<FlowPermission> sinksFromAnySource;

    // True: LITERAL->CONDITIONAL is added,
    // False: ANY->CONDITIONAL is added
    private final boolean strictConditionals;
    
    private final FlowPermission ANY;
    private final FlowPermission CONDITIONAL;
    private final FlowPermission LITERAL;
    
    public FlowPolicy(final Map<FlowPermission, Set<FlowPermission>> allowedFlows) {
        ANY = new FlowPermission(CoarseFlowPermission.ANY);
        CONDITIONAL = new FlowPermission(CoarseFlowPermission.CONDITIONAL);
        LITERAL = new FlowPermission(CoarseFlowPermission.LITERAL);
        
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
        ANY = new FlowPermission(CoarseFlowPermission.ANY);
        CONDITIONAL = new FlowPermission(CoarseFlowPermission.CONDITIONAL);
        LITERAL = new FlowPermission(CoarseFlowPermission.LITERAL);
        
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

    private HashMap<FlowPermission, Set<FlowPermission>> getDefaultAllowedFlows() {
        HashMap<FlowPermission, Set<FlowPermission>> defaultAllowedFlows = new HashMap<FlowPermission, Set<FlowPermission>>();
        TreeSet<FlowPermission> sinkSet = new TreeSet<FlowPermission>();
        sinkSet.add(CONDITIONAL);

        if (strictConditionals) {
            defaultAllowedFlows.put(LITERAL, sinkSet);
        } else {
            defaultAllowedFlows.put(ANY, sinkSet);
        }

        return defaultAllowedFlows;
    }

    public static Pair<Set<FlowPermission>, Set<FlowPermission>> annotatedTypeMirrorToFlows(
            final AnnotatedTypeMirror atm) {

        final Set<FlowPermission> sources = Flow.getSources(atm);
        final Set<FlowPermission> sinks = Flow.getSinks(atm);

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

    public List<Flow> forbiddenFlows(final Pair<Set<FlowPermission>, Set<FlowPermission>> flows) {
        final Set<FlowPermission> sources = flows.first;
        final Set<FlowPermission> sinks = flows.second;
        List<Flow> forflows = new ArrayList<Flow>();

        if (sources.isEmpty() || sinks.isEmpty()) {
            forflows.add(new Flow(sources, sinks));
            return forflows;
        }

        if (sinksFromAnySource != null) {
            sinks.removeAll(sinksFromAnySource);
        }

        for (final FlowPermission source : sources) {
            final Set<FlowPermission> allowedSink = allowedSourceToSinks.get(source);

            if (allowedSink == null) {
                forflows.add(new Flow(source, sinks));
            } else if (allowedSink.contains(ANY)) {
                // Then source->ANY is allowed
            } else if (!(allowedSink.containsAll(sinks))) {
                Flow flow = new Flow(source);
                for (FlowPermission sink : sinks) {
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

    public boolean areFlowsAllowed(final Pair<Set<FlowPermission>, Set<FlowPermission>> flows) {
        final Set<FlowPermission> sources = flows.first;
        final Set<FlowPermission> sinks = flows.second;

        if (sources.isEmpty() || sinks.isEmpty()) {
            return false;
        }

        if (sinksFromAnySource != null) {
            sinks.removeAll(sinksFromAnySource);
            if (sinks.isEmpty()) {
                return true;
            }
        }


        for (final FlowPermission source : sources) {
            final Set<FlowPermission> allowedSink = allowedSourceToSinks.get(source);

            if (allowedSink == null
                    || !(allowedSink.contains(ANY) || allowedSink.containsAll(sinks))) {
                return false;
            }
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

    public Set<FlowPermission> getIntersectionAllowedSinks(final Set<FlowPermission> sources) {
        //Start with all sinks and remove those that are not allowed
        Set<FlowPermission> sinks =  Flow.getSetOfAllSinks();

        for (FlowPermission source : sources) {
            final Set<FlowPermission> curSinks = allowedSourceToSinks.get(source);
            sinks = Flow.intersectSinks(sinks, curSinks);
        }
        sinks.addAll(getSinkFromSource(ANY, false));
        return sinks;
    }

    public Set<FlowPermission> getIntersectionAllowedSources(final /* Collection?? */ Collection<FlowPermission> sinks) {
        // TODO: Instead of starting with all sources, just get the first sink, 
        // unroll loop (start at 1)
        
        // Old Implementation/comment:
        // Start with all sources and remove those that are not allowed
        Set<FlowPermission> sources = Flow.getSetOfAllSources();
        
        /*
        Set<FlowPermission> sources =  new TreeSet<FlowPermission>();
        
        Iterator<FlowPermission> sinksIterator = sinks.iterator();
        if (sinksIterator.hasNext()) {
            FlowPermission firstSink = sinksIterator.next();
            sources = allowedSinkToSources.get(firstSink);
        }
        
        while (sinksIterator.hasNext()) { 
            final Set<FlowPermission> curSources = allowedSinkToSources.get(sinksIterator.next());
            sources = Flow.intersectSources(sources, curSources);
        }
        sources.addAll(getSourceFromSink(ANY, false));

        return  sources;
        */
        for (FlowPermission sink : sinks) {
            final Set<FlowPermission> curSources = allowedSinkToSources.get(sink);
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
        final Pattern linePattern = Pattern.compile("^\\s*((?:\\S+|" + EMPTY_REGEX
                + "))\\s*->\\s*((?:\\S+)(?:\\s*,\\s*\\S+)*)\\s*$");

        final List<String> errors = new ArrayList<String>();
        
        final List<CoarseFlowPermission> allCoarseSinkButAny = Arrays.asList(CoarseFlowPermission.values());
        final Set<FlowPermission> allSinkButAny = new TreeSet<FlowPermission>();
        // allCoarseSinkButAny.remove(CoarseFlowPermission.ANY);
        for (CoarseFlowPermission sink : allCoarseSinkButAny) {
            if (sink != CoarseFlowPermission.ANY) {
                allSinkButAny.add(new FlowPermission(sink));
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

                    Set<FlowPermission> sinks;
                    if (matcher.matches()) {
                        final String parameterizedFlowRegex = "(.*)[(\"](.*)[\")]";
                        String sourceStr = matcher.group(1).trim();
                        final String[] sinkStrs = matcher.group(2).split(",");
                        
                        if (sourceStr.matches(parameterizedFlowRegex))
                            sourceStr = sourceStr.substring(0, sourceStr.indexOf('('));
                                                                           
                        for (int i = 0; i < sinkStrs.length; i++)         
                            if (sinkStrs[i].matches(parameterizedFlowRegex)) 
                                sinkStrs[i] = sinkStrs[i].substring(0, sinkStrs[i].indexOf('('));                                
                        
                        CoarseFlowPermission source = null;
                        boolean skip = false;

                        if (sourceStr.equals(EMPTY) || sourceStr.equals(NOT_REVIEWED.toString())) {
                            errors.add(formatPolicyFileError(policyFile, lineNum, "FlowPermission "
                                    + sourceStr + " is not allowed in policy files", originalLine));
                            sinks = null;
                            skip = true;

                        } else {
                            try {
                                boolean foundInMap = false;
                                source = CoarseFlowPermission.valueOf(sourceStr);
                                sinks = null;
                                
                                for (FlowPermission allowedSource : allowedSourceToSinks.keySet()) {
                                    if (allowedSource.getPermission() == source) { // already in map
                                        foundInMap = true;
                                        sinks = allowedSourceToSinks.get(allowedSource);
                                        break;
                                    }
                                }

                                if (!foundInMap) {
                                    sinks = new TreeSet<FlowPermission>();
                                    // TODO: ADD PARAMETERS
                                    allowedSourceToSinks.put(new FlowPermission(source), sinks);
                                }
                            } catch (final IllegalArgumentException iaExc) {

                                errors.add(formatPolicyFileError(policyFile, lineNum,
                                        "Unrecognized source: " + sourceStr + " Known sources: "
                                                + enumValuesToString(CoarseFlowPermission.values()),
                                        originalLine));

                                sinks = null;
                                skip = true;
                            }

                        }
                        
                        for (final String sink : sinkStrs) {
                            if (sink.equals(EMPTY) || sink.equals(NOT_REVIEWED.toString())) {
                                errors.add(formatPolicyFileError(policyFile, lineNum,
                                        "FlowPermission " + sourceStr
                                                + " is not allowed in policy files", originalLine));
                                continue;
                            }
                            try {
                                final String trimmedSink = sink.trim();

                                // Read sinks even if source can't be decoded
                                // (i.e. source == null)
                                // in order to catch all errors in one pass
                                final CoarseFlowPermission sinkEnum = CoarseFlowPermission.valueOf(trimmedSink);

                                if (!skip) {
                                    // TODO: ADD PARAMETERS
                                    sinks.add(new FlowPermission(sinkEnum));
                                }

                            } catch (final IllegalArgumentException iaExc) {
                                errors.add(formatPolicyFileError(policyFile, lineNum,
                                        "Unrecognized sink: " + sink + " Known sinks: "
                                                + enumValuesToString(CoarseFlowPermission.values()),
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
      for(FlowPermission source : allowedSourceToSinks.keySet()){
          if(source.isSink()){
              //if the source can be a sink too, then there might be a transitive flow
              //TODO: handle what about WRITE_EXTERNAL_FILESYSTEM vs READ_EXTERNAL_FILESYSTEM
                if (allowedSinkToSources.containsKey(source)) {
                    Set<FlowPermission> sources = allowedSinkToSources.get(source);
                    Set<FlowPermission> sinks = allowedSourceToSinks.get(source);
                    Pair<Set<FlowPermission>, Set<FlowPermission>> flows = Pair.of(sources, sinks);
                    if (!areFlowsAllowed(flows)) {
                        System.err.flush();
                        System.err.println("Warning, flow policy has transive flow\n"
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

    public Set<FlowPermission> getSinkFromSource(final FlowPermission source, boolean includeAny) {
        return Flow.convertToAnySink(getSet(source, allowedSourceToSinks, includeAny), true);
    }

    public Set<FlowPermission> getSourceFromSink(final FlowPermission sink, boolean includeAny) {
        return Flow.convertToAnySource(getSet(sink, allowedSinkToSources, includeAny), true);

    }
    
    private Set<FlowPermission> getSet(
            final FlowPermission key, final Map<FlowPermission, Set<FlowPermission>> data, boolean includeAny) {
        Set<FlowPermission> values = data.get(key);
        if (includeAny) {
            final FlowPermission any = ANY;
            Set<FlowPermission> results = new TreeSet<FlowPermission>();
        if (values != null) {
            results.addAll(values);
        }
        results.addAll(getSet( any, data, false));
        return results;
        } else {
            if (values == null) {
                values = new TreeSet<FlowPermission>();
            }
            return Collections.unmodifiableSet(values);
        }
    }

    private Map<FlowPermission, Set<FlowPermission>> reverse (final Map<FlowPermission, Set<FlowPermission>> mapToReverse) {
        final Map<FlowPermission, Set<FlowPermission>> reversed = new HashMap<FlowPermission, Set<FlowPermission>>();
        for (final Map.Entry<FlowPermission, Set<FlowPermission>> keyToValues : mapToReverse.entrySet()) {
            FlowPermission valueInRevese = keyToValues.getKey();
            
            for (FlowPermission keyInReverse : keyToValues.getValue()) {
                Set<FlowPermission> newValues = reversed.get(keyInReverse);
                if (newValues == null) {
                    newValues = new TreeSet<FlowPermission>();
                    reversed.put(keyInReverse, newValues);
                }
                newValues.add(valueInRevese);
            }
        }
        return reversed;
    }
}

package sparta.checkers;

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
import java.util.TreeSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import sparta.checkers.quals.PFPermission;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;
import static sparta.checkers.quals.PFPermission.convertStringToPFPermission;

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

    public static final String EMPTY = "{}";
    public static final String EMPTY_REGEX = "\\{\\}";


    private final Map<PFPermission, Set<PFPermission>> allowedSourceToSinks;
    private final Map<PFPermission, Set<PFPermission>> allowedSinkToSources;

    private final/*@Nullable*/Set<PFPermission> sinksFromAnySource;
    
    private final PFPermission ANY;
    private ProcessingEnvironment processingEnv;
    

    /**
     *
     * @param flowPolicyFile
     * @param processingEnv2 
     */
    public FlowPolicy(final File flowPolicyFile, ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        ANY = new PFPermission(FlowPermission.ANY);

        this.allowedSourceToSinks = getDefaultAllowedFlows();
        if (flowPolicyFile != null && flowPolicyFile.exists()) {
            readPolicyFile(flowPolicyFile);
        }

        this.sinksFromAnySource = allowedSourceToSinks.get(ANY);
        this.allowedSinkToSources = reverse(allowedSourceToSinks);
        checkForTransitivity();

    }



    private HashMap<PFPermission, Set<PFPermission>> getDefaultAllowedFlows() {
        HashMap<PFPermission, Set<PFPermission>> defaultAllowedFlows = new HashMap<PFPermission, Set<PFPermission>>();
        return defaultAllowedFlows;
    }


    public static Pair<Set<PFPermission>, Set<PFPermission>> annotatedTypeMirrorToFlows(
            final AnnotatedTypeMirror atm) {

        final Set<PFPermission> sources = new TreeSet<>(
                Flow.getSources(atm));
        final Set<PFPermission> sinks = new TreeSet<>(
                Flow.getSinks(atm));

        return Pair.of(sources, sinks);
    }

    public boolean areFlowsAllowed(final AnnotatedTypeMirror atm) {
        final AnnotationMirror polySourceAnno = atm
                .getAnnotation(PolySource.class);
        final AnnotationMirror polySinkAnno = atm.getAnnotation(PolySink.class);
        final AnnotationMirror polyAllAnno = atm.getAnnotation(PolyAll.class);
        // If the type is marked with poly flow source or poly flow sink,
        // Then the flow is allowed.
        if (polySinkAnno != null || polySourceAnno != null
                || polyAllAnno != null) {
            return true;
        }

        return areFlowsAllowed(annotatedTypeMirrorToFlows(atm));
    }

    public List<Flow> forbiddenFlows(final AnnotatedTypeMirror atm) {
        return forbiddenFlows(annotatedTypeMirrorToFlows(atm));
    }

    public List<Flow> forbiddenFlows(
            final Pair<Set<PFPermission>, Set<PFPermission>> flows) {
        final Set<PFPermission> sources = flows.first;
        final Set<PFPermission> sinks = flows.second;
        List<Flow> forflows = new ArrayList<Flow>();


        for (final PFPermission source : sources) {
            for( final PFPermission sink : sinks){
                if(!areFlowsAllowed(Pair.of(Collections.singleton(source), Collections.singleton(sink)))){
                    forflows.add(new Flow(Collections.singleton(source),  Collections.singleton(sink)));
                }
            }
        }

        return forflows;
    }

    public boolean areFlowsAllowed(
            final Pair<Set<PFPermission>, Set<PFPermission>> flows) {
        final Set<PFPermission> sources = new TreeSet<>(
                flows.first);
        final Set<PFPermission> sinks = new TreeSet<>(
                flows.second);

        if (sources.isEmpty() || sinks.isEmpty()) {
            return true;
        }

        if (sinksFromAnySource != null) {
            sinks.removeAll(sinksFromAnySource);
            if (sinks.isEmpty()) {
                return true;
            }
        }

        for (final PFPermission source : sources) {
            final Set<PFPermission> allowedSinks = getAllowedSinks(source);

            if (allowedSinks == null) {
                return false;
            }

            if (allowedSinks.contains(ANY)) {
                return true;
            }

            for (PFPermission sink : sinks) {
                if (!FlowAnnotatedTypeFactory.isMatchInSet(sink, allowedSinks)) {
                    return false;
                }
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

    public Set<PFPermission> getIntersectionAllowedSinks(
            final Set<PFPermission> sources) {
        if(sources.isEmpty()) return Collections.singleton(ANY);

        // Start with all sinks and remove those that are not allowed
        Set<PFPermission> sinks = Flow.getSetOfAllSinks();

        for (PFPermission source : sources) {
            final Set<PFPermission> curSinks = getAllowedSinks(source);
            sinks = Flow.intersectSinks(sinks, curSinks);
        }
        sinks.addAll(getSinkFromSource(ANY, false));
        return sinks;
    }

    public Set<PFPermission> getIntersectionAllowedSources(
            final/* Collection?? */Collection<PFPermission> sinks) {
        if(sinks.isEmpty()) return Collections.singleton(ANY);
        Set<PFPermission> sources = Flow.getSetOfAllSources();

        for (PFPermission sink : sinks) {
            final Set<PFPermission> curSources = getAllowedSources(sink);
            sources = Flow.intersectSources(sources, curSources);
        }
        sources.addAll(getSourceFromSink(ANY, false));
        return sources;
    }

    private Set<PFPermission> getAllowedSources(
            PFPermission sink) {
        TreeSet<PFPermission> sources = new TreeSet<PFPermission>();
        // check flow policy for any sinks that match
        for (PFPermission posSink : allowedSinkToSources
                .keySet()) {
            if (posSink.getPermission() == sink.getPermission()) {
                if (FlowAnnotatedTypeFactory.allParametersMatch(
                        sink.getParameters(), posSink.getParameters())) {
                    Set<PFPermission> newsources = allowedSinkToSources
                            .get(posSink);
                    sources.addAll(newsources);
                }
            }
        }
        return sources;
    }

    private Set<PFPermission> getAllowedSinks(
            final PFPermission source) {
        TreeSet<PFPermission> sinks = new TreeSet<PFPermission>();
        // check flow policy for all matching sources
        for (PFPermission posSource : allowedSourceToSinks
                .keySet()) {
            if (posSource.getPermission() == source.getPermission()) {
                if (FlowAnnotatedTypeFactory.allParametersMatch(
                        source.getParameters(), posSource.getParameters())) {
                    sinks.addAll(allowedSourceToSinks.get(posSource));
                }
            }
        }
        return sinks;
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
     * white space between symbols) A runtime error will be thrown if ANY line
     * is malformed (All errors will be reported via standard out before hand)
     * 
     * @param policyFile
     *            A file formatted as above
     * @return
     */
    private void readPolicyFile(final File policyFile) {
        final Pattern linePattern = Pattern
                .compile("^\\s*((?:[^->]+))->\\s*((?:[^->]+)(?:,[^->]+)*)\\s*$");

        final List<FlowPermission> allCoarseSinkButAny = Arrays
                .asList(FlowPermission.values());
        final Set<PFPermission> allSinkButAny = new TreeSet<PFPermission>();

        for (FlowPermission sink : allCoarseSinkButAny) {
            if (sink != FlowPermission.ANY) {
                allSinkButAny.add(new PFPermission(sink));
            }
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(
                policyFile));) {
            int lineNum = 1;
            String originalLine = bufferedReader.readLine().trim();
            NextLine: while (originalLine != null) {

                // Remove anything from # on in the line
                final String line = stripComment(originalLine);

                if (!line.isEmpty() && !isWhiteSpaceLine(line)) {
                    try {
                        final Matcher matcher = linePattern.matcher(line);

                        if (!matcher.matches()) {
                            lineError(
                                    policyFile,
                                    lineNum,
                                    "Syntax error, Lines are of the form: flowSource -> sink1, sink2, ..., sinkN ",
                                    originalLine);
                        } else {
                            String sourceStr = matcher.group(1).trim();
                            String groupmatcher = matcher.group(2).trim();
                            if (notAllowed(sourceStr)) {
                                lineError(
                                        policyFile,
                                        lineNum,
                                        "FlowPermission "
                                                + sourceStr
                                                + " is not allowed in policy files",
                                        originalLine);
                                continue NextLine;
                            }
                            PFPermission sourcePFP = convertStringToPFPermission(sourceStr);

                            final String[] sinkStrs = groupmatcher
                                    .split(",(?![^(]*\\))");
                            List<PFPermission> sinkObjects = new ArrayList<PFPermission>();
                            for (String sinkStr : sinkStrs) {
                                if (notAllowed(sinkStr)) {
                                    lineError(
                                            policyFile,
                                            lineNum,
                                            "FlowPermission "
                                                    + sinkStr
                                                    + " is not allowed in policy files",
                                            originalLine);
                                    continue NextLine;
                                }
                                PFPermission sinkPFP = convertStringToPFPermission(sinkStr);
                                sinkObjects.add(sinkPFP);
                            }
                            addToMap(allSinkButAny, sourcePFP, sinkObjects);
                        }
                    } catch (final IllegalArgumentException iaExc) {
                        lineError(
                                policyFile,
                                lineNum,
                                "Unrecognized permisison: "
                                        + enumValuesToString(FlowPermission
                                                .values()), originalLine);
                    }
                }
                ++lineNum;
                originalLine = bufferedReader.readLine();
            }
            // checkForTransitivity();
        } catch (final IOException ioExc) {
            throw new RuntimeException(ioExc);
        }
    }

    private void addToMap(final Set<PFPermission> allSinkButAny,
            PFPermission sourcePFP,
            List<PFPermission> sinkObjects) {

        Set<PFPermission> sinks = allowedSourceToSinks
                .get(sourcePFP);

        if (sinks == null) {
            sinks = new TreeSet<PFPermission>();
            allowedSourceToSinks.put(sourcePFP, sinks);
        }
        sinks.addAll(sinkObjects);

        if (sinks.containsAll(allSinkButAny)) {
            sinks.clear();
            sinks.add(ANY);
        }
    }

    private boolean notAllowed(String sinkStr) {
        return false;
    }


    private void checkForTransitivity() {
        Map<PFPermission, Flow> transitiveFlows = new HashMap<>();
        for (PFPermission both : allowedSourceToSinks.keySet()) {
            if (null == both)
                continue;
            if (both.isSink()) {
                // if the source can be a sink too, then there might be a
                // transitive flow
                // TODO: handle what about WRITE_EXTERNAL_FILESYSTEM vs
                // READ_EXTERNAL_FILESYSTEM
                if (allowedSinkToSources.containsKey(both)) {
                    Set<PFPermission> sources = getAllowedSources(both);
                    Set<PFPermission> sinks = getAllowedSinks(both);
                    for (PFPermission source : sources) {
                        for (PFPermission sink : sinks) {
                        if (!areFlowsAllowed(Pair.of(
                                Collections.singleton(source), Collections.singleton(sink)))) {
                            Flow transtive = transitiveFlows.get(source);
                            if (transtive == null){
                                transtive = new Flow(source);
                                transitiveFlows.put(source, transtive);
                            }
                            transtive.addSink(sink);
                        }
                        }
                    }

                }
            }
        }
        StringBuffer flowsBuffer = new StringBuffer();
        for (Flow flow : transitiveFlows.values()) {
            flowsBuffer.append(flow+"\n");
        }
        String flows = flowsBuffer.toString();
        if(!flows.equals("")){
            warning("Found transitive flows:\n" + flows
                    + "Please add them to the flow policy");
        }
    }

    private static final Pattern WHITE_SPACE_PATTERN = Pattern
            .compile("^\\s*$");

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

    private void lineError(final File file, final int lineNum,
            final String message, final String line) {
        error(file.getAbsolutePath() + ":" + lineNum + ": " + message + "\n"
                + line);
    }

    private void error(String warning) {
        if (processingEnv != null)
            processingEnv.getMessager()
                    .printMessage(javax.tools.Diagnostic.Kind.ERROR,
                            "FlowPolicy: " + warning);
        else
            System.err.println("FlowPolicy: " + warning);
    }

    private void warning(String warning) {
        if (processingEnv != null)
            processingEnv.getMessager().printMessage(
                    javax.tools.Diagnostic.Kind.WARNING,
                    "FlowPolicy: " + warning);
        else
            System.err.println("FlowPolicy: " + warning);
    }

    public Set<PFPermission> getSinkFromSource(
            final PFPermission source, boolean includeAny) {
        return Flow.convertToAnySink(
                getSet(source, allowedSourceToSinks, includeAny), true);
    }

    public Set<PFPermission> getSourceFromSink(
            final PFPermission sink, boolean includeAny) {
        return Flow.convertToAnySource(
                getSet(sink, allowedSinkToSources, includeAny), true);

    }

    private Set<PFPermission> getSet(
            final PFPermission key,
            final Map<PFPermission, Set<PFPermission>> data,
            boolean includeAny) {
        Set<PFPermission> values = data.get(key);
        if (includeAny) {
            final PFPermission any = ANY;
            Set<PFPermission> results = new TreeSet<PFPermission>();
            if (values != null) {
                results.addAll(values);
            }
            results.addAll(getSet(any, data, false));
            return results;
        } else {
            if (values == null) {
                values = new TreeSet<PFPermission>();
            }
            return Collections.unmodifiableSet(values);
        }
    }

    private Map<PFPermission, Set<PFPermission>> reverse(
            final Map<PFPermission, Set<PFPermission>> mapToReverse) {
        final Map<PFPermission, Set<PFPermission>> reversed = new HashMap<PFPermission, Set<PFPermission>>();

        for (final Map.Entry<PFPermission, Set<PFPermission>> flow : mapToReverse
                .entrySet()) {
            PFPermission source = flow.getKey();

            for (PFPermission aSink : flow.getValue()) {
                Set<PFPermission> sources = reversed.get(aSink);
                if (sources == null) {
                    sources = new TreeSet<PFPermission>();
                    reversed.put(aSink, sources);
                }
                if (source != null)
                    sources.add(source);
            }
        }
        return reversed;
    }
}

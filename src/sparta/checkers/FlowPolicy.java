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

    public static final String EMPTY = "{}";
    public static final String EMPTY_REGEX = "\\{\\}";
    final String PARAMETERIZED_PERMISSION_REGEX = "(.*)[(\"](.*)[\")]";


    private final Map<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> allowedSourceToSinks;
    private final Map<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> allowedSinkToSources;

    private final/*@Nullable*/Set<ParameterizedFlowPermission> sinksFromAnySource;
    
    private final ParameterizedFlowPermission ANY;
    private ProcessingEnvironment processingEnv;
    

    /**
     *
     * @param flowPolicyFile
     * @param processingEnv2 
     */
    public FlowPolicy(final File flowPolicyFile, ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        ANY = new ParameterizedFlowPermission(FlowPermission.ANY);

        this.allowedSourceToSinks = getDefaultAllowedFlows();
        if (flowPolicyFile != null && flowPolicyFile.exists()) {
            readPolicyFile(flowPolicyFile);
        }

        this.sinksFromAnySource = allowedSourceToSinks.get(ANY);
        this.allowedSinkToSources = reverse(allowedSourceToSinks);
        checkForTransitivity();

    }



    private HashMap<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> getDefaultAllowedFlows() {
        HashMap<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> defaultAllowedFlows = new HashMap<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>>();
        return defaultAllowedFlows;
    }


    public static Pair<Set<ParameterizedFlowPermission>, Set<ParameterizedFlowPermission>> annotatedTypeMirrorToFlows(
            final AnnotatedTypeMirror atm) {

        final Set<ParameterizedFlowPermission> sources = new TreeSet<>(
                Flow.getSources(atm));
        final Set<ParameterizedFlowPermission> sinks = new TreeSet<>(
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
            final Pair<Set<ParameterizedFlowPermission>, Set<ParameterizedFlowPermission>> flows) {
        final Set<ParameterizedFlowPermission> sources = flows.first;
        final Set<ParameterizedFlowPermission> sinks = flows.second;
        List<Flow> forflows = new ArrayList<Flow>();


        for (final ParameterizedFlowPermission source : sources) {
            for( final ParameterizedFlowPermission sink : sinks){
                if(!areFlowsAllowed(Pair.of(Collections.singleton(source), Collections.singleton(sink)))){
                    forflows.add(new Flow(Collections.singleton(source),  Collections.singleton(sink)));
                }
            }
        }

        return forflows;
    }

    public boolean areFlowsAllowed(
            final Pair<Set<ParameterizedFlowPermission>, Set<ParameterizedFlowPermission>> flows) {
        final Set<ParameterizedFlowPermission> sources = new TreeSet<>(
                flows.first);
        final Set<ParameterizedFlowPermission> sinks = new TreeSet<>(
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

        for (final ParameterizedFlowPermission source : sources) {
            final Set<ParameterizedFlowPermission> allowedSinks = getAllowedSinks(source);

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

    public Set<ParameterizedFlowPermission> getIntersectionAllowedSinks(
            final Set<ParameterizedFlowPermission> sources) {
        if(sources.isEmpty()) return Collections.singleton(ANY);

        // Start with all sinks and remove those that are not allowed
        Set<ParameterizedFlowPermission> sinks = Flow.getSetOfAllSinks();

        for (ParameterizedFlowPermission source : sources) {
            final Set<ParameterizedFlowPermission> curSinks = getAllowedSinks(source);
            sinks = Flow.intersectSinks(sinks, curSinks);
        }
        sinks.addAll(getSinkFromSource(ANY, false));
        return sinks;
    }

    public Set<ParameterizedFlowPermission> getIntersectionAllowedSources(
            final/* Collection?? */Collection<ParameterizedFlowPermission> sinks) {
        if(sinks.isEmpty()) return Collections.singleton(ANY);
        Set<ParameterizedFlowPermission> sources = Flow.getSetOfAllSources();

        for (ParameterizedFlowPermission sink : sinks) {
            final Set<ParameterizedFlowPermission> curSources = getAllowedSources(sink);
            sources = Flow.intersectSources(sources, curSources);
        }
        sources.addAll(getSourceFromSink(ANY, false));
        return sources;
    }

    private Set<ParameterizedFlowPermission> getAllowedSources(
            ParameterizedFlowPermission sink) {
        TreeSet<ParameterizedFlowPermission> sources = new TreeSet<ParameterizedFlowPermission>();
        // check flow policy for any sinks that match
        for (ParameterizedFlowPermission posSink : allowedSinkToSources
                .keySet()) {
            if (posSink.getPermission() == sink.getPermission()) {
                if (FlowAnnotatedTypeFactory.allParametersMatch(
                        sink.getParameters(), posSink.getParameters())) {
                    Set<ParameterizedFlowPermission> newsources = allowedSinkToSources
                            .get(posSink);
                    sources.addAll(newsources);
                }
            }
        }
        return sources;
    }

    private Set<ParameterizedFlowPermission> getAllowedSinks(
            final ParameterizedFlowPermission source) {
        TreeSet<ParameterizedFlowPermission> sinks = new TreeSet<ParameterizedFlowPermission>();
        // check flow policy for all matching sources
        for (ParameterizedFlowPermission posSource : allowedSourceToSinks
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
        final Set<ParameterizedFlowPermission> allSinkButAny = new TreeSet<ParameterizedFlowPermission>();

        for (FlowPermission sink : allCoarseSinkButAny) {
            if (sink != FlowPermission.ANY) {
                allSinkButAny.add(new ParameterizedFlowPermission(sink));
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
                            ParameterizedFlowPermission sourcePFP = getPFP(sourceStr);

                            final String[] sinkStrs = groupmatcher
                                    .split(",(?![^(]*\\))");
                            List<ParameterizedFlowPermission> sinkObjects = new ArrayList<ParameterizedFlowPermission>();
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
                                ParameterizedFlowPermission sinkPFP = getPFP(sinkStr);
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

    private void addToMap(final Set<ParameterizedFlowPermission> allSinkButAny,
            ParameterizedFlowPermission sourcePFP,
            List<ParameterizedFlowPermission> sinkObjects) {

        Set<ParameterizedFlowPermission> sinks = allowedSourceToSinks
                .get(sourcePFP);

        if (sinks == null) {
            sinks = new TreeSet<ParameterizedFlowPermission>();
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

    private ParameterizedFlowPermission getPFP(String sinkStr) {
        sinkStr = sinkStr.trim();
        List<String> currentSinkParams = new ArrayList<String>();
        if (sinkStr.matches(PARAMETERIZED_PERMISSION_REGEX)) {
            String sinkParameterString = sinkStr.substring(
                    sinkStr.indexOf('(') + 1, sinkStr.indexOf(')'));
            sinkStr = sinkStr.substring(0, sinkStr.indexOf('(')).trim();

            // Save sink parameters
            String[] sinkParameterStrings = sinkParameterString.split(",");
            for (String param : sinkParameterStrings) {
                // Strip quotes and add to parameter list
                param = param.substring(param.indexOf('\"') + 1);
                param = param.substring(0, param.indexOf('\"'));
                param = param.trim();
                currentSinkParams.add(param);
            }
        }
        ParameterizedFlowPermission sinkPFP = new ParameterizedFlowPermission(
                FlowPermission.valueOf(sinkStr), currentSinkParams);
        return sinkPFP;
    }

    private void checkForTransitivity() {
        for (ParameterizedFlowPermission source : allowedSourceToSinks.keySet()) {
            if(null == source) continue;
            if (source.isSink()) {
                // if the source can be a sink too, then there might be a
                // transitive flow
                // TODO: handle what about WRITE_EXTERNAL_FILESYSTEM vs
                // READ_EXTERNAL_FILESYSTEM
                if (allowedSinkToSources.containsKey(source)) {
                    Set<ParameterizedFlowPermission> sources = getAllowedSources(source);
                    Set<ParameterizedFlowPermission> sinks = getAllowedSinks(source);
                    Pair<Set<ParameterizedFlowPermission>, Set<ParameterizedFlowPermission>> flows = Pair
                            .of(sources, sinks);
                    if (!areFlowsAllowed(flows)) {
                        warning("Found transitive flow\n"
                                + getAllowedSources(source) + "->"
                                + getAllowedSinks(source)
                                + "\nPlease add them to the flow policy");
                    }
                }
            }
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

    public Set<ParameterizedFlowPermission> getSinkFromSource(
            final ParameterizedFlowPermission source, boolean includeAny) {
        return Flow.convertToAnySink(
                getSet(source, allowedSourceToSinks, includeAny), true);
    }

    public Set<ParameterizedFlowPermission> getSourceFromSink(
            final ParameterizedFlowPermission sink, boolean includeAny) {
        return Flow.convertToAnySource(
                getSet(sink, allowedSinkToSources, includeAny), true);

    }

    private Set<ParameterizedFlowPermission> getSet(
            final ParameterizedFlowPermission key,
            final Map<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> data,
            boolean includeAny) {
        Set<ParameterizedFlowPermission> values = data.get(key);
        if (includeAny) {
            final ParameterizedFlowPermission any = ANY;
            Set<ParameterizedFlowPermission> results = new TreeSet<ParameterizedFlowPermission>();
            if (values != null) {
                results.addAll(values);
            }
            results.addAll(getSet(any, data, false));
            return results;
        } else {
            if (values == null) {
                values = new TreeSet<ParameterizedFlowPermission>();
            }
            return Collections.unmodifiableSet(values);
        }
    }

    private Map<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> reverse(
            final Map<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> mapToReverse) {
        final Map<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> reversed = new HashMap<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>>();

        for (final Map.Entry<ParameterizedFlowPermission, Set<ParameterizedFlowPermission>> flow : mapToReverse
                .entrySet()) {
            ParameterizedFlowPermission source = flow.getKey();

            for (ParameterizedFlowPermission aSink : flow.getValue()) {
                Set<ParameterizedFlowPermission> sources = reversed.get(aSink);
                if (sources == null) {
                    sources = new TreeSet<ParameterizedFlowPermission>();
                    reversed.put(aSink, sources);
                }
                if (source != null)
                    sources.add(source);
            }
        }
        return reversed;
    }
}

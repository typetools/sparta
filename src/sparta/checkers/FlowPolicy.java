package sparta.checkers;

import checkers.types.AnnotatedTypeMirror;
import checkers.util.AnnotationUtils;
import checkers.util.Pair;
import sparta.checkers.quals.FlowSinks;
import sparta.checkers.quals.FlowSources;

import javax.lang.model.element.AnnotationMirror;

import static sparta.checkers.quals.FlowSinks.FlowSink;
import static sparta.checkers.quals.FlowSources.FlowSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*>>>
import checkers.nullness.quals.Nullable;
*/

/**
 *
 */
public class FlowPolicy {

    //The name of the command line option to specify a policyFile
    //A -A will precede this name on the command line ( e.g. -ApolicyFile=/path/to/file )
    public static final String POLICY_FILE_OPTION = "flowPolicy";

    public static final String EMPTY = "{}";
    public static final String EMPTY_REGEX = "\\{\\}";

    private final Map<FlowSource, Set<FlowSink>> allowedFlows;

    private final /*@Nullable*/ Set<FlowSink> sinksFromAnySource;
    private final Set<FlowSink> sinksFromEmptySource;
    private final Set<FlowSource> sourcesToEmptySink;

    public FlowPolicy( final Map<FlowSource, Set<FlowSink>> allowedFlows,
                       final Set<FlowSink> sinksFromEmptySource,
                       final Set<FlowSource> sourceToEmptySink) {
        this.allowedFlows = allowedFlows;
        this.sinksFromEmptySource = sinksFromEmptySource;
        this.sinksFromAnySource = allowedFlows.get(FlowSource.ANY);
        this.sourcesToEmptySink = sourceToEmptySink;
    }

    public FlowPolicy( final File flowPolicyFile ) {
    	System.out.println(flowPolicyFile);
        assert flowPolicyFile != null  :  "PolicyFile cannot be null!";
        assert flowPolicyFile.exists() :  "PolicyFile doesn't exist!  Filename=" + flowPolicyFile.getAbsolutePath();

        this.sinksFromEmptySource = new HashSet<FlowSink>();
        if( this.sinksFromEmptySource.contains(FlowSink.ANY) ) {
            this.sinksFromEmptySource.addAll(Arrays.asList(FlowSink.values()));
        }

        //TODO: PERHAPS ALWAYS ALLOW TO FLOW TO NOWHERE
        this.sourcesToEmptySink   = new HashSet<FlowSource>();
        if( this.sourcesToEmptySink.contains(FlowSource.ANY) ) {
            this.sourcesToEmptySink.addAll(Arrays.asList(FlowSource.values()));
        }

        this.allowedFlows       = new HashMap<FlowSource, Set<FlowSink>>();
        readPolicyFile(flowPolicyFile);
        this.sinksFromAnySource = allowedFlows.get(FlowSource.ANY);
    }
    public FlowPolicy( ) {
        this.sinksFromEmptySource = new HashSet<FlowSink>();
        this.sourcesToEmptySink   = new HashSet<FlowSource>();
        this.allowedFlows       = new HashMap<FlowSource, Set<FlowSink>>();
        this.sinksFromAnySource = allowedFlows.get(FlowSource.ANY);
    }

    public Pair<Set<FlowSource>, Set<FlowSink>> annotatedTypeMirrorToFlows(final AnnotatedTypeMirror atm) {

        final AnnotationMirror sourceAnno = atm.getAnnotation(FlowSources.class);
        final AnnotationMirror sinkAnno   = atm.getAnnotation(FlowSinks.class);

        assert sourceAnno != null : "Annotated Type Mirror must have a FlowSources annotation";
        assert sinkAnno != null : "Annotated Type Mirror must have a FlowSinks annotation";

        final Set<FlowSource> sources = new HashSet<FlowSource>(
                AnnotationUtils.getElementValueEnumArray(sourceAnno, "value", FlowSources.FlowSource.class, true));

        final Set<FlowSink> sinks = new HashSet<FlowSink>(
                    AnnotationUtils.getElementValueEnumArray(sinkAnno,  "value", FlowSinks.FlowSink.class,   true));




        if( sources.contains( FlowSource.ANY ) ) {
            sources.addAll( Arrays.asList( FlowSource.values() ) );
            sources.remove( FlowSource.ANY );
        }

        if( sinks.contains(FlowSink.ANY) ) {
            sinks.addAll(Arrays.asList(FlowSink.values()));
            sinks.remove(FlowSink.ANY);
        }

        return Pair.of(sources, sinks);
    }

    /**
     */
    public static Pair<Set<FlowSource>, Set<FlowSink>> findUncheckedFlows(final Pair<Set<FlowSource>, Set<FlowSink>> from,
                                                                   final Pair<Set<FlowSource>, Set<FlowSink>> to) {
        final Set<FlowSource> sources = new HashSet<FlowSource>(from.first);
        final Set<FlowSink>   sinks   = new HashSet<FlowSink>(to.second);

        //hadSources and hadSinks is intended to guard against situtations similar to the following:
        // @FlowSource(A,B) @FlowSource(C,D) from;
        // @FlowSource(A,B) @FlowSink(C,E) to;
        // to = from;
        // In this situtation we want to check the flows A->C, A->E, B->C, B->E
        // After removing sinks and sources we would end up with the following flows to check:
        // sources({}) sinks(E)
        // But {} -> E is not actually one of the flows we wanted to check.  In fact, since
        // sources is empty we know that all of the flows we wanted to check are allowed.
        // We could also have situations like E -> {} when in fact we don't want to check
        // the empty set of sinks.
        boolean hadSources = !sources.isEmpty();
        boolean hadSinks   = !sinks.isEmpty();

        sources.removeAll(to.first);
        sinks.removeAll(from.second);

        if( hadSources && sources.isEmpty() ) {
            sinks.clear();
        } else if(hadSinks && sinks.isEmpty() ) {
            sources.clear();
        }

        return Pair.of(sources, sinks);
    }

    public boolean suppressFlowWarnings(final AnnotatedTypeMirror lhs, final AnnotatedTypeMirror rhs) {
        final Pair<Set<FlowSource>, Set<FlowSink>> flows =
                findUncheckedFlows(annotatedTypeMirrorToFlows(rhs), annotatedTypeMirrorToFlows(lhs));

        return areFlowsAllowed(flows);
    }

    public boolean areFlowsAllowed(final AnnotatedTypeMirror atm) {
       return areFlowsAllowed(annotatedTypeMirrorToFlows(atm));
    }

    public boolean areFlowsAllowed(final Pair<Set<FlowSource>, Set<FlowSink>> flows) {
        final Set<FlowSource> sources = flows.first;
        final Set<FlowSink>   sinks   = flows.second;

        if( sources.isEmpty() ) {

            if(sinks.isEmpty()) {
                return true;
            } else {
                return sinksFromEmptySource.contains(FlowSink.ANY) ||
                       sinksFromEmptySource.containsAll(sinks);
            }
        }

        if( sinks.isEmpty() ) {
            return sourcesToEmptySink.contains(FlowSource.ANY) || sourcesToEmptySink.containsAll(sources);
        }

        if(sinksFromAnySource != null) {
            sinks.removeAll(sinksFromAnySource);
        }

        if( sinks.isEmpty() ) {
            return true;
        }

        for(final FlowSource source : sources) {
            final Set<FlowSink> allowedSinks = allowedFlows.get(source);

            if(allowedSinks == null || !(allowedSinks.contains(FlowSink.ANY) ||
                                         allowedSinks.containsAll(sinks))) {
                return false;
            }
        }

        return true;
    }

    /**
     * If # appears in the line
     *   remove all text from # to the end of the string
     *   trim the string (this is important in order to recognize empty lines)
     * else
     *    return the original line
     *
     * @param input Line that may contain a comment
     * @return Line that does not contain a comment
     */
    public static String stripComment(final String input) {
        int commentIndex = input.indexOf('#');

        final String out;
        if(commentIndex > -1) {
            if(commentIndex == 0) {
                out = "";
            } else {
                out = input.substring(0, commentIndex).trim();
            }
        } else {
            out = input;
        }

        return out;
    }

    /**
     * Read the given file return a one to many Map of FlowSource -> FlowSinks where
     * each entry indicates what sinks a source is given blanket access to reach
     *
     *
     * Format:
     *   A flow policy file is read line by line where each line has the following format
     *   FlowSourceName -> FlowSinkName, FlowSinkName, FlowSinkName
     *
     *   FlowSourceName  = One of the names of the enums in FlowSource
     *   FlowSinkName<x> = One of the names of the enums in FlowSink
     *
     *   A source can appear twice, the output FlowSinks for that given source will contain
     *   the union of the two entries.
     *
     *   E.g.
     *   MICROPHONE   -> NETWORK, TEXT_MESSAGE
     *   MICROPHONE   -> FILESYSTEM
     *   TEXT_MESSAGE -> NETWORK
     *
     *   In this case there would be the following entry in the Map
     *   Key(MICROPHONE)   => Entries(NETWORK, TEXT_MESSAGE, FILESYSTEM)
     *   Key(TEXT_MESSAGE) => Entries(NETWORK)
     *
     *   Comments are permitted and start with a #
     *   Blank lines are ignored (as is white space between symbols)
     *   A runtime error will be thrown if ANY line is malformed
     *   (All errors will be reported via standard out before hand)
     *
     * @param policyFile A file formatted as above
     * @return
     */
    private void readPolicyFile(final File policyFile) {

        final Pattern linePattern  = Pattern.compile("^\\s*((?:\\S+|" + EMPTY_REGEX + "))\\s*->\\s*((?:\\S+)(?:\\s*,\\s*\\S+)*)\\s*$");

        final List<String> errors = new ArrayList<String>();

        BufferedReader bufferedReader = null;
        try {
            int lineNum = 1;
            bufferedReader = new BufferedReader(new FileReader(policyFile));
            String originalLine = bufferedReader.readLine().trim();

            while(originalLine != null) {

                //Remove anything from # on in the line
                final String line = stripComment(originalLine);

                if(!line.isEmpty() && !isWhiteSpaceLine(line)) {
                    final Matcher matcher = linePattern.matcher(line);

                    Set<FlowSink> sinks;
                    if(matcher.matches()) {
                        final String sourceStr   = matcher.group(1).trim();
                        final String [] sinkStrs = matcher.group(2).split(",");
                        FlowSource source = null;
                        boolean skip = false;

                        if( sourceStr.equals(EMPTY) ) {
                            sinks = sinksFromEmptySource;

                        } else {
                            try {
                                source = FlowSource.valueOf(sourceStr);

                                sinks = allowedFlows.get(source);
                                if(sinks == null && source != null) {
                                    sinks = new HashSet<FlowSink>(sinkStrs.length);
                                    allowedFlows.put(source, sinks);
                                }
                            } catch(final IllegalArgumentException iaExc) {

                                errors.add(
                                        formatPolicyFileError(policyFile, lineNum,
                                                "Unrecognized source: " + sourceStr +
                                                        " Known sources: " + enumValuesToString(FlowSource.values()) +
                                                        " Empty Source: {}",
                                                originalLine)
                                );

                                sinks = null;
                                skip = true;
                            }

                        }

                        for(final String sink : sinkStrs) {
                            try {
                                final String trimmedSink = sink.trim();
                                if(trimmedSink.equals(EMPTY)) {
                                    if(source != null) {
                                        sourcesToEmptySink.add(source);
                                    }
                                } else {
                                    //Read sinks even if source can't be decoded (i.e. source == null)
                                    //in order to catch all errors in one pass
                                    final FlowSink sinkEnum = FlowSink.valueOf(trimmedSink);

                                    if(!skip) {
                                        sinks.add(sinkEnum);
                                    }
                                }
                            } catch(final IllegalArgumentException iaExc) {
                                errors.add(
                                    formatPolicyFileError(policyFile, lineNum,
                                            "Unrecognized sink: " + sink +
                                            " Known sinks: " + enumValuesToString(FlowSink.values()),
                                            originalLine)
                                );
                            }
                        }

                    } else {
                        errors.add(
                            formatPolicyFileError(policyFile, lineNum,
                                "Syntax error, Lines are of the form: flowSource -> sink1, sink2, ..., sinkN ",
                                    originalLine )
                        );
                    }
                }

                ++lineNum;
                originalLine=bufferedReader.readLine();
            }

        } catch(final IOException ioExc) {
            throw new RuntimeException(ioExc);
        } finally {
            try {
                if(bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
            }
        }

        if( !errors.isEmpty() ) {
            System.out.println("\nErrors parsing policy file:");
            for(final String error : errors) {
                System.out.println(error);
                System.out.println();
            }

            System.out.flush();
            throw new RuntimeException("Errors parsing policy file: " + policyFile.getAbsolutePath());
        }
    }

    private static final Pattern WHITE_SPACE_PATTERN = Pattern.compile("^\\s*$");
    public static boolean isWhiteSpaceLine(final String line) {
        if(WHITE_SPACE_PATTERN.matcher(line).matches()) {
            return true;
        }
        return false;
    }

    public static String enumValuesToString(final Enum<?> [] enumValues) {

        String str = "";
        boolean comma = false;
        for(final Enum<?> ev : enumValues) {
            if(comma) {
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
        return file.getAbsolutePath() + ":" + lineNum + ": " +  message + "\n" + line;
    }
}

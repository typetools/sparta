package sparta.checkers;

import checkers.quals.PolyAll;
import checkers.types.AnnotatedTypeMirror;
import checkers.util.AnnotationUtils;
import checkers.util.Pair;
import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.Sources;
import sparta.checkers.quals.PolySinks;
import sparta.checkers.quals.PolySources;

import javax.lang.model.element.AnnotationMirror;

import static sparta.checkers.quals.SpartaPermission.*;
import  sparta.checkers.quals.SpartaPermission;

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
    public static final String STRICT_CONDITIONALS_OPTION = "strict-conditional";

    public static final String EMPTY = "{}";
    public static final String EMPTY_REGEX = "\\{\\}";

    private final Map<SpartaPermission, Set<SpartaPermission>>   allowedFlows;
    private final Map<SpartaPermission,   Set<SpartaPermission>> reversedAllowedFlows;

    private final /*@Nullable*/ Set<SpartaPermission> sinksFromAnySource;

    //True: LITERAL->CONDITIONAL is added,
    //False: ANY->CONDITIONAL is added
	private final boolean strictConditionals;

    public FlowPolicy( final Map<SpartaPermission, Set<SpartaPermission>> allowedFlows) {
        this.allowedFlows         = allowedFlows;
        this.reversedAllowedFlows = reverse(allowedFlows);
        this.sinksFromAnySource = allowedFlows.get(SpartaPermission.ANY);
        this.strictConditionals=false;
    }

    /**
     *
     * @param flowPolicyFile
     * @param strictConditionals if true LITERAL->CONDITIONAL is added, otherwise ANY->CONDITIONAL is added
     */
    public FlowPolicy( final File flowPolicyFile, boolean strictConditionals ) {
        this.strictConditionals=strictConditionals;
        this.allowedFlows       = getDefaultAllowedFlows();
        if(flowPolicyFile != null && flowPolicyFile.exists()  ){
            readPolicyFile(flowPolicyFile);
        }
        this.sinksFromAnySource = allowedFlows.get(SpartaPermission.ANY);
        this.reversedAllowedFlows = reverse(allowedFlows);
    }

    public FlowPolicy(final File flowPolicyFile){
    	this(flowPolicyFile,false);
    }

    public FlowPolicy( ) {
        this(false);
    }

    /**
     *
     * @param strictConditionals if true LITERAL->CONDITIONAL is added, otherwise ANY->CONDITIONAL is added
     */
    public FlowPolicy( boolean strictConditionals ) {
    	this(null,strictConditionals);
    }

    private  HashMap<SpartaPermission, Set<SpartaPermission>> getDefaultAllowedFlows(){
    	HashMap<SpartaPermission, Set<SpartaPermission>> defaultAllowedFlows = new HashMap<SpartaPermission, Set<SpartaPermission>>();
    	HashSet<SpartaPermission> sinkSet = new HashSet<SpartaPermission>(1);
    	sinkSet.add(SpartaPermission.CONDITIONAL);

    	if(strictConditionals){
        	defaultAllowedFlows.put(SpartaPermission.LITERAL, sinkSet);
    	}else{
        	defaultAllowedFlows.put(SpartaPermission.ANY, sinkSet);
    	}

    	return defaultAllowedFlows;
    }

    public Pair<Set<SpartaPermission>, Set<SpartaPermission>> annotatedTypeMirrorToFlows(final AnnotatedTypeMirror atm) {

        final AnnotationMirror sourceAnno = atm.getAnnotation(Sources.class);
        final AnnotationMirror sinkAnno   = atm.getAnnotation(Sinks.class);
        

        final Set<SpartaPermission> sources = FlowUtil.getSources(sourceAnno, true);
        final Set<SpartaPermission>   sinks   = FlowUtil.getSinks(sinkAnno, true); ;

        return Pair.of(sources, sinks);
    }

    public boolean areFlowsAllowed(final AnnotatedTypeMirror atm) {
    	final AnnotationMirror polySourceAnno = atm.getAnnotation(PolySources.class);
        final AnnotationMirror polySinkAnno   = atm.getAnnotation(PolySinks.class);
        final AnnotationMirror polyAllAnno   = atm.getAnnotation(PolyAll.class);
        //If the type is marked with poly flow source or poly flow sink, 
        //Then the flow is allowed.
        if(polySinkAnno != null || polySourceAnno != null || polyAllAnno != null){
        	return true;
        }
         
	return areFlowsAllowed(annotatedTypeMirrorToFlows(atm));
    }

    public List<Flow> forbiddenFlows(
	    final AnnotatedTypeMirror atm) {
	return forbiddenFlows(annotatedTypeMirrorToFlows(atm));
    }

    public List<Flow> forbiddenFlows(
	final Pair<Set<SpartaPermission>, Set<SpartaPermission>> flows) {
	final Set<SpartaPermission> sources = flows.first;
	final Set<SpartaPermission> sinks = flows.second;
	
	FlowUtil.allToAnySink(sinks, true);
	FlowUtil.allToAnySource(sources, true);
	List<Flow> forflows = new ArrayList<Flow>();
	
	if(sources.isEmpty() || sinks.isEmpty()){
	    forflows.add(new Flow(sources, sinks));
	    return forflows;
	} 

	if (sinksFromAnySource != null) {
	    sinks.removeAll(sinksFromAnySource);
	}
	   
		
	for (final SpartaPermission source : sources) {
	    final Set<SpartaPermission> allowedSinks = allowedFlows.get(source);

	    if (allowedSinks == null){
		forflows.add(new Flow(source, sinks));
	    }else if(allowedSinks.contains(SpartaPermission.ANY)){
		//Then source->ANY is allowed
	    }else if(!(allowedSinks.containsAll(sinks))) {
		Flow flow = new Flow(source);
		for(SpartaPermission sink : sinks){
		    if(!allowedSinks.contains(sink)){
			flow.addSink(sink);
		    }
		}
		if(flow.hasSinks()){
		    forflows.add(flow);
		}
	    }
	}

	    return forflows;
    }

    public boolean areFlowsAllowed(final Pair<Set<SpartaPermission>, Set<SpartaPermission>> flows) {
        final Set<SpartaPermission> sources = flows.first;
        final Set<SpartaPermission>   sinks   = flows.second;

        if( sources.isEmpty() || sinks.isEmpty() ) {
            return false;
        }

        if(sinksFromAnySource != null) {
            sinks.removeAll(sinksFromAnySource);
        }

        if( sinks.isEmpty() ) {
            return true;
        }

        for(final SpartaPermission source : sources) {
            final Set<SpartaPermission> allowedSinks = allowedFlows.get(source);

            if(allowedSinks == null || !(allowedSinks.contains(SpartaPermission.ANY) ||
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

    private final List<SpartaPermission>   allSinks   =  Collections.unmodifiableList(Arrays.asList(SpartaPermission.values()));
    private final List<SpartaPermission> allSources =  Collections.unmodifiableList(Arrays.asList(SpartaPermission.values()));

    public Set<SpartaPermission> getIntersectingSinks(final Collection<SpartaPermission> sources) {
        return getIntersectingValueSets(SpartaPermission.class, allowedFlows, allSinks, sources);
    }

    public Set<SpartaPermission> getIntersectingSources(final Collection<SpartaPermission> sinks) {
        return getIntersectingValueSets(SpartaPermission.class, reversedAllowedFlows, allSources, sinks);
    }

    /**
     * Read the given file return a one to many Map of SpartaPermission -> Sinks where
     * each entry indicates what sinks a source is given blanket access to reach
     *
     *
     * Format:
     *   A flow policy file is read line by line where each line has the following format
     *   SpartaPermissionName -> SpartaPermissionName, SpartaPermissionName, SpartaPermissionName
     *
     *   SpartaPermissionName  = One of the names of the enums in SpartaPermission
     *   SpartaPermissionName<x> = One of the names of the enums in SpartaPermission
     *
     *   A source can appear twice, the output Sinks for that given source will contain
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

        final Set<SpartaPermission> allSinksButAny = new HashSet<SpartaPermission>(Arrays.asList(SpartaPermission.values()));
        allSinksButAny.remove(SpartaPermission.ANY);


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

                    Set<SpartaPermission> sinks;
                    if(matcher.matches()) {
                        final String sourceStr   = matcher.group(1).trim();
                        final String [] sinkStrs = matcher.group(2).split(",");
                        SpartaPermission source = null;
                        boolean skip = false;

                        if( sourceStr.equals(EMPTY) ) {
                            errors.add(
                                    formatPolicyFileError(policyFile, lineNum,
                                            "Unrecognized source: " + EMPTY +
                                                    " is no longer allowed in policy files",
                                            originalLine)
                            );
                            sinks = null;
                            skip = true;

                        } else {
                            try {
                                source = SpartaPermission.valueOf(sourceStr);

                                sinks = allowedFlows.get(source);
                                if(sinks == null && source != null) {
                                    sinks = new HashSet<SpartaPermission>(sinkStrs.length);
                                    allowedFlows.put(source, sinks);
                                }
                            } catch(final IllegalArgumentException iaExc) {

                                errors.add(
                                        formatPolicyFileError(policyFile, lineNum,
                                                "Unrecognized source: " + sourceStr +
                                                        " Known sources: " + enumValuesToString(SpartaPermission.values()),
                                                originalLine)
                                );

                                sinks = null;
                                skip = true;
                            }

                        }

                        for(final String sink : sinkStrs) {
                            try {
								final String trimmedSink = sink.trim();

								// Read sinks even if source can't be decoded
								// (i.e. source == null)
								// in order to catch all errors in one pass
								final SpartaPermission sinkEnum = SpartaPermission
										.valueOf(trimmedSink);

								if (!skip) {
									sinks.add(sinkEnum);
								}

                            } catch(final IllegalArgumentException iaExc) {
                                errors.add(
                                    formatPolicyFileError(policyFile, lineNum,
                                            "Unrecognized sink: " + sink +
                                            " Known sinks: " + enumValuesToString(SpartaPermission.values()),
                                            originalLine)
                                );
                            }
                        }

                        //TODO: CHECK THIS WITH SUZANNE AND THE TEAM
                        if( !skip && sinks.containsAll( allSinksButAny ) ) {
                            sinks.clear();
                            sinks.add( SpartaPermission.ANY );
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
            } catch (IOException ignoredCloseExc) {
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
        return WHITE_SPACE_PATTERN.matcher(line).matches();
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

    public Set<SpartaPermission> getSinksFromSource(final SpartaPermission source, boolean includeAny) {
        return getSet(SpartaPermission.class, source, allowedFlows, includeAny);
    }

    public Set<SpartaPermission> getSourcesFromSink(final SpartaPermission sink, boolean includeAny) {
        return getSet(SpartaPermission.class, sink, reversedAllowedFlows, includeAny);
    }

    private <KEY extends Enum<KEY>, VALUE extends Enum<VALUE>> Set<VALUE> getSet(
            final Class<KEY> vc,
            final KEY key,
            final Map<KEY, Set<VALUE>> data,
            boolean includeAny) {

        Set<VALUE> values = data.get(key);
        if(includeAny) {
            final KEY any = KEY.valueOf(vc, "ANY");
            Set<VALUE> results = new HashSet<VALUE>();
            if(values != null) {
                results.addAll(values);
            }
            results.addAll(getSet(vc, any, data, false));
            return results;
        } else {
            if(values == null) {
                values = new HashSet<VALUE>();
            }

            return Collections.unmodifiableSet(values);
        }
    }

    public static <KEY, VALUE extends Enum<VALUE>> Set<VALUE> getIntersectingValueSets(final Class<VALUE> vc,
                                                       final Map<KEY, Set<VALUE>> kToV,
                                                       final Collection<VALUE> allValues,
                                                       final Collection<KEY> keys) {
        if(keys.isEmpty()) {
            return new HashSet<VALUE>();
        }

        final List<KEY> keyList = new ArrayList<KEY>(keys);

        final VALUE any = VALUE.valueOf(vc, "ANY");
        Set<VALUE> initial = kToV.get(keyList.get(0));

        final Set<VALUE> values;
        if( initial != null ) {

            if(initial.contains(any)) {
                values = new HashSet<VALUE>(allValues);
                values.remove(any);
            } else {
                values = new HashSet<VALUE>(initial);
            }
        } else {
            values = new HashSet<VALUE>();
        }

        for( int i = 1; i < keyList.size() && !values.isEmpty(); i++) {
            final KEY key = keyList.get(i);
            final Set<VALUE> curValues = kToV.get(key);

            if( curValues != null ) {
                if( !curValues.contains(any) ) {
                    values.retainAll(curValues);
                } //else retain what we currently have as they will be in the intersection with any
            } else {
                values.clear();
            }
        }

        if(values.size() == (allValues.size() - 1) ) {  // (allValues - any)
            values.clear();
            values.add(any);
        }

        return values;
    }

    private <KEY, VALUE> Map<VALUE, Set<KEY>> reverse(final Map<KEY, Set<VALUE>> mapToReverse) {
        final Map<VALUE, Set<KEY>> reversed = new HashMap<VALUE, Set<KEY>>();

        for(final Map.Entry<KEY, Set<VALUE>> keyToValues : mapToReverse.entrySet()) {
            KEY valueInReverse = keyToValues.getKey();

            for(VALUE keyInReverse : keyToValues.getValue()) {
                Set<KEY> newValues = reversed.get(keyInReverse);
                if (newValues == null) {
                    newValues = new HashSet<KEY>();
                    reversed.put(keyInReverse, newValues);
                }
                newValues.add(valueInReverse);
            }
        }

        return reversed;
    }

}

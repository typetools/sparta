package sparta.checkers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import checkers.basetype.BaseTypeChecker;
import checkers.quals.StubFiles;
import checkers.quals.TypeQualifiers;
import checkers.quals.PolyAll;
import checkers.source.SourceChecker;
import checkers.source.SupportedLintOptions;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.QualifierHierarchy;
import checkers.util.AnnotationUtils;
import checkers.util.MultiGraphQualifierHierarchy;
import checkers.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import checkers.util.stub.StubGenerator;
import checkers.util.QualifierPolymorphism;
import checkers.util.TreeUtils;

/*>>>
import checkers.compilermsgs.quals.CompilerMessageKey;
*/

import sparta.checkers.quals.Sink;
import  sparta.checkers.quals.FlowPermission;

import sparta.checkers.quals.Source;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;
import static sparta.checkers.FlowUtil.*;

@TypeQualifiers({Source.class, Sink.class,
    PolySource.class, PolySink.class,
    PolyAll.class})
@StubFiles("flow.astub")
@SupportedOptions({FlowPolicy.POLICY_FILE_OPTION, FlowChecker.MSG_FILTER_OPTION})
@SupportedLintOptions({FlowPolicy.STRICT_CONDITIONALS_OPTION})


public class FlowChecker extends BaseTypeChecker {
    public static final String MSG_FILTER_OPTION = "msgFilter";

	protected AnnotationMirror NOFLOWSOURCES, ANYFLOWSOURCES, POLYFLOWSOURCES;
    protected AnnotationMirror NOFLOWSINKS, ANYFLOWSINKS, POLYFLOWSINKS;
    protected AnnotationMirror POLYALL;
    protected AnnotationMirror LITERALFLOWSOURCE;
    protected AnnotationMirror FROMLITERALFLOWSINK;
    protected AnnotationMirror NRSOURCE, NRSINK;

    protected AnnotationMirror FLOW_SOURCES;
    protected AnnotationMirror FLOW_SINKS;

    protected FlowPolicy flowPolicy;
    protected Set<String> unfilteredMessages;
    //Methods that are not in a stub file
    protected final Map<String, Map<String, Map<Element, Integer>>> notInStubFile;


    public FlowChecker() {
		super();
        this.notInStubFile = new HashMap<>();
	}

	@Override
    public void initChecker() {

        Elements elements = processingEnv.getElementUtils();
        NOFLOWSOURCES = AnnotationUtils.fromClass(elements, Source.class);
        NOFLOWSINKS = AnnotationUtils.fromClass(elements, Sink.class);
        
        POLYFLOWSOURCES = AnnotationUtils.fromClass(elements, PolySource.class);
        POLYFLOWSINKS = AnnotationUtils.fromClass(elements, PolySink.class);
        POLYALL = AnnotationUtils.fromClass(elements, PolyAll.class);
        
        NRSOURCE = FlowUtil.createAnnoFromSource(processingEnv, new HashSet<FlowPermission>(Arrays.asList(FlowPermission.NOT_REVIEWED)));
        NRSINK = FlowUtil.createAnnoFromSink(processingEnv, new HashSet<FlowPermission>(Arrays.asList(FlowPermission.NOT_REVIEWED)));


        ANYFLOWSOURCES = FlowUtil.createAnnoFromSource(processingEnv, new HashSet<FlowPermission>(Arrays.asList(FlowPermission.ANY)));
        ANYFLOWSINKS = FlowUtil.createAnnoFromSink(processingEnv, new HashSet<FlowPermission>(Arrays.asList(FlowPermission.ANY)));

        FLOW_SOURCES = AnnotationUtils.fromClass(elements, Source.class);
        FLOW_SINKS   = AnnotationUtils.fromClass(elements, Sink.class);

		sourceValue = TreeUtils.getMethod("sparta.checkers.quals.Source", "value", 0, processingEnv);
        sinkValue = TreeUtils.getMethod("sparta.checkers.quals.Sink", "value", 0, processingEnv);

        super.initChecker();
        //Must call super.initChecker before the lint option can be checked.
        final boolean scArg = getLintOption(FlowPolicy.STRICT_CONDITIONALS_OPTION, false);
        final String pfArg = processingEnv.getOptions().get(FlowPolicy.POLICY_FILE_OPTION);


        if (pfArg == null || pfArg.trim().isEmpty()) {
           flowPolicy = new FlowPolicy(scArg);
        } else {
           flowPolicy = new FlowPolicy(new File(pfArg),scArg);
        }

        LITERALFLOWSOURCE = FlowUtil.createAnnoFromSource(processingEnv, new HashSet<FlowPermission>(Arrays.asList(FlowPermission.LITERAL)));

        final Set<FlowPermission> literalSink = new HashSet<FlowPermission>(flowPolicy.getSinkFromSource(FlowPermission.LITERAL, true));
        FROMLITERALFLOWSINK = FlowUtil.createAnnoFromSink(processingEnv, literalSink);


        String unfilteredStr = processingEnv.getOptions().get(MSG_FILTER_OPTION);
        if(unfilteredStr == null) {
            unfilteredMessages = null;
        } else {
            final String [] unfilteredMsgs = unfilteredStr.split(":");
            unfilteredMessages = new HashSet<String>();
            for(final String unfilteredMsg : unfilteredMsgs) {
                if(!unfilteredMsg.trim().isEmpty()) {
                    unfilteredMessages.add(unfilteredMsg.trim());
                }
            }

            if(unfilteredMessages.isEmpty()) {
                unfilteredMessages = null;
            }
        }
    }

    protected ExecutableElement sourceValue;
    protected ExecutableElement sinkValue;

    @SuppressWarnings("unchecked")
    public List<FlowPermission> getSource(AnnotatedTypeMirror atm) {
        AnnotationMirror anno = atm.getAnnotationInHierarchy(ANYFLOWSOURCES);
        AnnotationValue sourcesValue = AnnotationUtils.getElementValuesWithDefaults(anno).get(sourceValue);
        // TODO: Should we add NONE as an enum constant?
        if (sourcesValue == null) { // || ((List<FlowPermission>)sourcesValue.getValue()).isEmpty()) {
            return Collections.emptyList(); // singletonList(FlowPermission.NONE);
        } else {
            return (List<FlowPermission>) sourcesValue.getValue();
        }
    }

    @SuppressWarnings("unchecked")
    public List<FlowPermission> getSink(AnnotatedTypeMirror atm) {
        AnnotationMirror anno = atm.getAnnotationInHierarchy(ANYFLOWSINKS);
        AnnotationValue sinksValue = AnnotationUtils.getElementValuesWithDefaults(anno).get(sinkValue);
        if (sinksValue == null) {
            return Collections.emptyList();
        } else {
            return (List<FlowPermission>) sinksValue.getValue();
        }
    }
    public void typeProcessingOver() {
        printMethods();
        super.typeProcessingOver();
    }
    
    //TODO: would be nice if you could pass a file name
    private final String printMissMethod = "missingAPI.astub";
    //TODO: would be nice if there was a command line argument to turn this on and off
	private boolean printFrequency = true;

    private void printMethods() {
    	if (notInStubFile.isEmpty()) return;
        PrintStream out;
        int methodCount = 0;
		try {
			out = new PrintStream(new File(printMissMethod));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
        for (String pack : notInStubFile.keySet()) {
            out.println("package " + pack + ";");
            for (String clss : notInStubFile.get(pack).keySet()) {
                out.println("class " + clss + "{");
                Map<Element, Integer> map = notInStubFile.get(pack).get(clss);
                for (Element element : map.keySet()) {
                    StubGenerator stubGen = new StubGenerator(out);
                    if(printFrequency )
                    out.println("    //" + map.get(element)+" ("+element.getSimpleName()+")");
                    stubGen.skeletonFromMethod(element);
                    methodCount++;
                }
                out.println("}");
            }
        }
        System.err.println(methodCount+" methods to annotate.");
    }
    
   


    @Override
    protected MultiGraphQualifierHierarchy.MultiGraphFactory createQualifierHierarchyFactory() {
        return new MultiGraphQualifierHierarchy.MultiGraphFactory(this);
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new FlowQualifierHierarchy(factory);
    }

    private class FlowQualifierHierarchy extends MultiGraphQualifierHierarchy {

        protected FlowQualifierHierarchy(MultiGraphFactory f) {
            super(f);
        }

        @Override
        protected Set<AnnotationMirror>
        findBottoms(Map<AnnotationMirror, Set<AnnotationMirror>> supertypes) {
            Set<AnnotationMirror> newbottoms = AnnotationUtils.createAnnotationSet();
            newbottoms.add(NOFLOWSOURCES);
            newbottoms.add(ANYFLOWSINKS);
            return newbottoms;
        }

        @Override
        protected Set<AnnotationMirror>
        findTops(Map<AnnotationMirror, Set<AnnotationMirror>> supertypes) {
            Set<AnnotationMirror> newtops = AnnotationUtils.createAnnotationSet();
            newtops.add(ANYFLOWSOURCES);
            newtops.add(NOFLOWSINKS);
            return newtops;
        }

        private boolean isSourceQualifier(AnnotationMirror anno) {
            return NOFLOWSOURCES.getAnnotationType().equals(anno.getAnnotationType()) ||
                    isPolySourceQualifier(anno);
        }

        private boolean isPolySourceQualifier(AnnotationMirror anno) {
            return POLYFLOWSOURCES.getAnnotationType().equals(anno.getAnnotationType());
        }

        private boolean isSinkQualifier(AnnotationMirror anno) {
            return NOFLOWSINKS.getAnnotationType().equals(anno.getAnnotationType()) ||
                    isPolySinkQualifier(anno);
        }

        private boolean isPolySinkQualifier(AnnotationMirror anno) {
            return POLYFLOWSINKS.getAnnotationType().equals(anno.getAnnotationType());
        }

        @Override
        public AnnotationMirror getTopAnnotation(AnnotationMirror start) {
            if (isSourceQualifier(start)) {
                return ANYFLOWSOURCES;
            } else if (isSinkQualifier(start)) {
                return NOFLOWSINKS;
            } else if (QualifierPolymorphism.isPolyAll(start)) {
                return POLYALL;
            } else {
                SourceChecker.errorAbort("FlowChecker: unexpected AnnotationMirror: " + start);
                return null; // dead code
            }
        }

        @Override
        public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
	    try {
		checkAny(rhs);
		checkAny(lhs);
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		System.out.println(e.getMessage());
		// e.printStackTrace();
		// System.exit(0);
	    }
        	
            if (isSourceQualifier(rhs)) {
                if (isPolySourceQualifier(lhs)) {
                    // If LHS is poly, rhs has to be bottom or poly qualifier.
                    return AnnotationUtils.areSame(rhs, NOFLOWSOURCES) ||
                            AnnotationUtils.areSame(rhs, POLYFLOWSOURCES) ||
                            AnnotationUtils.areSame(rhs, POLYALL);
                } else if (isPolySourceQualifier(rhs)) {
                    // If RHS is poly, lhs has to be top or poly qualifier.
                    return AnnotationUtils.areSame(lhs, ANYFLOWSOURCES) ||
                            AnnotationUtils.areSame(lhs, POLYFLOWSOURCES) ||
                            AnnotationUtils.areSame(lhs, POLYALL);
                } else {
                    if (!isSourceQualifier(lhs)) {
                        return false;
                    }
                    List<FlowPermission> lhssrc = FlowUtil.getSource(lhs);
                    List<FlowPermission> rhssrc = FlowUtil.getSource(rhs);
                    return  AnnotationUtils.areSame(lhs, ANYFLOWSOURCES) || //TODO: Remove the ANY below when we start warning about Source(ANY, Something else)
                            lhssrc.containsAll(rhssrc) || lhssrc.contains(FlowPermission.ANY);
                }
            } else if (isSinkQualifier(rhs)) {
                if (isPolySinkQualifier(lhs)) {
                    // If LHS is poly, rhs has to be bottom or poly qualifier.
                    return AnnotationUtils.areSame(rhs, ANYFLOWSINKS) ||
                            AnnotationUtils.areSame(rhs, POLYFLOWSINKS);
                } else if (isPolySinkQualifier(rhs)) {
                    // If RHS is poly, lhs has to be top or poly qualifier.
                    return AnnotationUtils.areSame(lhs, NOFLOWSINKS) ||
                            AnnotationUtils.areSame(lhs, POLYFLOWSINKS);
                } else {
                    if (!isSinkQualifier(lhs)) {
                        return false;
                    }
                    List<FlowPermission> lhssnk = FlowUtil.getSink(lhs);
                    List<FlowPermission> rhssnk = FlowUtil.getSink(rhs);
                    return lhssnk.isEmpty() ||
                            rhssnk.containsAll(lhssnk) ||
                        (rhssnk.contains(FlowPermission.ANY) && rhssnk.size()==1);
                }
            } else if (QualifierPolymorphism.isPolyAll(rhs)) {
                // If RHS is polyall, the LHS has to be a top qualifier or also poly.
                return AnnotationUtils.areSame(lhs, NOFLOWSINKS) ||
                        AnnotationUtils.areSame(lhs, ANYFLOWSOURCES) ||
                        AnnotationUtils.areSame(lhs, POLYFLOWSINKS) ||
                        AnnotationUtils.areSame(lhs, POLYFLOWSOURCES);

            } else {
                SourceChecker.errorAbort("FlowChecker: unexpected AnnotationMirrors: " + rhs + " and " + lhs);
                return false; // dead code
            }
        }

	private void checkAny(AnnotationMirror anm) throws Exception {
	    boolean isPolySink = AnnotationUtils.areSame(anm, POLYFLOWSINKS);
	    boolean isPolySource = AnnotationUtils.areSame(anm, POLYFLOWSOURCES);
	  
	    if (!isPolySource && isSourceQualifier(anm)) {
		List<FlowPermission> sources = FlowUtil.getSource(anm);
		if (sources.contains(FlowPermission.ANY) && sources.size() > 1) {
		    throw new Exception(
			    "Found FlowPermission.ANY and something else");
		}
	    }
	    if (!isPolySink && isSinkQualifier(anm)) {
		List<FlowPermission> sinks = FlowUtil.getSink(anm);
		if (sinks.contains(FlowPermission.ANY) && sinks.size() > 1) {
		    throw new Exception("Found FlowPermission.ANY and something else");
		}
	    }

	}

		@Override
        protected void addPolyRelations(QualifierHierarchy qualHierarchy,
                Map<AnnotationMirror, Set<AnnotationMirror>> fullMap,
                Map<AnnotationMirror, AnnotationMirror> polyQualifiers,
                Set<AnnotationMirror> tops, Set<AnnotationMirror> bottoms) {
            AnnotationUtils.updateMappingToImmutableSet(fullMap, NOFLOWSOURCES, Collections.singleton(POLYALL));
            AnnotationUtils.updateMappingToImmutableSet(fullMap, NOFLOWSOURCES, Collections.singleton(POLYFLOWSOURCES));
            AnnotationUtils.updateMappingToImmutableSet(fullMap, ANYFLOWSINKS, Collections.singleton(POLYALL));
            AnnotationUtils.updateMappingToImmutableSet(fullMap, ANYFLOWSINKS, Collections.singleton(POLYFLOWSINKS));
            Set<AnnotationMirror> polyallTops = AnnotationUtils.createAnnotationSet();
            polyallTops.add(ANYFLOWSOURCES);
            polyallTops.add(NOFLOWSINKS);
            AnnotationUtils.updateMappingToImmutableSet(fullMap, POLYALL, polyallTops);
            AnnotationUtils.updateMappingToImmutableSet(fullMap, POLYFLOWSOURCES, Collections.singleton(ANYFLOWSOURCES));
            AnnotationUtils.updateMappingToImmutableSet(fullMap, POLYFLOWSINKS, Collections.singleton(NOFLOWSINKS));
        }

        @Override
        public AnnotationMirror leastUpperBound(AnnotationMirror a1, AnnotationMirror a2) {

            if (isSubtype(a1, a2))
                return a2;
            if (isSubtype(a2, a1))
                return a1;

            if (AnnotationUtils.areSameIgnoringValues(a1, a2)) {
                if( AnnotationUtils.areSameIgnoringValues(a1, FLOW_SOURCES) ) {
                    final Set<FlowPermission> superset = FlowUtil.getSource(a1, true);
                    superset.addAll(FlowUtil.getSource(a2, true));
                    FlowUtil.allToAnySource(superset, true);
                    return boundSource(superset);

                } else if( AnnotationUtils.areSameIgnoringValues(a1, FLOW_SINKS) ) {
                    final Set<FlowPermission> intersection =  FlowUtil.getSink(a1, true);
                    intersection.retainAll(FlowUtil.getSink(a2, true));
                    FlowUtil.allToAnySink(intersection, true);
                    return boundSink(intersection);

                }
                //Poly Flows must be handled as if they are Top Type   
            }else if(AnnotationUtils.areSame(a1, POLYFLOWSINKS)){
        	if( AnnotationUtils.areSameIgnoringValues(a2, FLOW_SINKS) ) {
        	    return boundSink(new HashSet<FlowPermission>());
        	}
        	
            }else if(AnnotationUtils.areSame(a2, POLYFLOWSINKS)){
        	if( AnnotationUtils.areSameIgnoringValues(a1, FLOW_SINKS) ) {
        	    return boundSink(new HashSet<FlowPermission>());
        	}
            }else if(AnnotationUtils.areSame(a1, POLYFLOWSOURCES)){
        	if( AnnotationUtils.areSameIgnoringValues(a2, FLOW_SOURCES) ) {
                    Set<FlowPermission> top = new HashSet<FlowPermission>();
                    top.add(FlowPermission.ANY);
                    return boundSource(top);
                }
        	
            }else if(AnnotationUtils.areSame(a2, POLYFLOWSOURCES)){
        	if( AnnotationUtils.areSameIgnoringValues(a1, FLOW_SOURCES) ) {
                    Set<FlowPermission> top = new HashSet<FlowPermission>();
                    top.add(FlowPermission.ANY);
                    return boundSource(top);
                }
            }


            return super.leastUpperBound(a1, a2);
        }

        @Override
        public AnnotationMirror greatestLowerBound(AnnotationMirror a1, AnnotationMirror a2) {

            if( AnnotationUtils.areSame(a1, a2) )
                return a1;

            if (AnnotationUtils.areSameIgnoringValues(a1, a2)) {
                if( AnnotationUtils.areSameIgnoringValues(a1, FLOW_SOURCES) ) {
                    final Set<FlowPermission> intersection = FlowUtil.getSource(a1, true);
                    intersection.retainAll(FlowUtil.getSource(a2, true));
                    FlowUtil.allToAnySource(intersection, true);
                    return boundSource(intersection);

                } else if( AnnotationUtils.areSameIgnoringValues(a1, FLOW_SINKS) ) {
                    final Set<FlowPermission> superSet =  FlowUtil.getSink(a1, true);
                    superSet.addAll(FlowUtil.getSink(a2, true));
                    FlowUtil.allToAnySink(superSet, true);
                    return boundSink(superSet);

                }
             //Poly Flows must be handled as if they are Bottom Type   
            }else if(AnnotationUtils.areSame(a1, POLYFLOWSINKS)){
        	if( AnnotationUtils.areSameIgnoringValues(a2, FLOW_SINKS) ) {
        	    Set<FlowPermission> bottom = new HashSet<FlowPermission>();
        	    bottom.add(FlowPermission.ANY);
        	    return boundSink(bottom);
        	}
        	
            }else if(AnnotationUtils.areSame(a2, POLYFLOWSINKS)){
        	if( AnnotationUtils.areSameIgnoringValues(a1, FLOW_SINKS) ) {
        	    Set<FlowPermission> bottom = new HashSet<FlowPermission>();
        	    bottom.add(FlowPermission.ANY);
        	    return boundSink(bottom);
        	}
            }else if(AnnotationUtils.areSame(a1, POLYFLOWSOURCES)){
        	if( AnnotationUtils.areSameIgnoringValues(a2, FLOW_SOURCES) ) {
                    return boundSource(new HashSet<FlowPermission>());
                }
        	
            }else if(AnnotationUtils.areSame(a2, POLYFLOWSOURCES)){
        	if( AnnotationUtils.areSameIgnoringValues(a1, FLOW_SOURCES) ) {
                    return boundSource(new HashSet<FlowPermission>());
                }
            }
            return super.greatestLowerBound(a1, a2);
        }


        private AnnotationMirror boundSource(final Set<FlowPermission> flowSource) {

            final AnnotationMirror am;
            if( flowSource.contains(FlowPermission.ANY) ) { //contains all Source
                am = getTopAnnotation(FLOW_SOURCES);
            } else if(flowSource.isEmpty()) {
                am = getBottomAnnotation(FLOW_SOURCES);
            } else {
                am = createAnnoFromSource( processingEnv, flowSource );
            }
            return am;
        }

        private AnnotationMirror boundSink(final Set<FlowPermission> flowSink) {
            final AnnotationMirror am;
            if( flowSink.isEmpty() ) {
                am = getTopAnnotation(FLOW_SINKS);
            } else if( flowSink.contains(FlowPermission.ANY) ) { //contains all Sink
                am = getBottomAnnotation(FLOW_SINKS);
            } else {
                am = createAnnoFromSink( processingEnv, flowSink );
            }
            return am;
        }
    }

    public FlowPolicy getFlowPolicy() {
        return flowPolicy;
    }

    @Override
    protected void message(Diagnostic.Kind kind, Object source, /*@CompilerMessageKey*/ String msgKey,
                           Object... args) {
        if( unfilteredMessages == null || unfilteredMessages.contains(msgKey)) {
            super.message(kind, source, msgKey, args);
        }
    }
}

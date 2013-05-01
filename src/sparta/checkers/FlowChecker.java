package sparta.checkers;

import java.io.File;
import java.util.*;

import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
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
import checkers.util.AnnotationBuilder;
import checkers.util.AnnotationUtils;
import checkers.util.MultiGraphQualifierHierarchy;
import checkers.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import checkers.util.QualifierPolymorphism;
import checkers.util.TreeUtils;

/*>>>
import checkers.compilermsgs.quals.CompilerMessageKey;
*/

import sparta.checkers.quals.Sinks;
import static sparta.checkers.quals.SPARTA_Permission.*;
import  sparta.checkers.quals.SPARTA_Permission;

import sparta.checkers.quals.Sources;
import sparta.checkers.quals.PolySinks;
import sparta.checkers.quals.PolySources;
import static sparta.checkers.FlowUtil.*;

@TypeQualifiers({Sources.class, Sinks.class,
    PolySources.class, PolySinks.class,
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

    protected AnnotationMirror FLOW_SOURCES;
    protected AnnotationMirror FLOW_SINKS;

    protected FlowPolicy flowPolicy;
    protected Set<String> unfilteredMessages;

    @Override
    public void initChecker() {
        Elements elements = processingEnv.getElementUtils();
        NOFLOWSOURCES = AnnotationUtils.fromClass(elements, Sources.class);
        NOFLOWSINKS = AnnotationUtils.fromClass(elements, Sinks.class);
        POLYFLOWSOURCES = AnnotationUtils.fromClass(elements, PolySources.class);
        POLYFLOWSINKS = AnnotationUtils.fromClass(elements, PolySinks.class);
        POLYALL = AnnotationUtils.fromClass(elements, PolyAll.class);

        ANYFLOWSOURCES = FlowUtil.createAnnoFromSources(processingEnv, new HashSet<SPARTA_Permission>(Arrays.asList(SPARTA_Permission.ANY)));
        ANYFLOWSINKS = FlowUtil.createAnnoFromSinks(processingEnv, new HashSet<SPARTA_Permission>(Arrays.asList(SPARTA_Permission.ANY)));

        FLOW_SOURCES = AnnotationUtils.fromClass(elements, Sources.class);
        FLOW_SINKS   = AnnotationUtils.fromClass(elements, Sinks.class);

        sourceValue = TreeUtils.getMethod("sparta.checkers.quals.Sources", "value", 0, processingEnv);
        sinkValue = TreeUtils.getMethod("sparta.checkers.quals.Sinks", "value", 0, processingEnv);

        super.initChecker();
        //Must call super.initChecker before the lint option can be checked.
        final boolean scArg = getLintOption(FlowPolicy.STRICT_CONDITIONALS_OPTION, false);
        final String pfArg = processingEnv.getOptions().get(FlowPolicy.POLICY_FILE_OPTION);


        if (pfArg == null || pfArg.trim().isEmpty()) {
           flowPolicy = new FlowPolicy(scArg);
        } else {
           flowPolicy = new FlowPolicy(new File(pfArg),scArg);
        }

        LITERALFLOWSOURCE = FlowUtil.createAnnoFromSources(processingEnv, new HashSet<SPARTA_Permission>(Arrays.asList(SPARTA_Permission.LITERAL)));

        final Set<SPARTA_Permission> literalSinks = new HashSet<SPARTA_Permission>(flowPolicy.getSinksFromSource(SPARTA_Permission.LITERAL, true));
        FROMLITERALFLOWSINK = FlowUtil.createAnnoFromSinks(processingEnv, literalSinks);


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
    public List<SPARTA_Permission> getSources(AnnotatedTypeMirror atm) {
        AnnotationMirror anno = atm.getAnnotationInHierarchy(ANYFLOWSOURCES);
        AnnotationValue sourcesValue = AnnotationUtils.getElementValuesWithDefaults(anno).get(sourceValue);
        // TODO: Should we add NONE as an enum constant?
        if (sourcesValue == null) { // || ((List<SPARTA_Permission>)sourcesValue.getValue()).isEmpty()) {
            return Collections.emptyList(); // singletonList(SPARTA_Permission.NONE);
        } else {
            return (List<SPARTA_Permission>) sourcesValue.getValue();
        }
    }

    @SuppressWarnings("unchecked")
    public List<SPARTA_Permission> getSinks(AnnotatedTypeMirror atm) {
        AnnotationMirror anno = atm.getAnnotationInHierarchy(ANYFLOWSINKS);
        AnnotationValue sinksValue = AnnotationUtils.getElementValuesWithDefaults(anno).get(sinkValue);
        if (sinksValue == null) {
            return Collections.emptyList();
        } else {
            return (List<SPARTA_Permission>) sinksValue.getValue();
        }
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
                    List<SPARTA_Permission> lhssrc = FlowUtil.getSources(lhs);
                    List<SPARTA_Permission> rhssrc = FlowUtil.getSources(rhs);
                    return  AnnotationUtils.areSame(lhs, ANYFLOWSOURCES) || //TODO: Remove the ANY below when we start warning about Sources(ANY, Something else)
                            lhssrc.containsAll(rhssrc) || lhssrc.contains(SPARTA_Permission.ANY);
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
                    List<SPARTA_Permission> lhssnk = FlowUtil.getSinks(lhs);
                    List<SPARTA_Permission> rhssnk = FlowUtil.getSinks(rhs);
                    return lhssnk.isEmpty() ||
                            rhssnk.containsAll(lhssnk) ||
                        (rhssnk.contains(SPARTA_Permission.ANY) && rhssnk.size()==1);
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
		List<SPARTA_Permission> sources = FlowUtil.getSources(anm);
		if (sources.contains(SPARTA_Permission.ANY) && sources.size() > 1) {
		    throw new Exception(
			    "Found SPARTA_Permission.ANY and something else");
		}
	    }
	    if (!isPolySink && isSinkQualifier(anm)) {
		List<SPARTA_Permission> sinks = FlowUtil.getSinks(anm);
		if (sinks.contains(SPARTA_Permission.ANY) && sinks.size() > 1) {
		    throw new Exception("Found SPARTA_Permission.ANY and something else");
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
                    final Set<SPARTA_Permission> superset = FlowUtil.getSources(a1, true);
                    superset.addAll(FlowUtil.getSources(a2, true));
                    FlowUtil.allToAnySource(superset, true);
                    return boundSources(superset);

                } else if( AnnotationUtils.areSameIgnoringValues(a1, FLOW_SINKS) ) {
                    final Set<SPARTA_Permission> intersection =  FlowUtil.getSinks(a1, true);
                    intersection.retainAll(FlowUtil.getSinks(a2, true));
                    FlowUtil.allToAnySink(intersection, true);
                    return boundSinks(intersection);

                }
                //Poly Flows must be handled as if they are Top Type   
            }else if(AnnotationUtils.areSame(a1, POLYFLOWSINKS)){
        	if( AnnotationUtils.areSameIgnoringValues(a2, FLOW_SINKS) ) {
        	    return boundSinks(new HashSet<SPARTA_Permission>());
        	}
        	
            }else if(AnnotationUtils.areSame(a2, POLYFLOWSINKS)){
        	if( AnnotationUtils.areSameIgnoringValues(a1, FLOW_SINKS) ) {
        	    return boundSinks(new HashSet<SPARTA_Permission>());
        	}
            }else if(AnnotationUtils.areSame(a1, POLYFLOWSOURCES)){
        	if( AnnotationUtils.areSameIgnoringValues(a2, FLOW_SOURCES) ) {
                    Set<SPARTA_Permission> top = new HashSet<SPARTA_Permission>();
                    top.add(SPARTA_Permission.ANY);
                    return boundSources(top);
                }
        	
            }else if(AnnotationUtils.areSame(a2, POLYFLOWSOURCES)){
        	if( AnnotationUtils.areSameIgnoringValues(a1, FLOW_SOURCES) ) {
                    Set<SPARTA_Permission> top = new HashSet<SPARTA_Permission>();
                    top.add(SPARTA_Permission.ANY);
                    return boundSources(top);
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
                    final Set<SPARTA_Permission> intersection = FlowUtil.getSources(a1, true);
                    intersection.retainAll(FlowUtil.getSources(a2, true));
                    FlowUtil.allToAnySource(intersection, true);
                    return boundSources(intersection);

                } else if( AnnotationUtils.areSameIgnoringValues(a1, FLOW_SINKS) ) {
                    final Set<SPARTA_Permission> superSet =  FlowUtil.getSinks(a1, true);
                    superSet.addAll(FlowUtil.getSinks(a2, true));
                    FlowUtil.allToAnySink(superSet, true);
                    return boundSinks(superSet);

                }
             //Poly Flows must be handled as if they are Bottom Type   
            }else if(AnnotationUtils.areSame(a1, POLYFLOWSINKS)){
        	if( AnnotationUtils.areSameIgnoringValues(a2, FLOW_SINKS) ) {
        	    Set<SPARTA_Permission> bottom = new HashSet<SPARTA_Permission>();
        	    bottom.add(SPARTA_Permission.ANY);
        	    return boundSinks(bottom);
        	}
        	
            }else if(AnnotationUtils.areSame(a2, POLYFLOWSINKS)){
        	if( AnnotationUtils.areSameIgnoringValues(a1, FLOW_SINKS) ) {
        	    Set<SPARTA_Permission> bottom = new HashSet<SPARTA_Permission>();
        	    bottom.add(SPARTA_Permission.ANY);
        	    return boundSinks(bottom);
        	}
            }else if(AnnotationUtils.areSame(a1, POLYFLOWSOURCES)){
        	if( AnnotationUtils.areSameIgnoringValues(a2, FLOW_SOURCES) ) {
                    return boundSources(new HashSet<SPARTA_Permission>());
                }
        	
            }else if(AnnotationUtils.areSame(a2, POLYFLOWSOURCES)){
        	if( AnnotationUtils.areSameIgnoringValues(a1, FLOW_SOURCES) ) {
                    return boundSources(new HashSet<SPARTA_Permission>());
                }
            }
            return super.greatestLowerBound(a1, a2);
        }


        private AnnotationMirror boundSources(final Set<SPARTA_Permission> flowSources) {

            final AnnotationMirror am;
            if( flowSources.contains(SPARTA_Permission.ANY) ) { //contains all Sources
                am = getTopAnnotation(FLOW_SOURCES);
            } else if(flowSources.isEmpty()) {
                am = getBottomAnnotation(FLOW_SOURCES);
            } else {
                am = createAnnoFromSources( processingEnv, flowSources );
            }
            return am;
        }

        private AnnotationMirror boundSinks(final Set<SPARTA_Permission> flowSinks) {
            final AnnotationMirror am;
            if( flowSinks.isEmpty() ) {
                am = getTopAnnotation(FLOW_SINKS);
            } else if( flowSinks.contains(SPARTA_Permission.ANY) ) { //contains all Sinks
                am = getBottomAnnotation(FLOW_SINKS);
            } else {
                am = createAnnoFromSinks( processingEnv, flowSinks );
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

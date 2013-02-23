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

import sparta.checkers.quals.FlowSinks;
import sparta.checkers.quals.FlowSinks.FlowSink;
import sparta.checkers.quals.FlowSources;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.PolyFlowSinks;
import sparta.checkers.quals.PolyFlowSources;
import static sparta.checkers.FlowUtil.*;

@TypeQualifiers({FlowSources.class, FlowSinks.class,
    PolyFlowSources.class, PolyFlowSinks.class,
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
        NOFLOWSOURCES = AnnotationUtils.fromClass(elements, FlowSources.class);
        NOFLOWSINKS = AnnotationUtils.fromClass(elements, FlowSinks.class);
        POLYFLOWSOURCES = AnnotationUtils.fromClass(elements, PolyFlowSources.class);
        POLYFLOWSINKS = AnnotationUtils.fromClass(elements, PolyFlowSinks.class);
        POLYALL = AnnotationUtils.fromClass(elements, PolyAll.class);

        ANYFLOWSOURCES = FlowUtil.createAnnoFromSources(processingEnv, new HashSet<FlowSource>(Arrays.asList(FlowSource.ANY)));
        ANYFLOWSINKS = FlowUtil.createAnnoFromSinks(processingEnv, new HashSet<FlowSink>(Arrays.asList(FlowSink.ANY)));

        FLOW_SOURCES = AnnotationUtils.fromClass(elements, FlowSources.class);
        FLOW_SINKS   = AnnotationUtils.fromClass(elements, FlowSinks.class);

        sourceValue = TreeUtils.getMethod("sparta.checkers.quals.FlowSources", "value", 0, processingEnv);
        sinkValue = TreeUtils.getMethod("sparta.checkers.quals.FlowSinks", "value", 0, processingEnv);

        super.initChecker();
        //Must call super.initChecker before the lint option can be checked.
        final boolean scArg = getLintOption(FlowPolicy.STRICT_CONDITIONALS_OPTION, false);
        final String pfArg = processingEnv.getOptions().get(FlowPolicy.POLICY_FILE_OPTION);


        if (pfArg == null || pfArg.trim().isEmpty()) {
           flowPolicy = new FlowPolicy(scArg);
        } else {
           flowPolicy = new FlowPolicy(new File(pfArg),scArg);
        }

        LITERALFLOWSOURCE = FlowUtil.createAnnoFromSources(processingEnv, new HashSet<FlowSource>(Arrays.asList(FlowSource.LITERAL)));

        final Set<FlowSink> literalSinks = new HashSet<FlowSink>(flowPolicy.getSinksFromSource(FlowSource.LITERAL, true));
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
    public List<FlowSource> getFlowSources(AnnotatedTypeMirror atm) {
        AnnotationMirror anno = atm.getAnnotationInHierarchy(ANYFLOWSOURCES);
        AnnotationValue sourcesValue = AnnotationUtils.getElementValuesWithDefaults(anno).get(sourceValue);
        // TODO: Should we add NONE as an enum constant?
        if (sourcesValue == null) { // || ((List<FlowSource>)sourcesValue.getValue()).isEmpty()) {
            return Collections.emptyList(); // singletonList(FlowSink.NONE);
        } else {
            return (List<FlowSource>) sourcesValue.getValue();
        }
    }

    @SuppressWarnings("unchecked")
    public List<FlowSink> getFlowSinks(AnnotatedTypeMirror atm) {
        AnnotationMirror anno = atm.getAnnotationInHierarchy(ANYFLOWSINKS);
        AnnotationValue sinksValue = AnnotationUtils.getElementValuesWithDefaults(anno).get(sinkValue);
        if (sinksValue == null) {
            return Collections.emptyList();
        } else {
            return (List<FlowSink>) sinksValue.getValue();
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
                    List<FlowSource> lhssrc = FlowUtil.getFlowSources(lhs);
                    List<FlowSource> rhssrc = FlowUtil.getFlowSources(rhs);
                    return  AnnotationUtils.areSame(lhs, ANYFLOWSOURCES) || //TODO: Remove the ANY below when we start warning about FlowSources(ANY, Something else)
                            lhssrc.containsAll(rhssrc) || lhssrc.contains(FlowSource.ANY);
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
                    List<FlowSink> lhssnk = FlowUtil.getFlowSinks(lhs);
                    List<FlowSink> rhssnk = FlowUtil.getFlowSinks(rhs);
                    return lhssnk.isEmpty() ||
                            rhssnk.containsAll(lhssnk) ||
                        (rhssnk.contains(FlowSink.ANY) && rhssnk.size()==1);
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
                    final Set<FlowSources.FlowSource> superset = FlowUtil.getFlowSources(a1, true);
                    superset.addAll(FlowUtil.getFlowSources(a2, true));
                    FlowUtil.allToAnySource(superset, true);
                    return boundFlowSources(superset);

                } else if( AnnotationUtils.areSameIgnoringValues(a1, FLOW_SINKS) ) {
                    final Set<FlowSinks.FlowSink> intersection =  FlowUtil.getFlowSinks(a1, true);
                    intersection.retainAll(FlowUtil.getFlowSinks(a2, true));
                    FlowUtil.allToAnySink(intersection, true);
                    return boundFlowSinks(intersection);

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
                    final Set<FlowSources.FlowSource> intersection = FlowUtil.getFlowSources(a1, true);
                    intersection.retainAll(FlowUtil.getFlowSources(a2, true));
                    FlowUtil.allToAnySource(intersection, true);
                    return boundFlowSources(intersection);

                } else if( AnnotationUtils.areSameIgnoringValues(a1, FLOW_SINKS) ) {
                    final Set<FlowSinks.FlowSink> superSet =  FlowUtil.getFlowSinks(a1, true);
                    superSet.addAll(FlowUtil.getFlowSinks(a2, true));
                    FlowUtil.allToAnySink(superSet, true);
                    return boundFlowSinks(superSet);

                }
            }

            return super.greatestLowerBound(a1, a2);
        }


        private AnnotationMirror boundFlowSources(final Set<FlowSource> flowSources) {

            final AnnotationMirror am;
            if( flowSources.contains(FlowSource.ANY) ) { //contains all FlowSources
                am = getTopAnnotation(FLOW_SOURCES);
            } else if(flowSources.isEmpty()) {
                am = getBottomAnnotation(FLOW_SOURCES);
            } else {
                am = createAnnoFromSources( processingEnv, flowSources );
            }
            return am;
        }

        private AnnotationMirror boundFlowSinks(final Set<FlowSink> flowSinks) {
            final AnnotationMirror am;
            if( flowSinks.isEmpty() ) {
                am = getTopAnnotation(FLOW_SINKS);
            } else if( flowSinks.contains(FlowSink.ANY) ) { //contains all FlowSinks
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

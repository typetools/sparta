package sparta.checkers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;

import checkers.basetype.BaseTypeChecker;
import checkers.quals.StubFiles;
import checkers.quals.TypeQualifiers;
import checkers.quals.PolyAll;
import checkers.source.SourceChecker;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.QualifierHierarchy;
import checkers.util.AnnotationUtils;
import checkers.util.MultiGraphQualifierHierarchy;
import checkers.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import checkers.util.QualifierPolymorphism;
import checkers.util.TreeUtils;

import sparta.checkers.quals.FlowSinks;
import sparta.checkers.quals.FlowSinks.FlowSink;
import sparta.checkers.quals.FlowSources;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.PolyFlowSinks;
import sparta.checkers.quals.PolyFlowSources;

@TypeQualifiers({FlowSources.class, FlowSinks.class,
    PolyFlowSources.class, PolyFlowSinks.class,
    PolyAll.class})
@StubFiles("flow.astub")
public class FlowChecker extends BaseTypeChecker {

    protected AnnotationMirror NOFLOWSOURCES, ANYFLOWSOURCES, POLYFLOWSOURCES;
    protected AnnotationMirror NOFLOWSINKS, ANYFLOWSINKS, POLYFLOWSINKS;
    protected AnnotationMirror POLYALL;

    @Override
    public void initChecker() {
        Elements elements = processingEnv.getElementUtils();
        NOFLOWSOURCES = AnnotationUtils.fromClass(elements, FlowSources.class);
        NOFLOWSINKS = AnnotationUtils.fromClass(elements, FlowSinks.class);
        POLYFLOWSOURCES = AnnotationUtils.fromClass(elements, PolyFlowSources.class);
        POLYFLOWSINKS = AnnotationUtils.fromClass(elements, PolyFlowSinks.class);
        POLYALL = AnnotationUtils.fromClass(elements, PolyAll.class);

        AnnotationUtils.AnnotationBuilder builder =
                new AnnotationUtils.AnnotationBuilder(processingEnv, FlowSources.class.getCanonicalName());
        builder.setValue("value", new FlowSource[] { FlowSource.ANY });
        ANYFLOWSOURCES = builder.build();

        builder = new AnnotationUtils.AnnotationBuilder(processingEnv, FlowSinks.class.getCanonicalName());
        builder.setValue("value", new FlowSink[] { FlowSink.ANY });
        ANYFLOWSINKS = builder.build();

        sourceValue = TreeUtils.getMethod("sparta.checkers.quals.FlowSources", "value", 0, processingEnv);
        sinkValue = TreeUtils.getMethod("sparta.checkers.quals.FlowSinks", "value", 0, processingEnv);

        super.initChecker();
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
                    return AnnotationUtils.areSame(elements, rhs, NOFLOWSOURCES) ||
                            AnnotationUtils.areSame(elements, rhs, POLYFLOWSOURCES) ||
                            AnnotationUtils.areSame(elements, rhs, POLYALL);
                } else if (isPolySourceQualifier(rhs)) {
                    // If RHS is poly, lhs has to be top or poly qualifier.
                    return AnnotationUtils.areSame(elements, lhs, ANYFLOWSOURCES) ||
                            AnnotationUtils.areSame(elements, lhs, POLYFLOWSOURCES) ||
                            AnnotationUtils.areSame(elements, lhs, POLYALL);
                } else {
                    if (!isSourceQualifier(lhs)) {
                        return false;
                    }
                    List<FlowSource> lhssrc = AnnotationUtils.elementValueEnumArrayWithDefaults(elements, lhs, "value", FlowSource.class);
                    List<FlowSource> rhssrc = AnnotationUtils.elementValueEnumArrayWithDefaults(elements, rhs, "value", FlowSource.class);
                    return  AnnotationUtils.areSame(elements, lhs, ANYFLOWSOURCES) ||
                            lhssrc.containsAll(rhssrc);
                }
            } else if (isSinkQualifier(rhs)) {
                if (isPolySinkQualifier(lhs)) {
                    // If LHS is poly, rhs has to be bottom or poly qualifier.
                    return AnnotationUtils.areSame(elements, rhs, ANYFLOWSINKS) ||
                            AnnotationUtils.areSame(elements, rhs, POLYFLOWSINKS);
                } else if (isPolySinkQualifier(rhs)) {
                    // If RHS is poly, lhs has to be top or poly qualifier.
                    return AnnotationUtils.areSame(elements, lhs, NOFLOWSINKS) ||
                            AnnotationUtils.areSame(elements, lhs, POLYFLOWSINKS);
                } else {
                    if (!isSinkQualifier(lhs)) {
                        return false;
                    }
                    List<FlowSink> lhssnk = AnnotationUtils.elementValueEnumArrayWithDefaults(elements, lhs, "value", FlowSink.class);
                    List<FlowSink> rhssnk = AnnotationUtils.elementValueEnumArrayWithDefaults(elements, rhs, "value", FlowSink.class);
                    return lhssnk.isEmpty() ||
                            rhssnk.containsAll(lhssnk) ||
                        (rhssnk.contains(FlowSink.ANY) && rhssnk.size()==1);
                }
            } else if (QualifierPolymorphism.isPolyAll(rhs)) {
                // If RHS is polyall, the LHS has to be a top qualifier or also poly.
                return AnnotationUtils.areSame(elements, lhs, NOFLOWSINKS) ||
                        AnnotationUtils.areSame(elements, lhs, ANYFLOWSOURCES) ||
                        AnnotationUtils.areSame(elements, lhs, POLYFLOWSINKS) ||
                        AnnotationUtils.areSame(elements, lhs, POLYFLOWSOURCES);
 
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
    }
}

package sparta.checkers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.basetype.BaseTypeChecker;
import checkers.quals.StubFiles;
import checkers.quals.TypeQualifiers;
import checkers.quals.PolyAll;
import checkers.source.SourceChecker;
import checkers.types.QualifierHierarchy;
import checkers.util.AnnotationUtils;
import checkers.util.MultiGraphQualifierHierarchy;
import checkers.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import checkers.util.QualifierPolymorphism;

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
    public void initChecker(ProcessingEnvironment env) {
        AnnotationUtils annoFactory = AnnotationUtils.getInstance(env);
        NOFLOWSOURCES = annoFactory.fromClass(FlowSources.class);
        NOFLOWSINKS = annoFactory.fromClass(FlowSinks.class);
        POLYFLOWSOURCES = annoFactory.fromClass(PolyFlowSources.class);
        POLYFLOWSINKS = annoFactory.fromClass(PolyFlowSinks.class);
        POLYALL = annoFactory.fromClass(PolyAll.class);

        AnnotationUtils.AnnotationBuilder builder =
                new AnnotationUtils.AnnotationBuilder(env, FlowSources.class.getCanonicalName());
        builder.setValue("value", new FlowSource[] { FlowSource.ANY });
        ANYFLOWSOURCES = builder.build();

        builder = new AnnotationUtils.AnnotationBuilder(env, FlowSinks.class.getCanonicalName());
        builder.setValue("value", new FlowSink[] { FlowSink.ANY });
        ANYFLOWSINKS = builder.build();

        super.initChecker(env);
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

        public FlowQualifierHierarchy(MultiGraphQualifierHierarchy hierarchy) {
            super(hierarchy);
        }

        protected FlowQualifierHierarchy(MultiGraphFactory f) {
            super(f);
            this.tops = AnnotationUtils.createAnnotationSet();
            this.tops.add(ANYFLOWSOURCES);
            this.tops.add(NOFLOWSINKS);
            this.bottoms = AnnotationUtils.createAnnotationSet();
            this.bottoms.add(NOFLOWSOURCES);
            this.bottoms.add(ANYFLOWSINKS);
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
                    List<FlowSource> lhssrc = AnnotationUtils.elementValueEnumArrayWithDefaults(lhs, "value", FlowSource.class);
                    List<FlowSource> rhssrc = AnnotationUtils.elementValueEnumArrayWithDefaults(rhs, "value", FlowSource.class);
                    return  AnnotationUtils.areSame(lhs, ANYFLOWSOURCES) ||
                            lhssrc.containsAll(rhssrc);
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
                    List<FlowSink> lhssnk = AnnotationUtils.elementValueEnumArrayWithDefaults(lhs, "value", FlowSink.class);
                    List<FlowSink> rhssnk = AnnotationUtils.elementValueEnumArrayWithDefaults(rhs, "value", FlowSink.class);
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
        protected void addPolyRelations(AnnotationUtils annoFactory,
                QualifierHierarchy qualHierarchy,
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

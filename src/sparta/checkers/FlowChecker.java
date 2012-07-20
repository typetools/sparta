package sparta.checkers;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.basetype.BaseTypeChecker;
import checkers.quals.StubFiles;
import checkers.quals.TypeQualifiers;
import checkers.source.SourceChecker;
import checkers.types.QualifierHierarchy;
import checkers.util.AnnotationUtils;
import checkers.util.MultiGraphQualifierHierarchy;

import sparta.checkers.quals.FlowSinks;
import sparta.checkers.quals.FlowSinks.FlowSink;
import sparta.checkers.quals.FlowSources;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.PolyFlowSinks;
import sparta.checkers.quals.PolyFlowSources;

@TypeQualifiers({FlowSources.class, FlowSinks.class,
    PolyFlowSources.class, PolyFlowSinks.class})
@StubFiles("flow.astub")
public class FlowChecker extends BaseTypeChecker {

    protected AnnotationMirror NOFLOWSOURCES, ANYFLOWSOURCES, POLYFLOWSOURCES;
    protected AnnotationMirror NOFLOWSINKS, ANYFLOWSINKS, POLYFLOWSINKS;

    @Override
    public void initChecker(ProcessingEnvironment env) {
        AnnotationUtils annoFactory = AnnotationUtils.getInstance(env);
        NOFLOWSOURCES = annoFactory.fromClass(FlowSources.class);
        NOFLOWSINKS = annoFactory.fromClass(FlowSinks.class);
        POLYFLOWSOURCES = annoFactory.fromClass(PolyFlowSources.class);
        POLYFLOWSINKS = annoFactory.fromClass(PolyFlowSinks.class);

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
    protected QualifierHierarchy createQualifierHierarchy() {
        return new FlowQualifierHierarchy((MultiGraphQualifierHierarchy)super.createQualifierHierarchy());
    }

    private final class FlowQualifierHierarchy extends MultiGraphQualifierHierarchy {

        public FlowQualifierHierarchy(MultiGraphQualifierHierarchy hierarchy) {
            super(hierarchy);
        }

        private boolean isSourceQualifier(AnnotationMirror anno) {
            return NOFLOWSOURCES.getAnnotationType().equals(anno.getAnnotationType()) ||
                    POLYFLOWSOURCES.getAnnotationType().equals(anno.getAnnotationType());
        }

        private boolean isPolySourceQualifier(AnnotationMirror anno) {
            return POLYFLOWSOURCES.getAnnotationType().equals(anno.getAnnotationType());
        }

        private boolean isSinkQualifier(AnnotationMirror anno) {
            return NOFLOWSINKS.getAnnotationType().equals(anno.getAnnotationType()) ||
                    POLYFLOWSINKS.getAnnotationType().equals(anno.getAnnotationType());
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
                            AnnotationUtils.areSame(rhs, POLYFLOWSOURCES);
                } else if (isPolySourceQualifier(rhs)) {
                    // If RHS is poly, lhs has to be top or poly qualifier.
                    return AnnotationUtils.areSame(lhs, ANYFLOWSOURCES) ||
                            AnnotationUtils.areSame(lhs, POLYFLOWSOURCES);
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
            } else {
                SourceChecker.errorAbort("FlowChecker: unexpected AnnotationMirrors: " + rhs + " and " + lhs);
                return false; // dead code
            }
       }
    }
}

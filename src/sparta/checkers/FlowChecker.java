package sparta.checkers;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.basetype.BaseTypeChecker;
import checkers.quals.StubFiles;
import checkers.quals.TypeQualifiers;
import checkers.types.QualifierHierarchy;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.util.AnnotationUtils;
import checkers.util.MultiGraphQualifierHierarchy;

import sparta.checkers.quals.FlowSinks;
import sparta.checkers.quals.FlowSinks.FlowSink;
import sparta.checkers.quals.FlowSources;
import sparta.checkers.quals.FlowSources.FlowSource;

@TypeQualifiers({FlowSources.class, FlowSinks.class})
@StubFiles("flow.astub")
public class FlowChecker extends BaseTypeChecker {

    protected AnnotationMirror NOFLOWSOURCES, ANYFLOWSOURCES;
    protected AnnotationMirror NOFLOWSINKS, ANYFLOWSINKS;

    @Override
    public void initChecker(ProcessingEnvironment env) {
        AnnotationUtils annoFactory = AnnotationUtils.getInstance(env);
        NOFLOWSOURCES = annoFactory.fromClass(FlowSources.class);
        NOFLOWSINKS = annoFactory.fromClass(FlowSinks.class);

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
    public boolean isValidUse(AnnotatedDeclaredType declarationType,
            AnnotatedDeclaredType useType) {
        // The default annotation on a class is FlowSources({}), which is not a supertype of
        // any interesting use.
        // Let's just always allow annotations.
        return true;
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

        @Override
        public AnnotationMirror getRootAnnotation(AnnotationMirror start) {
            if (NOFLOWSOURCES.getAnnotationType().equals(start.getAnnotationType())) {
                return ANYFLOWSOURCES;
            } else if (NOFLOWSINKS.getAnnotationType().equals(start.getAnnotationType())) {
                return NOFLOWSINKS;
            } else {
                throw new CheckerError("FlowChecker: unexpected AnnotationMirror: " + start);
            }
        }

        @Override
        public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
            if (lhs.getAnnotationType()!=rhs.getAnnotationType()) {
                return false;
            }
            if (NOFLOWSOURCES.getAnnotationType().equals(rhs.getAnnotationType())) {
                List<FlowSource> lhssrc = AnnotationUtils.elementValueEnumArrayWithDefaults(lhs, "value", FlowSource.class);
                List<FlowSource> rhssrc = AnnotationUtils.elementValueEnumArrayWithDefaults(rhs, "value", FlowSource.class);
                return  AnnotationUtils.areSame(lhs, ANYFLOWSOURCES) ||
                        lhssrc.containsAll(rhssrc);
            } else if (NOFLOWSINKS.getAnnotationType().equals(rhs.getAnnotationType())) {
                List<FlowSink> lhssnk = AnnotationUtils.elementValueEnumArrayWithDefaults(lhs, "value", FlowSink.class);
                List<FlowSink> rhssnk = AnnotationUtils.elementValueEnumArrayWithDefaults(rhs, "value", FlowSink.class);
                return lhssnk.isEmpty() ||
                        rhssnk.containsAll(lhssnk) ||
                        (rhssnk.contains(FlowSink.ANY) && rhssnk.size()==1);
            } else {
                throw new CheckerError("FlowChecker: unexpected AnnotationMirrors: " + rhs + " and " + lhs);
            }
       }
    }
}
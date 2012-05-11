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
import checkers.util.GraphQualifierHierarchy;

import sparta.checkers.quals.FlowSources;
import sparta.checkers.quals.FlowSources.FlowSource;

@TypeQualifiers(FlowSources.class)
@StubFiles("flow.astub")
public class FlowChecker extends BaseTypeChecker {

    protected AnnotationMirror NOFLOWSOURCES, ANYFLOWSOURCES;

    @Override
    public void initChecker(ProcessingEnvironment env) {
        AnnotationUtils annoFactory = AnnotationUtils.getInstance(env);
        NOFLOWSOURCES = annoFactory.fromClass(FlowSources.class);

        AnnotationUtils.AnnotationBuilder builder =
                new AnnotationUtils.AnnotationBuilder(env, FlowSources.class.getCanonicalName());
        builder.setValue("value", new FlowSource[] { FlowSource.ANY });
        ANYFLOWSOURCES = builder.build();

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
    protected QualifierHierarchy createQualifierHierarchy() {
        return new FlowQualifierHierarchy((GraphQualifierHierarchy)super.createQualifierHierarchy());
    }

    private final class FlowQualifierHierarchy extends GraphQualifierHierarchy {

        public FlowQualifierHierarchy(GraphQualifierHierarchy hierarchy) {
            super(hierarchy);
        }

        @Override
        public AnnotationMirror getRootAnnotation(AnnotationMirror start) {
            return ANYFLOWSOURCES;
        }

        @Override
        public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
            List<FlowSource> lhsflows = AnnotationUtils.elementValueEnumArrayWithDefaults(lhs, "value", FlowSource.class);
            List<FlowSource> rhsflows = AnnotationUtils.elementValueEnumArrayWithDefaults(rhs, "value", FlowSource.class);
            return AnnotationUtils.areSame(lhs, ANYFLOWSOURCES) || lhsflows.containsAll(rhsflows);
        }
    }
}
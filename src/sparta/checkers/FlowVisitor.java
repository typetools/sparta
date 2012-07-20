package sparta.checkers;

import com.sun.source.tree.CompilationUnitTree;

import checkers.basetype.BaseTypeVisitor;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;

public class FlowVisitor extends BaseTypeVisitor<FlowChecker> {

    public FlowVisitor(FlowChecker checker, CompilationUnitTree root) {
       super(checker, root);
    }

    @Override
    public boolean isValidUse(AnnotatedDeclaredType declarationType,
            AnnotatedDeclaredType useType) {
        // The default annotation on a class is FlowSources({}), which is not a supertype of
        // any interesting use.
        // Let's just always allow annotations.
        return true;
    }
}

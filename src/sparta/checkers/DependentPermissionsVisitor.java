package sparta.checkers;

import java.util.List;
import sparta.checkers.quals.DependentPermissions;
import checkers.basetype.BaseTypeVisitor;
import checkers.source.Result;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.util.AnnotatedTypes;

import com.sun.source.tree.*;

public class DependentPermissionsVisitor extends
        BaseTypeVisitor<DependentPermissionsChecker, DependentPermissionsAnnotatedTypeFactory> {
    public DependentPermissionsVisitor(DependentPermissionsChecker checker, CompilationUnitTree root) {
        super(checker, root);
    }

    // filter out other warnings that has nothing to do with
    // DependentPermissions class
    @Override
    protected void commonAssignmentCheck(AnnotatedTypeMirror varType,
            AnnotatedTypeMirror valueType, Tree valueTree, String errorKey,
            boolean isLocalVariableAssignement) {
        if (valueType.getAnnotation(DependentPermissions.class) == null
                && varType.getAnnotation(DependentPermissions.class) == null) {
            return;
        } else {
            super.commonAssignmentCheck(varType, valueType, valueTree, errorKey,
                    isLocalVariableAssignement);
        }
    }

    @Override
    protected boolean checkConstructorInvocation(AnnotatedDeclaredType dt,
            AnnotatedExecutableType constructor, Tree src) {
        // Ignore the default annotation on the constructor
        return true;
    }

    // TODO: should we require a match between switch expression and cases?

    @Override
    public boolean isValidUse(AnnotatedDeclaredType declarationType, AnnotatedDeclaredType useType) {
        // The checker calls this method to compare the annotation used in a
        // type to the modifier it adds to the class declaration. As our default
        // modifier is Unqualified, this results in an error when a non-subtype
        // is used. Can we use FenumTop as default instead?
        return true;
    }

}

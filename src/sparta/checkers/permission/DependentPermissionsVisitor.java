package sparta.checkers.permission;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;

import sparta.checkers.permission.qual.DependentPermissions;

import com.sun.source.tree.Tree;

public class DependentPermissionsVisitor extends
        BaseTypeVisitor<DependentPermissionsAnnotatedTypeFactory> {
    public DependentPermissionsVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    // filter out other warnings that has nothing to do with
    // DependentPermissions class
    @Override
    protected void commonAssignmentCheck(AnnotatedTypeMirror varType,
            AnnotatedTypeMirror valueType, Tree valueTree, String errorKey) {
        if (valueType.getAnnotation(DependentPermissions.class) == null
                && varType.getAnnotation(DependentPermissions.class) == null) {
            return;
        } else {
            super.commonAssignmentCheck(varType, valueType, valueTree, errorKey);
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
    public boolean isValidUse(AnnotatedDeclaredType declarationType, AnnotatedDeclaredType useType, Tree tree) {
        //TODO: this was copied from the Fenum Checker.  Does this vistor need this hack??
        // The checker calls this method to compare the annotation used in a
        // type to the modifier it adds to the class declaration. As our default
        // modifier is Unqualified, this results in an error when a non-subtype
        // is used. Can we use FenumTop as default instead?
        return true;
    }

}

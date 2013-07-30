package sparta.checkers;

import checkers.basetype.BaseTypeVisitor;
import checkers.source.Result;
import checkers.util.AnnotationUtils;
import checkers.util.TreeUtils;

import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

import sparta.checkers.quals.MayRequiredPermissions;
import sparta.checkers.quals.RequiredPermissions;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;

/**
 * Propagate required permissions up the call stack. Require that they are
 * declared in the manifest.
 * 
 * TODO: should we propagate required permissions from (anonymous) inner classes
 * to the outside?
 */
public class RequiredPermissionsVisitor extends BaseTypeVisitor<RequiredPermissionsChecker> {

    public RequiredPermissionsVisitor(RequiredPermissionsChecker checker, CompilationUnitTree root) {
        super(checker, root);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        ExecutableElement methodElt = visitMethodRequiredPermissions(node);
        visitMethodMayRequirePermissions(node, methodElt);
        return super.visitMethodInvocation(node, p);
    }

    private void visitMethodMayRequirePermissions(MethodInvocationTree node,
            ExecutableElement methodElt) {
        // Look for @MayRequiredPermissions on the enclosing method
        AnnotationMirror mayReqP = atypeFactory.getDeclAnnotation(methodElt,
                MayRequiredPermissions.class);
        if (mayReqP != null) {
            List<String> mayReqPerms = AnnotationUtils.getElementValueArray(mayReqP, "value",
                    String.class, false);

            if (!mayReqPerms.isEmpty()) {
                ExecutableElement callerElt = TreeUtils.elementFromDeclaration(TreeUtils
                        .enclosingMethod(getCurrentPath()));
                AnnotationMirror callerReq = atypeFactory.getDeclAnnotation(callerElt,
                        MayRequiredPermissions.class);
                List<String> callerPerms;
                List<String> missing = new LinkedList<String>();
                if (callerReq == null) {
                    missing.addAll(mayReqPerms);
                    callerPerms = new LinkedList<String>();
                } else {
                    callerPerms = AnnotationUtils.getElementValueArray(callerReq, "value",
                            String.class, false);
                    for (String perm : mayReqPerms) {
                        if (!callerPerms.contains(perm)) {
                            missing.add(perm);
                        }
                    }
                }
                if (!missing.isEmpty()) {
                    checker.report(
                            Result.failure("may.required.permissions", missing, callerPerms,
                                    AnnotationUtils.getElementValue(mayReqP, "notes", String.class,
                                            false)), node);
                }
            }

        }
    }

    private ExecutableElement visitMethodRequiredPermissions(MethodInvocationTree node) {
        // Look for @RequiredPermissions on the enclosing method
        ExecutableElement methodElt = TreeUtils.elementFromUse(node);
        AnnotationMirror reqP = atypeFactory
                .getDeclAnnotation(methodElt, RequiredPermissions.class);
        if (reqP != null) {
            List<String> reqPerms = AnnotationUtils.getElementValueArray(reqP, "value",
                    String.class, false);
            if (!reqPerms.isEmpty()) {
                ExecutableElement callerElt = TreeUtils.elementFromDeclaration(TreeUtils
                        .enclosingMethod(getCurrentPath()));
                AnnotationMirror callerReq = atypeFactory.getDeclAnnotation(callerElt,
                        RequiredPermissions.class);
                List<String> callerPerms;
                List<String> missing = new LinkedList<String>();
                if (callerReq == null) {
                    missing.addAll(reqPerms);
                    callerPerms = new LinkedList<String>();
                } else {
                    callerPerms = AnnotationUtils.getElementValueArray(callerReq, "value",
                            String.class, false);
                    for (String perm : reqPerms) {
                        if (!callerPerms.contains(perm)) {
                            missing.add(perm);
                        }
                    }
                }
                if (!missing.isEmpty()) {
                    checker.report(Result.failure("unsatisfied.permissions", missing, callerPerms),
                            node);
                }
            }
        }
        return methodElt;
    }

    @Override
    public Void visitMethod(MethodTree node, Void p) {
        // Ensure that all constants in @RequiredPermissions are in Manifest
        return super.visitMethod(node, p);
    }
}
package sparta.checkers;

import checkers.source.Result;
import checkers.source.SourceChecker;
import checkers.source.SourceVisitor;
import checkers.util.ElementUtils;
import checkers.util.TreeUtils;

import javax.lang.model.element.Element;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;

/**
 * A utility class for displaying all method calls and field accesses of
 * methods/fields for which the source code is not available.
 * 
 * <p>
 * The class is an annotation processor; in order to use it, invoke the compiler
 * on the source file(s) for which you wish to view the binary-only fields and
 * method. You may also wish to use the {@code -proc:only} javac option to stop
 * compilation after annotation processing.
 */
public class ReportBinaryChecker extends SourceChecker {

    private static final String[] ignorePackages = { "java", "javax", "android", "com.android" };

    private static boolean shouldReport(Element elem) {
        for (String ignoredPkg : ignorePackages) {
            if (ElementUtils.getVerboseName(elem).startsWith(ignoredPkg)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected SourceVisitor<?, ?> createSourceVisitor(CompilationUnitTree root) {
        return new ReportBinaryVisitor(this, root);
    }

    public static class ReportBinaryVisitor extends SourceVisitor<Void, Void> {
        public ReportBinaryVisitor(ReportBinaryChecker checker, CompilationUnitTree root) {
            super(checker, root);
        }

        @Override
        public Void visitMemberSelect(MemberSelectTree node, Void p) {
            Element elem = TreeUtils.elementFromUse(node);
            if (elem != null && trees.getTree(elem) == null && shouldReport(elem)) {
                checker.report(Result.warning("binary-only: " + ElementUtils.getVerboseName(elem)),
                        node);
            }
            return super.visitMemberSelect(node, p);
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
            Element elem = TreeUtils.elementFromUse(node);
            if (elem != null && trees.getTree(elem) == null && shouldReport(elem)) {
                checker.report(Result.warning("binary-only: " + ElementUtils.getVerboseName(elem)),
                        node);
            }
            return super.visitMethodInvocation(node, p);
        }
    }
}

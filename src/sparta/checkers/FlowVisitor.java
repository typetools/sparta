package sparta.checkers;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.SwitchTree;

import checkers.basetype.BaseTypeVisitor;
import checkers.source.Result;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;

public class FlowVisitor extends BaseTypeVisitor<FlowChecker> {

    public FlowVisitor(FlowChecker checker, CompilationUnitTree root) {
       super(checker, root);
    }

    private void ensureNoFlow(ExpressionTree tree, /*@checkers.compilermsgs.quals.CompilerMessageKey*/ String errMsg) {
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(tree);
        if (!type.hasAnnotation(checker.NOFLOWSINKS) ||
                !type.hasAnnotation(checker.NOFLOWSOURCES)) {
            checker.report(Result.failure(errMsg, type.getAnnotations()), tree);
        }
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
    public Void visitConditionalExpression(ConditionalExpressionTree node, Void p) {
        ensureNoFlow(node.getCondition(), "condition.flow");
        return super.visitConditionalExpression(node, p);
    }

    @Override
    public Void visitIf(IfTree node, Void p) {
        ensureNoFlow(node.getCondition(), "condition.flow");
        return super.visitIf(node, p);
    }

    @Override
    public Void visitSwitch(SwitchTree node, Void p) {
        ensureNoFlow(node.getExpression(), "condition.flow");
        return super.visitSwitch(node, p);
    }
    
    @Override
    public Void visitCase(CaseTree node, Void p) {
        ExpressionTree exprTree = node.getExpression();
        if (exprTree != null)
            ensureNoFlow(exprTree, "condition.flow");
        return super.visitCase(node, p);
    }
    
    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, Void p) {
        ensureNoFlow(node.getCondition(), "condition.flow");
        return super.visitDoWhileLoop(node, p);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, Void p) {
        ensureNoFlow(node.getCondition(), "condition.flow");
        return super.visitWhileLoop(node, p);
    }

    // Nothing needed for EnhancedForLoop, no boolean get's unboxed there.
    @Override
    public Void visitForLoop(ForLoopTree node, Void p) {
        if (node.getCondition()!=null) {
            // Condition is null e.g. in "for (;;) {...}"
            ensureNoFlow(node.getCondition(), "condition.flow");
        }
        return super.visitForLoop(node, p);
    }

}

package sparta.checkers;

import java.util.Set;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

import sparta.checkers.quals.ParameterizedFlowPermission;
import sparta.checkers.validator.BaseValidator;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.WhileLoopTree;

/**
 * A Validator that prints a warning any time a sensitive source is used in a
 * conditional
 * 
 * @author smillst
 *
 */
public class ConditionalValidator extends BaseValidator {
    private AnnotatedTypeFactory atypeFactory;
    private BaseTypeChecker checker;

    public ConditionalValidator(AnnotatedTypeFactory atypeFactory,
            BaseTypeChecker checker) {
        super();
        this.atypeFactory = atypeFactory;
        this.checker = checker;
    }

    private void checkConditionalPredicate(ExpressionTree tree) {
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(tree);
        final Set<ParameterizedFlowPermission> sources = Flow.getSources(type);
        if (sources.size() > 1) {
            checker.report(Result.failure("condition.flow", sources), tree);
        }
    }

    @Override
    public void visitConditionalExpression(ConditionalExpressionTree node) {
        checkConditionalPredicate(node.getCondition());
    }

    @Override
    public void visitIf(IfTree node) {
        checkConditionalPredicate(node.getCondition());

    }

    @Override
    public void visitSwitch(SwitchTree node) {
        checkConditionalPredicate(node.getExpression());

    }

    @Override
    public void visitCase(CaseTree node) {
        ExpressionTree exprTree = node.getExpression();
        if (exprTree != null)
            checkConditionalPredicate(exprTree);

    }

    @Override
    public void visitDoWhileLoop(DoWhileLoopTree node) {
        checkConditionalPredicate(node.getCondition());

    }

    @Override
    public void visitWhileLoop(WhileLoopTree node) {
        checkConditionalPredicate(node.getCondition());

    }

    // Nothing needed for EnhancedForLoop, no boolean get's unboxed there.
    @Override
    public void visitForLoop(ForLoopTree node) {
        if (node.getCondition() != null) {
            // Condition is null e.g. in "for (;;) {...}"
            checkConditionalPredicate(node.getCondition());
        }

    }
}

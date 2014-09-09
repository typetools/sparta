package sparta.checkers;

/*>>>
import org.checkerframework.checker.compilermsgs.qual.*;
*/

import java.util.Set;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeValidator;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedPrimitiveType;

import sparta.checkers.quals.ParameterizedFlowPermission;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.WhileLoopTree;

public class FlowVisitor extends BaseTypeVisitor<FlowAnnotatedTypeFactory> {

    /**
     * Should the checker warn when a non-literal source is used in
     * a conditional (regardless of the flow policy)?  
     */
    public static final String CHECK_CONDITIONALS_OPTION = "checkconditionals";
    
    private boolean checkConditional = Boolean.valueOf(checker.getOption(CHECK_CONDITIONALS_OPTION, "false"));

    public FlowVisitor(BaseTypeChecker checker) {
        super(checker);
    }
    @Override
    public Void visitAnnotatedType(AnnotatedTypeTree node, Void p) {
        
        return null;
    }
@Override
protected FlowAnnotatedTypeFactory createTypeFactory() {
    return new FlowAnnotatedTypeFactory(checker);
}
    @Override
    public boolean isValidUse(AnnotatedDeclaredType declarationType,
            AnnotatedTypeMirror.AnnotatedDeclaredType useType, Tree tree) {

        return areFlowsValid(declarationType, tree) && areFlowsValid(useType, tree);
       
    }

    @Override
    public boolean isValidUse(AnnotatedPrimitiveType type, Tree tree) {
        return areFlowsValid(type, tree);
    }

    @Override
    public boolean isValidUse(AnnotatedArrayType type, Tree tree) {
        return areFlowsValid(type, tree);
    }

    private void checkConditionalPredicate(ExpressionTree tree) {       
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(tree);
        final Set<ParameterizedFlowPermission> sources = Flow.getSources(type);
        if(checkConditional){
            if(sources.size() > 1){
                checker.report(Result.failure("condition.flow", sources), tree);
            }
        }
    }

    @Override
    public Void visitConditionalExpression(ConditionalExpressionTree node, Void p) {
        checkConditionalPredicate(node.getCondition());
        return super.visitConditionalExpression(node, p);
    }

    @Override
    public Void visitIf(IfTree node, Void p) {
        checkConditionalPredicate(node.getCondition());
        return super.visitIf(node, p);
    }

    @Override
    public Void visitSwitch(SwitchTree node, Void p) {
        checkConditionalPredicate(node.getExpression());
        return super.visitSwitch(node, p);
    }

    @Override
    public Void visitCase(CaseTree node, Void p) {
        ExpressionTree exprTree = node.getExpression();
        if (exprTree != null)
            checkConditionalPredicate(exprTree);
        return super.visitCase(node, p);
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, Void p) {
        checkConditionalPredicate(node.getCondition());
        return super.visitDoWhileLoop(node, p);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, Void p) {
        checkConditionalPredicate(node.getCondition());
        return super.visitWhileLoop(node, p);
    }

    // Nothing needed for EnhancedForLoop, no boolean get's unboxed there.
    @Override
    public Void visitForLoop(ForLoopTree node, Void p) {
        if (node.getCondition() != null) {
            // Condition is null e.g. in "for (;;) {...}"
            checkConditionalPredicate(node.getCondition());
        }

        return super.visitForLoop(node, p);
    }

    /**
     * For some reason, the FlowPermission[] passed to @Source or @Sink is
     * annotated and causes a type error. TODO: we should figure out why this is
     * happening in the first place and try to fix it.
     */
    @Override
    public Void visitAnnotation(AnnotationTree node, Void p) {
        return null;
    }


    @Override
    protected void commonAssignmentCheck(AnnotatedTypeMirror varType,
            AnnotatedTypeMirror valueType, Tree valueTree, /*@CompilerMessageKey*/String errorKey,
            boolean isLocalVariableAssignement) {

        super.commonAssignmentCheck(varType, valueType, valueTree, errorKey,
                isLocalVariableAssignement);
        atypeFactory.getFlowAnalizer().addAssignmentFlow(varType, valueType,
                atypeFactory.getTypeHierarchy(), getCurrentPath());
    }


    private boolean areFlowsValid(final AnnotatedTypeMirror atm, Tree tree) {
        atypeFactory.getFlowAnalizer().addTypeFlow(atm,
                atypeFactory.getTypeHierarchy(), getCurrentPath());
      
        final FlowPolicy flowPolicy = atypeFactory.getFlowPolicy();
        if (flowPolicy != null) {
           return flowPolicy.areFlowsAllowed(atm);
        }
        return true;
    }

    @Override
    protected BaseTypeValidator createTypeValidator() {
        return new BaseTypeValidator(checker, this, atypeFactory){
            @Override
            protected void reportError(final AnnotatedTypeMirror type, final Tree p) {
                StringBuffer buf = new StringBuffer();
                for (Flow flow : ((FlowAnnotatedTypeFactory)atypeFactory).getFlowPolicy().forbiddenFlows(type)) {
                    buf.append(flow.toString() + "\n");
                }
                checker.report(Result.failure("forbidden.flow", type.toString(), buf.toString()), p);
            }
        };
    }

}

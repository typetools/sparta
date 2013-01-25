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
import checkers.types.AnnotatedTypeMirror.AnnotatedArrayType;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypeMirror.AnnotatedPrimitiveType;

public class FlowVisitor extends BaseTypeVisitor<FlowChecker> {

    public FlowVisitor(FlowChecker checker, CompilationUnitTree root) {
       super(checker, root);
    }


   @Override
   public /**@Nullable*/ String isValidUse(AnnotatedDeclaredType declarationType,
                                           AnnotatedDeclaredType useType) {
       String errType = areFlowsValid(useType);
       if(errType == null)
           errType = areFlowsValid(declarationType);
       return errType;
   }

   //TODO: DO I HAVE TO DO MORE HERE
   @Override
   public /**@Nullable*/ String isValidUse(AnnotatedPrimitiveType type) {
       return areFlowsValid(type);
   }

   //TODO: DO I HAVE TO DO MORE HERE
   @Override
   public /**@Nullable*/ String isValidUse(AnnotatedArrayType type) {
       return areFlowsValid(type);
   }

    private void ensureNoFlow(ExpressionTree tree, /*@checkers.compilermsgs.quals.CompilerMessageKey*/ String errMsg) {
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(tree);
        if (!type.hasAnnotation(checker.NOFLOWSINKS) ||
                !type.hasAnnotation(checker.NOFLOWSOURCES)) {
            checker.report(Result.failure(errMsg, type.getAnnotations()), tree);
        }
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

    //TODO: Questions for Mike
    //DO WE AUTOMATICALLY ALLOW ANY -> {}
    //WHAT ABOUT {} -> ANY

    private String areFlowsValid(final AnnotatedTypeMirror atm) {
        final FlowPolicy flowPolicy = checker.getFlowPolicy();

        if( flowPolicy != null ) {

            if( !checker.getFlowPolicy().areFlowsAllowed(atm) ) {
                return "forbidden.flow";
            }
        }

        return isValidToError(true);
    }
}

package sparta.checkers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import sparta.checkers.quals.Sinks.SPARTA_Permission;
import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.SPARTA_Permission;

import com.sun.source.tree.*;

import checkers.basetype.BaseTypeVisitor;
import checkers.source.Result;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedArrayType;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypeMirror.AnnotatedPrimitiveType;
import checkers.util.AnnotationUtils;

public class FlowVisitor extends BaseTypeVisitor<FlowChecker> {

    public FlowVisitor(FlowChecker checker, CompilationUnitTree root) {
       super(checker, root);
    }


    @Override
    public boolean isValidUse(AnnotatedDeclaredType declarationType,
                                           AnnotatedDeclaredType useType) {
       return areFlowsValid(useType) ;
               //&& areFlowsValid(declarationType);
    }

    @Override
    public boolean isValidUse(AnnotatedPrimitiveType type) {
       return areFlowsValid(type);
    }

    @Override
    public boolean isValidUse(AnnotatedArrayType type) {
       return areFlowsValid(type);
    }

    private void ensureContionalSink(ExpressionTree tree) {
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(tree);

        final AnnotationMirror sinkAnno = type.getAnnotation(Sinks.class);
        final Set<SPARTA_Permission> sinks = FlowUtil.getSinks(sinkAnno, false);
        if (!sinks.contains(SPARTA_Permission.ANY) && !sinks.contains(SPARTA_Permission.CONDITIONAL)) {
            checker.report(
                    Result.failure("condition.flow", type.getAnnotations()),
                    tree);
        }
    }

    @Override
    public Void visitConditionalExpression(ConditionalExpressionTree node, Void p) {
        ensureContionalSink(node.getCondition());
        return super.visitConditionalExpression(node, p);
    }

    @Override
    public Void visitIf(IfTree node, Void p) {
        ensureContionalSink(node.getCondition());
        return super.visitIf(node, p);
    }

    @Override
    public Void visitSwitch(SwitchTree node, Void p) {
        ensureContionalSink(node.getExpression());
        return super.visitSwitch(node, p);
    }

    @Override
    public Void visitCase(CaseTree node, Void p) {
        ExpressionTree exprTree = node.getExpression();
        if (exprTree != null)
            ensureContionalSink(exprTree);
        return super.visitCase(node, p);
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, Void p) {
        ensureContionalSink(node.getCondition());
        return super.visitDoWhileLoop(node, p);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, Void p) {
        ensureContionalSink(node.getCondition());
        return super.visitWhileLoop(node, p);
    }

    // Nothing needed for EnhancedForLoop, no boolean get's unboxed there.
    @Override
    public Void visitForLoop(ForLoopTree node, Void p) {
        if (node.getCondition()!=null) {
            // Condition is null e.g. in "for (;;) {...}"
            ensureContionalSink(node.getCondition());
        }
        return super.visitForLoop(node, p);
    }

    private boolean areFlowsValid(final AnnotatedTypeMirror atm) {
        final FlowPolicy flowPolicy = checker.getFlowPolicy();

        if( flowPolicy != null ) {
            return checker.getFlowPolicy().areFlowsAllowed(atm);
        }

        return true;
    }


    @Override
    protected BaseTypeVisitor<FlowChecker>.TypeValidator createTypeValidator() {
        return new FlowTypeValidator();
    }

    protected class FlowTypeValidator extends BaseTypeVisitor<FlowChecker>.TypeValidator {
        @Override
        protected void reportError(final AnnotatedTypeMirror type, final Tree p) {
            StringBuffer buf = new StringBuffer();
            for(Flow flow: checker.getFlowPolicy().forbiddenFlows(type)){
        	buf.append(flow.toString()+"\n");
            }
            checker.report(Result.failure("forbidden.flow",
                    type.toString(), buf.toString()), p);

            isValid = false;
        }
 

        @Override
        protected void reportValidityResult(final String errorType, final AnnotatedTypeMirror type, final Tree p) {

            checker.report(Result.failure(errorType,
                    type.toString()), p);

            isValid = false;
        }

    }
}

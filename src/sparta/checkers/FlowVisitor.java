package sparta.checkers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import sparta.checkers.quals.FlowSinks.FlowSink;
import sparta.checkers.quals.*;

import checkers.types.visitors.AnnotatedTypeScanner;
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

    //TODO: WHY IS THIS HERE BUT NOT USED?
    private void ensureNoFlow(ExpressionTree tree, /*@checkers.compilermsgs.quals.CompilerMessageKey*/ String errMsg) {
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(tree);
        if (!type.hasAnnotation(checker.NOFLOWSINKS) ||
                !type.hasAnnotation(checker.NOFLOWSOURCES)) {
            checker.report(Result.failure(errMsg, type.getAnnotations()), tree);
        }
    }

	private void ensureContionalSink(ExpressionTree tree) {
		AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(tree);

		final AnnotationMirror sinkAnno = type.getAnnotation(FlowSinks.class);
		final Set<FlowSink> sinks = new HashSet<FlowSink>(
				AnnotationUtils.getElementValueEnumArray(sinkAnno, "value",
						FlowSinks.FlowSink.class, true));
		if (!sinks.contains(FlowSink.CONDITIONAL)) {
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
        protected void reportError(final AnnotatedTypeMirror type, final Tree p) {
            reportValidityResult("forbidden.flow", type, p);
        }
 
        protected void reportValidityResult(final String errorType, final AnnotatedTypeMirror type, final Tree p) {
        	
            checker.report(Result.failure(errorType,
                    type.toString()), p);

            isValid = false;
        }

    }
}

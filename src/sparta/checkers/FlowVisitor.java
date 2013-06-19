package sparta.checkers;


import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import sparta.checkers.quals.*;

import com.sun.source.tree.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;

import checkers.basetype.BaseTypeVisitor;
import checkers.quals.DefaultQualifier;
import checkers.source.Result;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedArrayType;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.types.AnnotatedTypeMirror.AnnotatedNoType;
import checkers.types.AnnotatedTypeMirror.AnnotatedPrimitiveType;
import checkers.types.AnnotatedTypeMirror.AnnotatedTypeVariable;
import checkers.types.AnnotatedTypeMirror.AnnotatedWildcardType;


public class FlowVisitor extends BaseTypeVisitor<FlowChecker> {

    private boolean topAllowed = false;

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

        final AnnotationMirror sinkAnno = type.getAnnotation(Sink.class);
        final Set<FlowPermission> sinks = FlowUtil.getSink(sinkAnno, false);
        if (!sinks.contains(FlowPermission.ANY) && !sinks.contains(FlowPermission.CONDITIONAL)) {
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
    /**
     * For some reason, the FlowPermission[] passed to @Source or @Sink
     * is annotated and causes a type error.
     * TODO: we should figure out why this is happening in the first place
     * and try to fix it.
     */
    @Override
    public Void visitAnnotation(AnnotationTree node, Void p) {
        List<? extends ExpressionTree> args = node.getArguments();
        if (args.isEmpty()) {
            // Nothing to do if there are no annotation arguments.
            return null;
        }

        Element anno = TreeInfo.symbol((JCTree) node.getAnnotationType());
        if (anno.toString().equals(Sink.class.getName()) ||
                anno.toString().equals(Source.class.getName()) || 
                anno.toString().equals(RequiredPermissions.class.getName())) {
            // Skip these two annotations, as we don't care about the
            // arguments to them.
            return null;
        }
        return super.visitAnnotation(node, p);
    }
    
    /**
     * Check the return type of an invoked method for forbidden 
     * flows in case the method was annotated in a stub file. 
     * (Parameters are checked during the pseudo assignment of the
     * arguments to the parameters.)
     * TODO: It would be better to to check this in 
     * checkers.types.AnnotatedTypeFactory.methodFromUse(MethodInvocationTree)
     */
    @Override
    protected void checkMethodInvocability(AnnotatedExecutableType method,
            MethodInvocationTree node) {
        AnnotatedTypeMirror returnType = method.getReturnType();
        if (!(returnType instanceof AnnotatedNoType)) {
            warnForbiddenFlows(returnType, node);
        }
        super.checkMethodInvocability(method, node);

    }

    private boolean warnForbiddenFlows(final AnnotatedTypeMirror type,
            final Tree tree) {

        if (!areFlowsValid(type)) {
            StringBuffer buf = new StringBuffer();
            for (Flow flow : checker.getFlowPolicy().forbiddenFlows(type)) {
                buf.append(flow.toString() + "\n");
            }
            checker.report(
                    Result.failure("forbidden.flow", type.toString(),
                            buf.toString()), tree);
            return false;
        }
        return true;
    }

    private boolean areFlowsValid(final AnnotatedTypeMirror atm) {

    	boolean isLocal = atm.getElement() != null && atm.getElement().getKind() == ElementKind.LOCAL_VARIABLE;
        if((isLocal || this.topAllowed)  && FlowUtil.isTop(atm)){
        	//Local variables are allowed to be top type so a more specific type can
        	//be inferred.
        	return true;
        }

        final FlowPolicy flowPolicy = checker.getFlowPolicy();

        if( flowPolicy != null ) {
            return checker.getFlowPolicy().areFlowsAllowed(atm);
        }

        return true;
    }


    @Override
    protected BaseTypeVisitor<FlowChecker>.TypeValidator createTypeValidator() {
        return new FlowTypeValidator(this);
    }

    protected class FlowTypeValidator extends BaseTypeVisitor<FlowChecker>.TypeValidator {

		private FlowVisitor flowVisitor;

		public FlowTypeValidator(FlowVisitor flowVisitor) {
			this.flowVisitor = flowVisitor;
		}

		@Override
		public Void visitTypeVariable(AnnotatedTypeVariable type, Tree tree) {
			// Keep in sync with visitWildcard
			AnnotatedTypeMirror upperbound = type.getUpperBound();
			if (upperbound != null) {
				//Allow top on upper bounds
				flowVisitor.setAllowTop(true);
			}
			Void tmpReturn = super.visitTypeVariable(type, tree);
			//Disallow top, done processing type variable
			flowVisitor.setAllowTop(false);
			return tmpReturn;
		}

		@Override
		public Void visitWildcard(AnnotatedWildcardType type, Tree tree) {
			// Keep in sync with visitTypeVariable
			AnnotatedTypeMirror upperbound = type.getExtendsBound();
			if (upperbound != null) {
				//Allow top on upper bounds
				flowVisitor.setAllowTop(true);
			}
			Void tmpReturn = super.visitWildcard(type, tree);
			//Disallow top, done processing wildcard
			flowVisitor.setAllowTop(false);
			return tmpReturn;
		}
        	   
    	
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
/**
 * Do not warn if any type is ANY->{}
 * 
 * This turned on when visiting type parameters or wildcards with upper bounds.
 * @param topAllowed
 */
	public void setAllowTop(boolean topAllowed) {
		this.topAllowed  = topAllowed;
		
	}
}

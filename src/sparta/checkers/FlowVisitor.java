package sparta.checkers;

/*>>>
import checkers.compilermsgs.quals.*;
*/

import checkers.basetype.BaseTypeChecker;
import checkers.basetype.BaseTypeValidator;
import checkers.basetype.BaseTypeVisitor;
import checkers.source.Result;
import checkers.types.AnnotatedTypeFactory;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedArrayType;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.types.AnnotatedTypeMirror.AnnotatedNoType;
import checkers.types.AnnotatedTypeMirror.AnnotatedPrimitiveType;
import checkers.types.AnnotatedTypeMirror.AnnotatedTypeVariable;
import checkers.types.AnnotatedTypeMirror.AnnotatedWildcardType;


import javacutils.Pair;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import sparta.checkers.quals.DependentPermissions;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.MayRequiredPermissions;
import sparta.checkers.quals.RequiredPermissions;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;

import sparta.checkers.quals.DependentPermissions;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.MayRequiredPermissions;
import sparta.checkers.quals.RequiredPermissions;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;

public class FlowVisitor extends BaseTypeVisitor<FlowChecker, FlowAnnotatedTypeFactory> {

    private boolean topAllowed = false;

    public FlowVisitor(FlowChecker checker, CompilationUnitTree root) {
        super(checker, root);
    }

    @Override
    public boolean isValidUse(AnnotatedDeclaredType declarationType,
            AnnotatedDeclaredType useType, Tree tree) {
        
        return areFlowsValid(useType, tree);
        // && areFlowsValid(declarationType);
    }

    @Override
    public boolean isValidUse(AnnotatedPrimitiveType type, Tree tree) {
        return areFlowsValid(type, tree);
    }

    @Override
    public boolean isValidUse(AnnotatedArrayType type, Tree tree) {
        return areFlowsValid(type, tree);
    }

    private void ensureConditionalSink(ExpressionTree tree) {
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(tree);

        final AnnotationMirror sinkAnno = type.getAnnotation(Sink.class);
        final Set<FlowPermission> sinks = FlowUtil.getSink(sinkAnno, false);
        if (!sinks.contains(FlowPermission.ANY) && !sinks.contains(FlowPermission.CONDITIONAL)) {
            checker.report(Result.failure("condition.flow", type.getAnnotations()), tree);
        }
    }

    @Override
    public Void visitConditionalExpression(ConditionalExpressionTree node, Void p) {
        ensureConditionalSink(node.getCondition());
        return super.visitConditionalExpression(node, p);
    }

    @Override
    public Void visitIf(IfTree node, Void p) {
        ensureConditionalSink(node.getCondition());
        return super.visitIf(node, p);
    }

    @Override
    public Void visitSwitch(SwitchTree node, Void p) {
        ensureConditionalSink(node.getExpression());
        return super.visitSwitch(node, p);
    }

    @Override
    public Void visitCase(CaseTree node, Void p) {
        ExpressionTree exprTree = node.getExpression();
        if (exprTree != null)
            ensureConditionalSink(exprTree);
        return super.visitCase(node, p);
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, Void p) {
        ensureConditionalSink(node.getCondition());
        return super.visitDoWhileLoop(node, p);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, Void p) {
        ensureConditionalSink(node.getCondition());
        return super.visitWhileLoop(node, p);
    }

    // Nothing needed for EnhancedForLoop, no boolean get's unboxed there.
    @Override
    public Void visitForLoop(ForLoopTree node, Void p) {
        if (node.getCondition() != null) {
            // Condition is null e.g. in "for (;;) {...}"
            ensureConditionalSink(node.getCondition());
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
        List<? extends ExpressionTree> args = node.getArguments();
        if (args.isEmpty()) {
            // Nothing to do if there are no annotation arguments.
            return null;
        }

        Element anno = TreeInfo.symbol((JCTree) node.getAnnotationType());
        if (anno.toString().equals(Sink.class.getName())
                || anno.toString().equals(Source.class.getName())
                || anno.toString().equals(RequiredPermissions.class.getName())
                || anno.toString().equals(MayRequiredPermissions.class.getName())
                || anno.toString().equals(DependentPermissions.class.getName())) {
            // Skip these two annotations, as we don't care about the
            // arguments to them.
            return null;
        }
        return super.visitAnnotation(node, p);
    }

    /**
     * Check the return type of an invoked method for forbidden flows in case
     * the method was annotated in a stub file. (Parameters are checked during
     * the pseudo assignment of the arguments to the parameters.) TODO: It would
     * be better to to check this in
     * checkers.types.AnnotatedTypeFactory.methodFromUse(MethodInvocationTree)
     */
    @Override
    protected void checkMethodInvocability(AnnotatedExecutableType method, MethodInvocationTree node) {
        AnnotatedTypeMirror returnType = method.getReturnType();
        if (!(returnType instanceof AnnotatedNoType)) {
            warnForbiddenFlows(returnType, node);
        }
        super.checkMethodInvocability(method, node);

        AnnotatedTypeMirror methodReceiver = method.getReceiverType().getErased();
        AnnotatedTypeMirror treeReceiver = methodReceiver.getCopy(false);
        AnnotatedTypeMirror rcv = atypeFactory.getReceiverType(node);
        treeReceiver.addAnnotations(rcv.getEffectiveAnnotations());

        if (!checker.getTypeHierarchy().isSubtype(treeReceiver, methodReceiver)) {
            Set<FlowPermission> sinks = new HashSet<FlowPermission>(FlowUtil.getSink(methodReceiver));
            Set<FlowPermission> sources = new HashSet<FlowPermission>(FlowUtil.getSource(treeReceiver));
            Flow flow = new Flow(sources, sinks);
            checker.getFlowAnalizer().getAllFlows().add(Pair.of(getCurrentPath(), flow));
            checker.getFlowAnalizer().getForbiddenAssignmentFlows().add(flow);
        }
    }

    @Override
    protected void commonAssignmentCheck(AnnotatedTypeMirror varType,
            AnnotatedTypeMirror valueType, Tree valueTree, /*@CompilerMessageKey*/String errorKey,
            boolean isLocalVariableAssignement) {

        super.commonAssignmentCheck(varType, valueType, valueTree, errorKey,
                isLocalVariableAssignement);

        Set<FlowPermission> sinks = new HashSet<FlowPermission>(FlowUtil.getSink(varType));
        Set<FlowPermission> sources = new HashSet<FlowPermission>(FlowUtil.getSource(valueType));
        Flow flow = new Flow(sources, sinks);
        checker.getFlowAnalizer().getAssignmentFlows().add(flow);
        boolean success = checker.getTypeHierarchy().isSubtype(valueType, varType);
        if (!success) {
            checker.getFlowAnalizer().getAllFlows().add(Pair.of(getCurrentPath(), flow));
            checker.getFlowAnalizer().getForbiddenAssignmentFlows().add(flow);
        }
    }

    private boolean warnForbiddenFlows(final AnnotatedTypeMirror type,  Tree tree) {

        if (!areFlowsValid(type, tree)) {
            StringBuffer buf = new StringBuffer();
            for (Flow flow : checker.getFlowPolicy().forbiddenFlows(type)) {
                buf.append(flow.toString() + "\n");
            }
            checker.report(Result.failure("forbidden.flow", type.toString(), buf.toString()), tree);
            return false;
        }
        return true;
    }

    void reportError(AnnotatedTypeMirror type, Tree tree) {
        StringBuffer buf = new StringBuffer();
        for (Flow flow : checker.getFlowPolicy().forbiddenFlows(type)) {
            buf.append(flow.toString() + "\n");
        }
        checker.report(Result.failure("forbidden.flow", type.toString(), buf.toString()), tree);
    }

    private boolean areFlowsValid(final AnnotatedTypeMirror atm, Tree tree) {
        
//TODO: Only allow top for locals and upper bounds
        boolean isLocal = false;//atm.getKind() != null
//                && atm.getKind() == TypeKind.LOCAL_VARIABLE;
        if ((isLocal || this.topAllowed) && FlowUtil.isTop(atm)) {
            // Local variables are allowed to be top type so a more specific
            // type can
            // be inferred.
            return true;
        }

        Set<FlowPermission> sinks = new HashSet<FlowPermission>(FlowUtil.getSink(atm));
        Set<FlowPermission> sources = new HashSet<FlowPermission>(FlowUtil.getSource(atm));
        Flow flow = new Flow(sources, sinks);
        checker.getFlowAnalizer().getTypeFlows().add(flow);

        final FlowPolicy flowPolicy = checker.getFlowPolicy();
        if (flowPolicy != null) {
            boolean allowed = checker.getFlowPolicy().areFlowsAllowed(atm);
            if (!allowed) {
                checker.getFlowAnalizer().getAllFlows().add(Pair.of(getCurrentPath(), flow));
                checker.getFlowAnalizer().getForbiddenTypeFlows().add(flow);
            }
            return allowed;
        }
        return true;
    }

    @Override
    protected BaseTypeValidator createTypeValidator() {
        return new FlowTypeValidator(checker, this, atypeFactory);
    }

    protected class FlowTypeValidator extends BaseTypeValidator {
        private final FlowVisitor flowVisitor;
        public FlowTypeValidator(BaseTypeChecker<?> checker, FlowVisitor visitor,
                AnnotatedTypeFactory atypeFactory) {
            super(checker, visitor, atypeFactory);
            this.flowVisitor = visitor;
        }

        @Override
        public Void visitTypeVariable(AnnotatedTypeVariable type, Tree tree) {
            // Keep in sync with visitWildcard
            // getUpperBound() has side effects we don't want
            // So we just get the field
            AnnotatedTypeMirror upperbound = type.getUpperBoundField();
            if (upperbound != null) {
                // Allow top on upper bounds
                flowVisitor.setAllowTop(true);
            }
            Void tmpReturn = super.visitTypeVariable(type, tree);
            // Disallow top, done processing type variable
            flowVisitor.setAllowTop(false);
            return tmpReturn;
        }

        @Override
        public Void visitWildcard(AnnotatedWildcardType type, Tree tree) {
            // Keep in sync with visitTypeVariable
            // getExtendsBound() has side effects we don't want
            // So we just get the field
            AnnotatedTypeMirror upperbound = type.getExtendsBoundField();
            if (upperbound != null) {
                // Allow top on upper bounds
                flowVisitor.setAllowTop(true);
            }
            Void tmpReturn = super.visitWildcard(type, tree);
            // Disallow top, done processing wildcard
            flowVisitor.setAllowTop(false);
            return tmpReturn;
        }

        @Override
        protected void reportError(final AnnotatedTypeMirror type, final Tree p) {
            StringBuffer buf = new StringBuffer();
            for (Flow flow : ((FlowChecker) checker).getFlowPolicy().forbiddenFlows(type)) {
                buf.append(flow.toString() + "\n");
            }
            checker.report(Result.failure("forbidden.flow", type.toString(), buf.toString()), p);

            isValid = false;
        }

        @Override
        protected void reportValidityResult(final String errorType, final AnnotatedTypeMirror type,
                final Tree p) {

            checker.report(Result.failure(errorType, type.toString()), p);
            isValid = false;
        }

    }

    /**
     * Do not warn if any type is ANY->{}
     * 
     * This turned on when visiting type parameters or wildcards with upper
     * bounds.
     * 
     * @param topAllowed
     */
    public void setAllowTop(boolean topAllowed) {
        this.topAllowed = topAllowed;

    }
}

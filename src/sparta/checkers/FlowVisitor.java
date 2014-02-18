package sparta.checkers;

/*>>>
import checkers.compilermsgs.quals.*;
*/

import android.text.Annotation;

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

import javacutils.AnnotationUtils;
import javacutils.InternalUtils;
import javacutils.Pair;
import javacutils.TreeUtils;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;

import sparta.checkers.quals.DependentPermissions;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.ParameterizedFlowPermission;
import sparta.checkers.quals.MayRequiredPermissions;
import sparta.checkers.quals.RequiredPermissions;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;

public class FlowVisitor extends BaseTypeVisitor<FlowAnnotatedTypeFactory> {

    private boolean topAllowed = false;
    /**
     * Should the checker warn when a non-literal source is used in
     * a conditional (regardless of the flow policy)?  
     */
    public static final String CHECK_CONDITIONALS_OPTION = "checkconditionals";
    
    private ParameterizedFlowPermission ANY;
    private ParameterizedFlowPermission CONDITIONAL;
    private ParameterizedFlowPermission LITERAL;
    private boolean checkConditional = Boolean.valueOf(checker.getOption(CHECK_CONDITIONALS_OPTION, "false"));

    public FlowVisitor(BaseTypeChecker checker) {
        super(checker);
        
        ANY = new ParameterizedFlowPermission(FlowPermission.ANY);
        CONDITIONAL = new ParameterizedFlowPermission(FlowPermission.CONDITIONAL);
        LITERAL = new ParameterizedFlowPermission(FlowPermission.LITERAL);
    }
@Override
protected FlowAnnotatedTypeFactory createTypeFactory() {
    return new FlowAnnotatedTypeFactory(checker);
}
    @Override
    public boolean isValidUse(AnnotatedDeclaredType declarationType,
            AnnotatedDeclaredType useType, Tree tree) {

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

    private void ensureConditionalSink(ExpressionTree tree) {

        
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(tree);
        final Set<ParameterizedFlowPermission> sinks = Flow.getSinks(type);
        if (!ParameterizedFlowPermission.coarsePermissionExists(ANY, sinks) && 
            !ParameterizedFlowPermission.coarsePermissionExists(CONDITIONAL, sinks)) {
            checker.report(Result.failure("condition.flow", type.getAnnotations()), tree);

        }
        if(checkConditional){
            final Set<ParameterizedFlowPermission> sources = Flow.getSources(type);
            if(sources.size() > 1 || 
                    !ParameterizedFlowPermission.coarsePermissionExists(LITERAL, sources)){
                checker.report(Result.failure("condition.flow", sources), tree);
                
            }

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

        if (!atypeFactory.getTypeHierarchy().isSubtype(treeReceiver, methodReceiver)) {
            Set<ParameterizedFlowPermission> sinks = Flow.getSinks(methodReceiver);
            Set<ParameterizedFlowPermission> sources = Flow.getSources(treeReceiver);
            Flow flow = new Flow(sources, sinks);
            atypeFactory.getFlowAnalizer().getAllFlows().add(Pair.of(getCurrentPath(), flow));
            atypeFactory.getFlowAnalizer().getForbiddenAssignmentFlows().add(flow);
        }
    }

    @Override
    protected void commonAssignmentCheck(AnnotatedTypeMirror varType,
            AnnotatedTypeMirror valueType, Tree valueTree, /*@CompilerMessageKey*/String errorKey,
            boolean isLocalVariableAssignement) {

        super.commonAssignmentCheck(varType, valueType, valueTree, errorKey,
                isLocalVariableAssignement);

        Set<ParameterizedFlowPermission> sinks = Flow.getSinks(varType);
        Set<ParameterizedFlowPermission> sources = Flow.getSources(valueType);
        Flow flow = new Flow(sources, sinks);
        atypeFactory.getFlowAnalizer().getAssignmentFlows().add(flow);
        boolean success = atypeFactory.getTypeHierarchy().isSubtype(valueType, varType);
        if (!success) {
            atypeFactory.getFlowAnalizer().getAllFlows().add(Pair.of(getCurrentPath(), flow));
            atypeFactory.getFlowAnalizer().getForbiddenAssignmentFlows().add(flow);
        }
    }

    private void warnForbiddenFlows(final AnnotatedTypeMirror type,  Tree tree) {
        if (!areFlowsValid(type, tree)) {
           reportError(type, tree);
        }
    }

    void reportError(AnnotatedTypeMirror type, Tree tree) {
        StringBuffer buf = new StringBuffer();
        for (Flow flow : atypeFactory.getFlowPolicy().forbiddenFlows(type)) {
            buf.append(flow.toString() + "\n");
        }
        checker.report(Result.failure("forbidden.flow", type.toString(), buf.toString()), tree);
    }

    private boolean areFlowsValid(final AnnotatedTypeMirror atm, Tree tree) {

        Element ele = InternalUtils.symbol(tree);
        boolean local = (ele != null && ele.getKind() == ElementKind.LOCAL_VARIABLE);
        boolean field = (ele != null && ele.getKind() == ElementKind.FIELD);
        boolean wild = atm.getKind() == TypeKind.WILDCARD;
        boolean typeVar = atm.getKind() == TypeKind.TYPEVAR;
        boolean typePara = (ele != null && ele.getKind() == ElementKind.TYPE_PARAMETER);
        boolean nullLiteral = tree.getKind() == Tree.Kind.NULL_LITERAL;

        if ((local || this.topAllowed || typePara|| typeVar|| wild) && Flow.isTop(atm))
        {
            // Local variables are allowed to be top type so a more specific
            // type can be inferred.
            return true;
        }

        //The null literal is allow to be bottom
        //A variable may be bottom if null is assigned to it
        //and flow refinement changes the type.
        if(Flow.isBottom(atm) && (local || nullLiteral || field)) {
            
            //Make sure bottom was not explicitly written by the programmer.
            if (tree instanceof VariableTree) {
                //Get the annotations written on the type
                VariableTree vtree = (VariableTree) tree;
                List<? extends AnnotationTree> annoTrees = vtree.getModifiers().getAnnotations();
                List<AnnotationMirror> annos = InternalUtils
                        .annotationsFromTypeAnnotationTrees(annoTrees);
                
                //Get the sets of sources and sinks off the annotations
                Flow flow = new Flow();
                for (AnnotationMirror anno : annos) {
                    if (AnnotationUtils.areSameByClass(anno, Source.class)) {
                        flow.addSource(Flow.getSources(anno));
                    } else if (AnnotationUtils.areSameByClass(anno, Sink.class)) {
                        flow.addSink(Flow.getSinks(anno));
                    }
                }
                //If programmer actually wrote {}->ANY, 
                //then give an error
                if(flow.isBottom()){
                    return false; 
                }
            }
            
            //if the flow is bottom because it is null
            //or it is a local variable that was inferred to be null
            return true;
        }


        Flow flow = new Flow(atm);
        atypeFactory.getFlowAnalizer().getTypeFlows().add(flow);

        final FlowPolicy flowPolicy = atypeFactory.getFlowPolicy();
        if (flowPolicy != null) {
            boolean allowed = flowPolicy.areFlowsAllowed(atm);
            if (!allowed) {
                atypeFactory.getFlowAnalizer().getAllFlows().add(Pair.of(getCurrentPath(), flow));
                atypeFactory.getFlowAnalizer().getForbiddenTypeFlows().add(flow);
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
        public FlowTypeValidator(BaseTypeChecker checker, FlowVisitor visitor,
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
            for (Flow flow : ((FlowAnnotatedTypeFactory)atypeFactory).getFlowPolicy().forbiddenFlows(type)) {
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

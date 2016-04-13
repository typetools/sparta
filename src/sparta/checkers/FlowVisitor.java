package sparta.checkers;

/*>>>
import org.checkerframework.checker.compilermsgs.qual.*;
*/

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeValidator;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedPrimitiveType;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.Tree;

import sparta.checkers.validator.BaseFlowVisitor;

public class FlowVisitor extends BaseFlowVisitor {

    public static final String CHECK_CONDITIONALS_OPTION = "checkconditionals";
    
    private boolean checkConditional = Boolean.valueOf(checker.getOption(CHECK_CONDITIONALS_OPTION, "false"));

    public FlowVisitor(BaseTypeChecker checker) {
        super(checker);
        if(checkConditional){
            this.setValidator(new ConditionalValidator(atypeFactory, checker),
                    new AddsSourceValidator(atypeFactory, checker));
        }else{
            this.setValidator( new AddsSourceValidator(atypeFactory, checker));
        }
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
            AnnotatedTypeMirror valueType, Tree valueTree, /*@CompilerMessageKey*/String errorKey) {

        super.commonAssignmentCheck(varType, valueType, valueTree, errorKey);
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

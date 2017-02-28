package sparta.checkers.validator;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;

import sparta.checkers.FlowAnnotatedTypeFactory;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
/**
 * Calls the Validator on each visited tree.
 * @author smillst
 *
 */
public class BaseFlowVisitor extends BaseTypeVisitor<FlowAnnotatedTypeFactory> {
    protected Validator validator;

    public BaseFlowVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    protected void setValidator(Validator... validator) {
        this.validator = new ValidatorList(validator);
    }

    @Override
    public void processClassTree(ClassTree node) {
        validator.visitClass(node);
        super.processClassTree(node);
    }

    @Override
    public Void visitMethod(MethodTree node, Void p) {
        validator.visitMethod(node);
        return super.visitMethod(node, p);
    }

    @Override
    public Void visitTypeParameter(TypeParameterTree node, Void p) {
        validator.visitTypeParameter(node);
        return super.visitTypeParameter(node, p);
    }

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        validator.visitVariable(node);
        return super.visitVariable(node, p);
    }

    @Override
    public Void visitAssignment(AssignmentTree node, Void p) {
        validator.visitAssignment(node);
        return super.visitAssignment(node, p);
    }

    @Override
    public Void visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
        validator.visitEnhancedForLoop(node);
        return super.visitEnhancedForLoop(node, p);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        validator.visitMethodInvocation(node);
        return super.visitMethodInvocation(node, p);
    }

    @Override
    public Void visitNewClass(NewClassTree node, Void p) {
        validator.visitNewClass(node);
        return super.visitNewClass(node, p);
    }

    @Override
    public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
        validator.visitLambdaExpression(node);
        return super.visitLambdaExpression(node, p);
    }

    @Override
    public Void visitMemberReference(MemberReferenceTree node, Void p) {
        validator.visitMemberReference(node);
        return super.visitMemberReference(node, p);
    }

    @Override
    public Void visitReturn(ReturnTree node, Void p) {
        validator.visitReturn(node);
        return super.visitReturn(node, p);
    }

    @Override
    public Void visitAnnotation(AnnotationTree node, Void p) {
        validator.visitAnnotation(node);
        return super.visitAnnotation(node, p);
    }

    @Override
    public Void visitConditionalExpression(ConditionalExpressionTree node,
            Void p) {
        validator.visitConditionalExpression(node);
        return super.visitConditionalExpression(node, p);
    }

    @Override
    public Void visitUnary(UnaryTree node, Void p) {
        validator.visitUnary(node);
        return super.visitUnary(node, p);
    }

    @Override
    public Void visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
        validator.visitCompoundAssignment(node);
        return super.visitCompoundAssignment(node, p);
    }

    @Override
    public Void visitNewArray(NewArrayTree node, Void p) {
        validator.visitNewArray(node);
        return super.visitNewArray(node, p);
    }

    @Override
    public Void visitTypeCast(TypeCastTree node, Void p) {
        validator.visitTypeCast(node);
        return super.visitTypeCast(node, p);
    }

    @Override
    public Void visitInstanceOf(InstanceOfTree node, Void p) {
        validator.visitInstanceOf(node);
        return super.visitInstanceOf(node, p);
    }

    @Override
    public Void visitArrayAccess(ArrayAccessTree node, Void p) {
        validator.visitArrayAccess(node);
        return super.visitArrayAccess(node, p);
    }

    @Override
    public Void visitIdentifier(IdentifierTree node, Void p) {
        validator.visitIdentifier(node);
        return super.visitIdentifier(node, p);
    }

    @Override
    public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
        validator.visitCompilationUnit(node);
        return super.visitCompilationUnit(node, p);
    }

    @Override
    public Void visitIf(IfTree node, Void p) {
        validator.visitIf(node);
        return super.visitIf(node, p);
    }

    @Override
    public Void visitSwitch(SwitchTree node, Void p) {
        validator.visitSwitch(node);
        return super.visitSwitch(node, p);
    }

    @Override
    public Void visitCase(CaseTree node, Void p) {
        ExpressionTree exprTree = node.getExpression();
        if (exprTree != null)
            validator.visitCase(node);
        return super.visitCase(node, p);
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, Void p) {
        validator.visitDoWhileLoop(node);
        return super.visitDoWhileLoop(node, p);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, Void p) {
        validator.visitWhileLoop(node);
        return super.visitWhileLoop(node, p);
    }

}

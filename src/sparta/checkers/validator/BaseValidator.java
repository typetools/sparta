package sparta.checkers.validator;

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
import com.sun.source.tree.ForLoopTree;
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
 * Base implementation of Validator. Each method is a no op. This way subclasses
 * only have to implement relevent validator methods.
 *
 * @author smillst
 *
 */
public class BaseValidator implements Validator {
    @Override
    public void visitAnnotation(AnnotationTree node) {
    }

    @Override
    public void visitArrayAccess(ArrayAccessTree node) {
    }

    @Override
    public void visitAssignment(AssignmentTree node) {
    }

    @Override
    public void visitClass(ClassTree node) {
    }

    @Override
    public void visitMethod(MethodTree node) {
    }

    @Override
    public void visitTypeParameter(TypeParameterTree node) {
    }

    @Override
    public void visitVariable(VariableTree node) {
    }

    @Override
    public void visitEnhancedForLoop(EnhancedForLoopTree node) {
    }

    @Override
    public void visitMethodInvocation(MethodInvocationTree node) {
    }

    @Override
    public void visitNewClass(NewClassTree node) {
    }

    @Override
    public void visitLambdaExpression(LambdaExpressionTree node) {
    }

    @Override
    public void visitMemberReference(MemberReferenceTree node) {
    }

    @Override
    public void visitReturn(ReturnTree node) {
    }

    @Override
    public void visitConditionalExpression(ConditionalExpressionTree node) {
    }

    @Override
    public void visitUnary(UnaryTree node) {
    }

    @Override
    public void visitCompoundAssignment(CompoundAssignmentTree node) {
    }

    @Override
    public void visitNewArray(NewArrayTree node) {
    }

    @Override
    public void visitTypeCast(TypeCastTree node) {
    }

    @Override
    public void visitInstanceOf(InstanceOfTree node) {
    }

    @Override
    public void visitIdentifier(IdentifierTree node) {
    }

    @Override
    public void visitCompilationUnit(CompilationUnitTree node) {
    }

    @Override
    public void visitIf(IfTree node) {
    }

    @Override
    public void visitSwitch(SwitchTree node) {
    }

    @Override
    public void visitCase(CaseTree node) {
    }

    @Override
    public void visitDoWhileLoop(DoWhileLoopTree node) {
    }

    @Override
    public void visitWhileLoop(WhileLoopTree node) {
    }

    @Override
    public void visitForLoop(ForLoopTree node) {
    }
}

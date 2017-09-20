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
 * Interface for validating trees
 *
 * See {@link BaseValidator}
 *
 * @author smillst
 *
 */
public interface Validator {
    // The following methods do not exist because the BaseTypeVisitor
    // does not visit them
    // public void visitAnnotatedType(AnnotatedTypeTree node);
    // public void visitArrayType(ArrayTypeTree node);
    // public void visitAssert(AssertTree node);
    // public void visitBinary(BinaryTree node);
    // public void visitBlock(BlockTree node);
    // public void visitBreak(BreakTree node);
    // public void visitCase(CaseTree node);

    public void visitAnnotation(AnnotationTree node);

    public void visitArrayAccess(ArrayAccessTree node);

    public void visitAssignment(AssignmentTree node);

    public void visitClass(ClassTree node);

    public void visitMethod(MethodTree node);

    public void visitTypeParameter(TypeParameterTree node);

    public void visitVariable(VariableTree node);

    public void visitEnhancedForLoop(EnhancedForLoopTree node);

    public void visitMethodInvocation(MethodInvocationTree node);

    public void visitNewClass(NewClassTree node);

    public void visitLambdaExpression(LambdaExpressionTree node);

    public void visitMemberReference(MemberReferenceTree node);

    public void visitReturn(ReturnTree node);

    public void visitConditionalExpression(ConditionalExpressionTree node);

    public void visitUnary(UnaryTree node);

    public void visitCompoundAssignment(CompoundAssignmentTree node);

    public void visitNewArray(NewArrayTree node);

    public void visitTypeCast(TypeCastTree node);

    public void visitInstanceOf(InstanceOfTree node);

    public void visitIdentifier(IdentifierTree node);

    public void visitCompilationUnit(CompilationUnitTree node);

    public void visitIf(IfTree node);

    public void visitSwitch(SwitchTree node);

    public void visitCase(CaseTree node);

    public void visitDoWhileLoop(DoWhileLoopTree node);

    public void visitWhileLoop(WhileLoopTree node);

    public void visitForLoop(ForLoopTree node);
}

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
 * A Validator that passes calls to its methods to a list of validators
 *
 * @author smillst
 *
 */
public class ValidatorList implements Validator {
    final Validator[] validators;

    public ValidatorList(Validator... visitors) {
        this.validators = visitors;
    }

    public void visitAnnotation(AnnotationTree node) {
        for (Validator v : validators) {
            v.visitAnnotation(node);
        }
    }

    public void visitArrayAccess(ArrayAccessTree node) {
        for (Validator v : validators) {
            v.visitArrayAccess(node);
        }
    }

    public void visitAssignment(AssignmentTree node) {
        for (Validator v : validators) {
            v.visitAssignment(node);
        }
    }

    public void visitClass(ClassTree node) {
        for (Validator v : validators) {
            v.visitClass(node);
        }
    }

    public void visitMethod(MethodTree node) {
        for (Validator v : validators) {
            v.visitMethod(node);
        }
    }

    public void visitTypeParameter(TypeParameterTree node) {
        for (Validator v : validators) {
            v.visitTypeParameter(node);
        }
    }

    public void visitVariable(VariableTree node) {
        for (Validator v : validators) {
            v.visitVariable(node);
        }
    }

    public void visitEnhancedForLoop(EnhancedForLoopTree node) {
        for (Validator v : validators) {
            v.visitEnhancedForLoop(node);
        }
    }

    public void visitMethodInvocation(MethodInvocationTree node) {
        for (Validator v : validators) {
            v.visitMethodInvocation(node);
        }
    }

    public void visitNewClass(NewClassTree node) {
        for (Validator v : validators) {
            v.visitNewClass(node);
        }
    }

    public void visitLambdaExpression(LambdaExpressionTree node) {
        for (Validator v : validators) {
            v.visitLambdaExpression(node);
        }
    }

    public void visitMemberReference(MemberReferenceTree node) {
        for (Validator v : validators) {
            v.visitMemberReference(node);
        }
    }

    public void visitReturn(ReturnTree node) {
        for (Validator v : validators) {
            v.visitReturn(node);
        }
    }

    public void visitConditionalExpression(ConditionalExpressionTree node) {
        for (Validator v : validators) {
            v.visitConditionalExpression(node);
        }
    }

    public void visitUnary(UnaryTree node) {
        for (Validator v : validators) {
            v.visitUnary(node);
        }
    }

    public void visitCompoundAssignment(CompoundAssignmentTree node) {
        for (Validator v : validators) {
            v.visitCompoundAssignment(node);
        }
    }

    public void visitNewArray(NewArrayTree node) {
        for (Validator v : validators) {
            v.visitNewArray(node);
        }
    }

    public void visitTypeCast(TypeCastTree node) {
        for (Validator v : validators) {
            v.visitTypeCast(node);
        }
    }

    public void visitInstanceOf(InstanceOfTree node) {
        for (Validator v : validators) {
            v.visitInstanceOf(node);
        }
    }

    public void visitIdentifier(IdentifierTree node) {
        for (Validator v : validators) {
            v.visitIdentifier(node);
        }
    }

    public void visitCompilationUnit(CompilationUnitTree node) {
        for (Validator v : validators) {
            v.visitCompilationUnit(node);
        }
    }

    @Override
    public void visitIf(IfTree node) {
        for (Validator v : validators) {
            v.visitIf(node);
        }
    }

    @Override
    public void visitSwitch(SwitchTree node) {
        for (Validator v : validators) {
            v.visitSwitch(node);
        }
    }

    @Override
    public void visitCase(CaseTree node) {
        for (Validator v : validators) {
            v.visitCase(node);
        }
    }

    @Override
    public void visitDoWhileLoop(DoWhileLoopTree node) {
        for (Validator v : validators) {
            v.visitDoWhileLoop(node);
        }
    }

    @Override
    public void visitWhileLoop(WhileLoopTree node) {
        for (Validator v : validators) {
            v.visitWhileLoop(node);
        }
    }

    @Override
    public void visitForLoop(ForLoopTree node) {
        for (Validator v : validators) {
            v.visitForLoop(node);
        }
    }
}

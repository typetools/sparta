package sparta.checkers;

import java.util.Collections;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;

import checkers.basetype.BaseTypeChecker;
import checkers.quals.DefaultLocation;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.BasicAnnotatedTypeFactory;
import checkers.types.TreeAnnotator;

public class FlowAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<FlowChecker> {

    public FlowAnnotatedTypeFactory(FlowChecker checker, CompilationUnitTree root) {
        super(checker, root);

        // Use the bottom type as default for everything but local variables.
        defaults.addAbsoluteDefault(checker.NOFLOWSOURCES, Collections.singleton(DefaultLocation.ALL_EXCEPT_LOCALS));
        // Use the top type for local variables and let flow refine the type.
        defaults.setLocalVariableDefault(Collections.singleton(checker.ANYFLOWSOURCES));

        // Default is always the top annotation for sinks.
        defaults.addAbsoluteDefault(checker.NOFLOWSINKS, Collections.singleton(DefaultLocation.ALL));
        // But let's send null down any sink.
        this.treeAnnotator.addTreeKind(Tree.Kind.NULL_LITERAL, checker.ANYFLOWSINKS);

        this.postInit();
    }

    @Override
    public TreeAnnotator createTreeAnnotator(FlowChecker checker) {
        return new FlowTreeAnnotator(checker);
    }

    private class FlowTreeAnnotator extends TreeAnnotator {

        public FlowTreeAnnotator(BaseTypeChecker checker) {
            super(checker, FlowAnnotatedTypeFactory.this);
        }

        @Override
        public Void visitCompoundAssignment(CompoundAssignmentTree node, AnnotatedTypeMirror type) {
            AnnotatedTypeMirror rhs = getAnnotatedType(node.getExpression());
            AnnotatedTypeMirror lhs = getAnnotatedType(node.getVariable());
            Set<AnnotationMirror> lubs = qualHierarchy.leastUpperBounds(rhs.getAnnotations(), lhs.getAnnotations());
            type.replaceAnnotations(lubs);
            return super.visitCompoundAssignment(node, type);
        }

        @Override
        public Void visitBinary(BinaryTree node, AnnotatedTypeMirror type) {
            AnnotatedTypeMirror a = getAnnotatedType(node.getLeftOperand());
            AnnotatedTypeMirror b = getAnnotatedType(node.getRightOperand());
            Set<AnnotationMirror> lubs = qualHierarchy.leastUpperBounds(a.getAnnotations(), b.getAnnotations());
            type.replaceAnnotations(lubs);
            return super.visitBinary(node, type);
        }

        @Override
        public Void visitUnary(UnaryTree node, AnnotatedTypeMirror type) {
            AnnotatedTypeMirror exp = getAnnotatedType(node.getExpression());
            type.replaceAnnotations(exp.getAnnotations());
            return super.visitUnary(node, type);
        }

    }
}

package sparta.checkers;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.qual.StubFiles;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedTypeVariable;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedWildcardType;

import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;

import java.util.Set;

import javax.lang.model.type.TypeKind;

import sparta.checkers.quals.PFPermission;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.Tree;

@StubFiles("flow.astub")
public class FlowShow extends FlowChecker {

    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new FlowShowVisitor(this);
    }

    protected class FlowShowVisitor extends BaseTypeVisitor<FlowAnnotatedTypeFactory> {

        public FlowShowVisitor(BaseTypeChecker checker) {
            super(checker);
        }

        @Override
        public FlowAnnotatedTypeFactory createTypeFactory() {
            return new FlowAnnotatedTypeFactory(FlowShow.this);
        }

        @Override
        public Void scan(Tree tree, Void p) {
            super.scan(tree, p);
            if (TreeUtils.isExpressionTree(tree) && !(tree instanceof AnnotationTree)
                    && !(tree.getKind() == Tree.Kind.NULL_LITERAL)) {
                AnnotatedTypeMirror type = this.atypeFactory.getAnnotatedType(tree);
                if (type.getKind() == TypeKind.WILDCARD) {
                    type = ((AnnotatedWildcardType) type).getExtendsBound();
                } else if (type.getKind() == TypeKind.TYPEVAR) {
                    type = ((AnnotatedTypeVariable) type).getUpperBound();
                }

                boolean show = false;

                if (!AnnotationUtils.areSame(type.getAnnotationInHierarchy(atypeFactory.NOSOURCE), atypeFactory.NOSOURCE)) {
                    show = true;
                }
                if (!AnnotationUtils.areSame(type.getAnnotationInHierarchy(atypeFactory.NOSINK), atypeFactory.NOSINK)) {
                    show = true;
                }
                if (show) {
                    Set<PFPermission> src = Flow.getSources(type);
                    String stsrc = src.isEmpty() ? "NONE" : src.toString();
                    Set<PFPermission> snk = Flow.getSinks(type);
                    String stsnk = snk.isEmpty() ? "NONE" : snk.toString();
                    String msg = "FLOW TREE " + tree + " KIND " + tree.getKind() + " SOURCES "
                            + stsrc + " SINKS " + stsnk;
                    trees.printMessage(javax.tools.Diagnostic.Kind.OTHER, msg, tree, currentRoot);
                }
            }
            return null;
        }
    }
}
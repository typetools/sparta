package sparta.checkers;

import checkers.quals.StubFiles;
import checkers.quals.TypeQualifiers;
import checkers.source.SourceVisitor;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedTypeVariable;
import checkers.types.AnnotatedTypeMirror.AnnotatedWildcardType;

import javacutils.AnnotationUtils;
import javacutils.TreeUtils;

import java.util.List;

import javax.lang.model.type.TypeKind;

import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;

@TypeQualifiers({ Source.class, Sink.class, PolySource.class, PolySink.class })
@StubFiles("flow.astub")
public class FlowShow extends FlowChecker {
    @Override
    public FlowAnnotatedTypeFactory createFactory(CompilationUnitTree root) {
        return new FlowAnnotatedTypeFactory(this, root);
    }

    @Override
    protected SourceVisitor<?, ?, ?, ?> createSourceVisitor(CompilationUnitTree root) {
        return new FlowShowVisitor(this, root);
    }

    protected class FlowShowVisitor extends
            SourceVisitor<FlowChecker, FlowAnnotatedTypeFactory, Void, Void> {

        private final Trees treemsg;

        public FlowShowVisitor(FlowShow checker, CompilationUnitTree root) {
            super(checker, root);
            treemsg = Trees.instance(processingEnv);
        }

        @Override
        public Void scan(Tree tree, Void p) {
            super.scan(tree, p);
            if (TreeUtils.isExpressionTree(tree) && !(tree instanceof AnnotationTree)
                    && !(tree.getKind() == Tree.Kind.NULL_LITERAL)) {
                AnnotatedTypeMirror type = this.atypeFactory.getAnnotatedType(tree);
                if (type.getKind() == TypeKind.WILDCARD) {
                    type = ((AnnotatedWildcardType) type).getEffectiveExtendsBound();
                } else if (type.getKind() == TypeKind.TYPEVAR) {
                    type = ((AnnotatedTypeVariable) type).getEffectiveUpperBound();
                }

                boolean show = false;

                if (!AnnotationUtils.areSame(type.getAnnotationInHierarchy(NOSOURCE), NOSOURCE)) {
                    show = true;
                }
                if (!AnnotationUtils.areSame(type.getAnnotationInHierarchy(NOSINK), NOSINK)) {
                    show = true;
                }
                if (show) {
                    List<FlowPermission> src = getSource(type);
                    String stsrc = src.isEmpty() ? "NONE" : src.toString();
                    List<FlowPermission> snk = getSink(type);
                    String stsnk = snk.isEmpty() ? "NONE" : snk.toString();
                    String msg = "FLOW TREE " + tree + " KIND " + tree.getKind() + " SOURCES "
                            + stsrc + " SINKS " + stsnk;
                    treemsg.printMessage(javax.tools.Diagnostic.Kind.OTHER, msg, tree, currentRoot);
                }
            }
            return null;
        }
    }
}
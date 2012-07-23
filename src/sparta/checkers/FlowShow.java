package sparta.checkers;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;

import checkers.quals.StubFiles;
import checkers.quals.TypeQualifiers;
import checkers.source.SourceVisitor;
import checkers.types.AnnotatedTypeFactory;
import checkers.types.AnnotatedTypeMirror;
import checkers.util.AnnotationUtils;

import sparta.checkers.quals.FlowSinks;
import sparta.checkers.quals.FlowSources;
import sparta.checkers.quals.PolyFlowSinks;
import sparta.checkers.quals.PolyFlowSources;

@TypeQualifiers({FlowSources.class, FlowSinks.class,
    PolyFlowSources.class, PolyFlowSinks.class})
@StubFiles("flow.astub")
public class FlowShow extends FlowChecker {
    @Override
    public AnnotatedTypeFactory createFactory(CompilationUnitTree root) {
        return new FlowAnnotatedTypeFactory(this, root);
    }

    @Override
    protected SourceVisitor<?, ?> createSourceVisitor(CompilationUnitTree root) {
        return new FlowShowVisitor(this, root);
    }

    protected class FlowShowVisitor extends SourceVisitor<Void, Void> {

        private Trees treemsg;

        public FlowShowVisitor(FlowShow checker, CompilationUnitTree root) {
            super(checker, root);
            treemsg = Trees.instance(env);
        }

        @Override
        public Void scan(Tree tree, Void p) {
            super.scan(tree, p);
            if (tree instanceof ExpressionTree &&
                    !(tree instanceof AnnotationTree) &&
                    !(tree.getKind()==Tree.Kind.NULL_LITERAL)) {
                AnnotatedTypeMirror type = this.atypeFactory.getAnnotatedType(tree);
                boolean show = false;

                if (!AnnotationUtils.areSame(type.getAnnotationInHierarchy(NOFLOWSOURCES), NOFLOWSOURCES)) {
                    show = true;
                }
                if (!AnnotationUtils.areSame(type.getAnnotationInHierarchy(NOFLOWSINKS), NOFLOWSINKS)) {
                    show = true;
                }
                if (show) {
                    String msg = "tree kind: " + tree.getKind() +
                            "   type: " + type;
                    treemsg.printMessage(javax.tools.Diagnostic.Kind.OTHER, msg, tree, currentRoot);
                }
            }
            return null;
        }
    }
}
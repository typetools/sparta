package sparta.checkers;

import java.util.Collections;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;

import checkers.quals.DefaultLocation;
import checkers.types.BasicAnnotatedTypeFactory;

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
}

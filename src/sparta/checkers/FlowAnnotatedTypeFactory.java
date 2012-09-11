package sparta.checkers;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;

import sparta.checkers.quals.NoFlow;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;

import checkers.quals.DefaultLocation;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.BasicAnnotatedTypeFactory;
import checkers.util.InternalUtils;
import checkers.util.QualifierDefaults.DefaultApplier;


public class FlowAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<FlowChecker> {

    public FlowAnnotatedTypeFactory(FlowChecker checker, CompilationUnitTree root) {
        super(checker, root);

        // The list of Elements that should be treated more conservatively.
        List<Element> conservative = new ArrayList<Element>();
        Elements elemutils = env.getElementUtils();
        conservative.add(elemutils.getPackageElement("android"));
        conservative.add(elemutils.getPackageElement("com.android"));
        conservative.add(elemutils.getPackageElement("org.apache.http"));
        conservative.add(elemutils.getPackageElement("java"));
        /* Is using all of java.* too conservative? At least the following
         * packages should be conservative:
        conservative.add(elemutils.getPackageElement("java.io"));
        conservative.add(elemutils.getPackageElement("java.net"));
        conservative.add(elemutils.getPackageElement("java.sql"));
        conservative.add(elemutils.getPackageElement("java.util"));
        // We might want to exclude java.lang.
         * TODO: java.lang.reflect should be conservative.
        conservative.add(elemutils.getPackageElement("java.lang"));
        */


        // Use the bottom type as default for everything but local variables.
        defaults.addAbsoluteDefault(checker.NOFLOWSOURCES, DefaultLocation.OTHERWISE);
        // Use the top type for local variables and let flow refine the type.
        defaults.addAbsoluteDefault(checker.ANYFLOWSOURCES, DefaultLocation.LOCALS);

        for (Element cons : conservative) {
            // Use the top type for return types in the Android packages
            defaults.addElementDefault(cons, checker.ANYFLOWSOURCES, DefaultLocation.RETURNS);
        }

        // Default is always the top annotation for sinks.
        defaults.addAbsoluteDefault(checker.NOFLOWSINKS, DefaultLocation.ALL);
        // But let's send null down any sink.
        this.treeAnnotator.addTreeKind(Tree.Kind.NULL_LITERAL, checker.ANYFLOWSINKS);

        this.postInit();
    }

    // Test whether the element or any of its enclosing elements has
    // the NoFlow annotation.
    private boolean isNoFlowElement(Element element) {
        while (element != null) {
            if (this.getDeclAnnotation(element, NoFlow.class) != null) {
                return true;
            }
            element = element.getEnclosingElement();
        }
        return false;
    }

    @Override
    protected void annotateImplicit(Tree tree, AnnotatedTypeMirror type) {
        Element element = InternalUtils.symbol(tree);
        if (element != null && isNoFlowElement(element)) {
            new DefaultApplier(element, DefaultLocation.RETURNS, type).scan(type, checker.NOFLOWSOURCES);
            // new DefaultApplier(element, DefaultLocation.PARAMETERS, type).scan(type, checker.NOFLOWSOURCES);
        }

        super.annotateImplicit(tree, type);
    }

    @Override
    protected void annotateImplicit(Element element, AnnotatedTypeMirror type) {
        if (element != null && isNoFlowElement(element)) {
            new DefaultApplier(element, DefaultLocation.RETURNS, type).scan(type, checker.NOFLOWSOURCES);
            // new DefaultApplier(element, DefaultLocation.PARAMETERS, type).scan(type, checker.NOFLOWSOURCES);
        }

        super.annotateImplicit(element, type);
    }

}

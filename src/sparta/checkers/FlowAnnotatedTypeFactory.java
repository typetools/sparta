package sparta.checkers;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;

import sparta.checkers.quals.ConservativeFlow;
import sparta.checkers.quals.NoFlow;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;

import checkers.quals.DefaultLocation;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.BasicAnnotatedTypeFactory;
import checkers.util.ElementUtils;
import checkers.util.InternalUtils;
import checkers.util.QualifierDefaults.DefaultApplier;


public class FlowAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<FlowChecker> {

    public FlowAnnotatedTypeFactory(FlowChecker checker, CompilationUnitTree root) {
        super(checker, root);

        // Use the bottom type as default for everything but local variables.
        defaults.addAbsoluteDefault(checker.NOFLOWSOURCES, DefaultLocation.OTHERWISE);
        // Use the top type for local variables and let flow refine the type.
        defaults.addAbsoluteDefault(checker.ANYFLOWSOURCES, DefaultLocation.LOCALS);

        // Default is always the top annotation for sinks.
        defaults.addAbsoluteDefault(checker.NOFLOWSINKS, DefaultLocation.ALL);
        // But let's send null down any sink.
        this.treeAnnotator.addTreeKind(Tree.Kind.NULL_LITERAL, checker.ANYFLOWSINKS);

        this.postInit();
    }

    @Override
    public void annotateImplicit(Tree tree, AnnotatedTypeMirror type) {
        Element element = InternalUtils.symbol(tree);
        handleDefaulting(element, type);
        super.annotateImplicit(tree, type);
    }

    @Override
    public void annotateImplicit(Element element, AnnotatedTypeMirror type) {
        handleDefaulting(element, type);
        super.annotateImplicit(element, type);
    }

    protected void handleDefaulting(final Element element, final AnnotatedTypeMirror type) {
        Element iter = element;
        while (iter != null) {
            if (this.getDeclAnnotation(iter, NoFlow.class) != null) {
                // Use no flow sources for the return type.
                new DefaultApplier(element, DefaultLocation.RETURNS, type).scan(type, checker.NOFLOWSOURCES);
                // Nothing needs to be done for parameters.
                // new DefaultApplier(start, DefaultLocation.PARAMETERS, type).scan(type, checker.NOFLOWSOURCES);
                // Cache the result for future uses.
                // defaults.addElementDefault(element, checker.NOFLOWSOURCES, DefaultLocation.RETURNS);
                return;
            } else if (this.getDeclAnnotation(iter, ConservativeFlow.class) != null) {
                // Use the top type for return types in the Android packages
                new DefaultApplier(element, DefaultLocation.RETURNS, type).scan(type, checker.ANYFLOWSOURCES);
                // Nothing needs to be done for parameters.
                // new DefaultApplier(start, DefaultLocation.PARAMETERS, type).scan(type, checker.NOFLOWSOURCES);
                // Cache the result for future uses.
                // defaults.addElementDefault(element, checker.ANYFLOWSOURCES, DefaultLocation.RETURNS);
                return;
            }
            if (iter instanceof PackageElement) {
                iter = ElementUtils.parentPackage(this.elements, (PackageElement) iter);
            } else {
                iter = iter.getEnclosingElement();
            }
        }
    }

}

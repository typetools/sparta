package sparta.checkers;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

import checkers.types.AnnotatedTypeFactory;
import checkers.util.*;
import sparta.checkers.quals.ConservativeFlow;
import sparta.checkers.quals.FlowSinks;
import sparta.checkers.quals.FlowSources;
import sparta.checkers.quals.NoFlow;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;

import checkers.quals.DefaultLocation;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.BasicAnnotatedTypeFactory;
import checkers.util.QualifierDefaults.DefaultApplier;

import java.util.*;

import static checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;


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
        treeAnnotator.addTreeKind(Tree.Kind.NULL_LITERAL, checker.ANYFLOWSINKS);


        //Literals, other than null are different too
        treeAnnotator.addTreeKind(Tree.Kind.INT_LITERAL, checker.LITERALFLOWSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.LONG_LITERAL, checker.LITERALFLOWSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.DOUBLE_LITERAL, checker.LITERALFLOWSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.BOOLEAN_LITERAL, checker.LITERALFLOWSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.CHAR_LITERAL, checker.LITERALFLOWSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.STRING_LITERAL, checker.LITERALFLOWSOURCE);

        treeAnnotator.addTreeKind(Tree.Kind.INT_LITERAL, checker.FROMLITERALFLOWSINK);
        treeAnnotator.addTreeKind(Tree.Kind.LONG_LITERAL, checker.FROMLITERALFLOWSINK);
        treeAnnotator.addTreeKind(Tree.Kind.DOUBLE_LITERAL, checker.FROMLITERALFLOWSINK);
        treeAnnotator.addTreeKind(Tree.Kind.BOOLEAN_LITERAL, checker.FROMLITERALFLOWSINK);
        treeAnnotator.addTreeKind(Tree.Kind.CHAR_LITERAL, checker.FROMLITERALFLOWSINK);
        treeAnnotator.addTreeKind(Tree.Kind.STRING_LITERAL, checker.FROMLITERALFLOWSINK);

        postInit();
    }

    @Override
    protected QualifierDefaults createQualifierDefaults() {
        return new FlowCompletingDefaults(elements, this);
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
                // new DefaultApplier(element, DefaultLocation.PARAMETERS, type).scan(type, checker.NOFLOWSOURCES);
                return;

            } else if (this.getDeclAnnotation(iter, ConservativeFlow.class) != null) {
                // Use the top types for return types
                new DefaultApplier(element, DefaultLocation.RETURNS, type).scan(type, checker.ANYFLOWSOURCES);
                // Use the bottom types for parameter types
                new DefaultApplier(element, DefaultLocation.PARAMETERS, type).scan(type, checker.ANYFLOWSINKS);
                // Let @NoFlow override conservative defaults
                new DefaultApplier(element, DefaultLocation.PARAMETERS, type).scan(type, checker.NOFLOWSINKS);
                return;

            }

            if (iter instanceof PackageElement) {
                iter = ElementUtils.parentPackage(this.elements, (PackageElement) iter);
            } else {
                iter = iter.getEnclosingElement();
            }
        }
    }

    protected <T extends Enum<T>> List<T> getValuesOrEmpty( AnnotationMirror am, Class<T> enumClass) {
        if(am == null) {
            return new ArrayList<T>();
        } else {
            final List<T> values = AnnotationUtils.getElementValueEnumArray(am, "value", enumClass, true);
            return values;
        }
    }

    class FlowCompletingDefaults extends QualifierDefaults {

        //Instantiated lazily
        private HashSet<FlowSinks.FlowSink>     sinksFromAny = null;
        private HashSet<FlowSources.FlowSource> sourcesToAny = null;

        public FlowCompletingDefaults(Elements elements, AnnotatedTypeFactory atypeFactory) {
            super(elements, atypeFactory);
        }

        protected void completePolicyFlows(final Element element, final AnnotatedTypeMirror type) {

            if (type == null || type.getKind() == TypeKind.NONE || type.getKind() == TypeKind.NULL ) {
                return;
            }

            if( element != null && element.getKind() == ElementKind.METHOD && type.getKind() == TypeKind.EXECUTABLE ) {
                for ( final AnnotatedTypeMirror atm : ((AnnotatedExecutableType) type).getParameterTypes()) {
                    completePolicyFlows(atm);
                }
                completePolicyFlows(((AnnotatedExecutableType) type).getReturnType());

            } else {
                completePolicyFlows(type);
            }
        }

        protected void completePolicyFlows(final AnnotatedTypeMirror type) {

            final FlowPolicy flowPolicy = checker.getFlowPolicy();
            if(sinksFromAny == null) {
                sinksFromAny = new HashSet<FlowSinks.FlowSink>();
                sinksFromAny.addAll(flowPolicy.getSinksFromSource(FlowSources.FlowSource.ANY, false));

                sourcesToAny = new HashSet<FlowSources.FlowSource>();
                sourcesToAny.addAll(flowPolicy.getSourcesFromSink(FlowSinks.FlowSink.ANY, false));
            }


            final Set<AnnotationMirror> explicitAnnos = type.getExplicitAnnotations();

            AnnotationMirror flowSourcesQual = null;
            AnnotationMirror flowSinksQuals  = null;
            for(final AnnotationMirror am : explicitAnnos) {
                if( flowSourcesQual == null && AnnotationUtils.areSameIgnoringValues(am, checker.FLOW_SOURCES) ) {
                    flowSourcesQual = am;
                } else if( flowSinksQuals == null && AnnotationUtils.areSameIgnoringValues(am, checker.FLOW_SINKS) ) {
                    flowSinksQuals = am;
                }

                if (flowSourcesQual != null && flowSinksQuals != null) {
                    break;
                }
            }

            final List<FlowSources.FlowSource> sources = getValuesOrEmpty(flowSourcesQual, FlowSources.FlowSource.class);
            final List<FlowSinks.FlowSink>     sinks   = getValuesOrEmpty(flowSinksQuals,  FlowSinks.FlowSink.class);


            if( !sources.isEmpty() && flowSinksQuals == null) {
                final Set<FlowSinks.FlowSink>  newSinks = flowPolicy.getIntersectingSinks(sources);
                newSinks.addAll(sinksFromAny);

                if( !newSinks.isEmpty() ) {
                    type.replaceAnnotation( checker.createAnnoFromSinks(newSinks) );
                }

            } else if( flowSourcesQual == null && !sinks.isEmpty() ) {

                final Set<FlowSources.FlowSource> newSources = checker.getFlowPolicy().getIntersectingSources(sinks);
                newSources.addAll(sourcesToAny);

                if( !newSources.isEmpty() ) {
                    type.replaceAnnotation( checker.createAnnoFromSources(newSources) );
                }
            }
        }

        @Override
        public void annotate(Element elt, AnnotatedTypeMirror type) {
            completePolicyFlows(elt, type);
            super.annotate(elt, type);
        }

        @Override
        public void annotate(Tree tree, AnnotatedTypeMirror type) {
            Element element = InternalUtils.symbol(tree);
            completePolicyFlows(element, type);
            super.annotate(tree, type);
        }
    }
}

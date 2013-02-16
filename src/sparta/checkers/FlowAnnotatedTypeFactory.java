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
                new FlowDefaultApplier(element, DefaultLocation.RETURNS, type).scan(type, checker.NOFLOWSOURCES);
                // Nothing needs to be done for parameters.
                // new DefaultApplier(element, DefaultLocation.PARAMETERS, type).scan(type, checker.NOFLOWSOURCES);
                return;

            } else if (this.getDeclAnnotation(iter, ConservativeFlow.class) != null) {
                // Use the top types for return types
                new FlowDefaultApplier(element, DefaultLocation.RETURNS, type).scan(type, checker.ANYFLOWSOURCES);
                // Use the bottom types for parameter types
                new FlowDefaultApplier(element, DefaultLocation.PARAMETERS, type).scan(type, checker.ANYFLOWSINKS);
                // Let @NoFlow override conservative defaults
                new FlowDefaultApplier(element, DefaultLocation.PARAMETERS, type).scan(type, checker.NOFLOWSINKS);
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
                final AnnotatedExecutableType exeType = (AnnotatedExecutableType) type;
                for ( final AnnotatedTypeMirror atm : exeType.getParameterTypes()) {
                    completePolicyFlows(atm);
                }

                completePolicyFlows( exeType.getReturnType(), getExplicitReturnTypeAnnotations(exeType) );

            } else {
                completePolicyFlows(type);
            }
        }

        private Set<AnnotationMirror> getExplicitReturnTypeAnnotations(final AnnotatedExecutableType aet) {
            final Set<AnnotationMirror> methodAnnos = aet.getExplicitAnnotations();
            final Set<AnnotationMirror> returnAnnos = new HashSet<AnnotationMirror>();
            for(final AnnotationMirror methodAnno : methodAnnos) {
                com.sun.tools.javac.code.Attribute.TypeCompound tc = (com.sun.tools.javac.code.Attribute.TypeCompound) methodAnno;

                //.isEmpty means we DO NOT handle array/List type parameters
                if( tc.position.type == com.sun.tools.javac.code.TargetType.METHOD_RETURN && tc.position.location.isEmpty() ) {
                    returnAnnos.add(methodAnno);
                }
            }

            return returnAnnos;
        }

        //TODO: THIS SHOULD REALLY RETURN AN EITHER TYPE
        protected Pair<AnnotationMirror, AnnotationMirror> explicitAnnosToFlowQuals(final Set<AnnotationMirror> explicitAnnos) {
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

            return Pair.of(flowSourcesQual, flowSinksQuals);
        }

        protected Pair<Set<FlowSources.FlowSource>, Set<FlowSinks.FlowSink>> getNewSourcesOrSinks(final Pair<AnnotationMirror, AnnotationMirror> sourceToSinkQuals) {
            final FlowPolicy flowPolicy = checker.getFlowPolicy();

            final List<FlowSources.FlowSource> sources = getValuesOrEmpty(sourceToSinkQuals.first,   FlowSources.FlowSource.class);
            final List<FlowSinks.FlowSink>     sinks   = getValuesOrEmpty(sourceToSinkQuals.second,  FlowSinks.FlowSink.class);

            final Set<FlowSinks.FlowSink>  newSinks;
            final Set<FlowSources.FlowSource> newSources;

            if( !sources.isEmpty() && sourceToSinkQuals.second == null) {
                newSources = null;
                newSinks = flowPolicy.getIntersectingSinks(sources);
                newSinks.addAll(sinksFromAny);

            }  else if( sourceToSinkQuals.first == null && !sinks.isEmpty() ) {
                newSources = checker.getFlowPolicy().getIntersectingSources(sinks);
                newSources.addAll(sourcesToAny);
                newSinks = null;

            } else {
                newSources = null;
                newSinks   = null;
            }

            return Pair.of(newSources, newSinks);
        }

        protected void completePolicyFlows(final AnnotatedTypeMirror type) {
            completePolicyFlows( type, type.getExplicitAnnotations() );
        }

        protected void completePolicyFlows(final AnnotatedTypeMirror type, final Set<AnnotationMirror> explicitAnnos)  {
            if( type.getKind() == TypeKind.VOID || explicitAnnos == null || explicitAnnos.isEmpty() ) {
                return;
            }

            final FlowPolicy flowPolicy = checker.getFlowPolicy();
            if(sinksFromAny == null) {
                sinksFromAny = new HashSet<FlowSinks.FlowSink>();
                sinksFromAny.addAll(flowPolicy.getSinksFromSource(FlowSources.FlowSource.ANY, false));

                sourcesToAny = new HashSet<FlowSources.FlowSource>();
                sourcesToAny.addAll(flowPolicy.getSourcesFromSink(FlowSinks.FlowSink.ANY, false));
            }

            //Using pairs like Either -- the rest of this method would be much shorter with Eithers
            Pair<AnnotationMirror, AnnotationMirror> sourceToSinkQuals =
                    explicitAnnosToFlowQuals(explicitAnnos);

            Pair<Set<FlowSources.FlowSource>, Set<FlowSinks.FlowSink>> newSourcesOrSinks =
                    getNewSourcesOrSinks( sourceToSinkQuals );

            final AnnotationMirror newAnno;
            if( newSourcesOrSinks.first != null) {
                newAnno = checker.createAnnoFromSources( newSourcesOrSinks.first );

            } else if( newSourcesOrSinks.second != null ) {
                newAnno = checker.createAnnoFromSinks( newSourcesOrSinks.second );

            } else {
                newAnno  = null;
            }

            if( newAnno != null ) {
                type.replaceAnnotation( newAnno );
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

    //TODO: This fix should really occur in the actual DefaultApplier
    private static class FlowDefaultApplier extends DefaultApplier {

        public FlowDefaultApplier(Element elt, DefaultLocation location, AnnotatedTypeMirror type) {
            super(elt, location, type);
        }

        @Override
        public Void scan(AnnotatedTypeMirror t, AnnotationMirror qual) {
            if (t == null || t.getKind() == TypeKind.NONE)
                return null;

            if( t.getUnderlyingType() instanceof javax.lang.model.type.NoType) {
                return null;
            }

            return super.scan(t, qual);
        }
    }
}

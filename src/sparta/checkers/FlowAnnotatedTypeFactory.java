package sparta.checkers;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

import checkers.types.AnnotatedTypeFactory;
import checkers.util.*;
import com.sun.tools.javac.code.TypeAnnotationPosition;
import sparta.checkers.quals.ConservativeFlow;
import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.Sources;
import sparta.checkers.quals.DefaultFlow;
import sparta.checkers.quals.PolyFlow;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;

import checkers.quals.DefaultLocation;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.BasicAnnotatedTypeFactory;
import checkers.util.QualifierDefaults.DefaultApplier;

import java.util.*;
import  sparta.checkers.quals.SpartaPermission;

import static checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;



public class FlowAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<FlowChecker> {

    public FlowAnnotatedTypeFactory(FlowChecker checker, CompilationUnitTree root) {
        super(checker, root);

        // Use the bottom type as default for everything but local variables.
        defaults.addAbsoluteDefault(checker.LITERALFLOWSOURCE, DefaultLocation.OTHERWISE);
        // Use the top type for local variables and let flow refine the type.
        defaults.addAbsoluteDefault(checker.ANYFLOWSOURCES, DefaultLocation.LOCALS);

        // Default is LITERAL -> (ALL MAPPED SINKS) for everything but local variables.
        defaults.addAbsoluteDefault(checker.FROMLITERALFLOWSINK, DefaultLocation.OTHERWISE);
        // Use the top type for local variables and let flow refine the type.
        defaults.addAbsoluteDefault(checker.NOFLOWSINKS, DefaultLocation.LOCALS);
        // But let's send null down any sink and give it no sources.
        treeAnnotator.addTreeKind(Tree.Kind.NULL_LITERAL, checker.ANYFLOWSINKS);
        treeAnnotator.addTreeKind(Tree.Kind.NULL_LITERAL, checker.NOFLOWSOURCES);

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
            if (this.getDeclAnnotation(iter, DefaultFlow.class) != null) {
                // Use LITERAL->?  the return type.
                new FlowDefaultApplier(element, DefaultLocation.OTHERWISE, type).scan(type, checker.LITERALFLOWSOURCE);
                new FlowDefaultApplier(element, DefaultLocation.OTHERWISE, type).scan(type, checker.FROMLITERALFLOWSINK);
               


                return;

            } else if (this.getDeclAnnotation(iter, ConservativeFlow.class) != null) {
                // Use the top types for return types
                new FlowDefaultApplier(element, DefaultLocation.RETURNS, type).scan(type, checker.ANYFLOWSOURCES);
                // Use the bottom types for parameter types
                new FlowDefaultApplier(element, DefaultLocation.PARAMETERS, type).scan(type, checker.ANYFLOWSINKS);
                // Let @DefaultFlow override conservative defaults
                new FlowDefaultApplier(element, DefaultLocation.PARAMETERS, type).scan(type, checker.NOFLOWSINKS);
                return;

            } else if (this.getDeclAnnotation(iter, PolyFlow.class) != null) {
                // Use poly flow sources and sinks for return types .
                new FlowDefaultApplier(element, DefaultLocation.RETURNS, type).scan(type, checker.POLYFLOWSOURCES);
                new FlowDefaultApplier(element, DefaultLocation.RETURNS, type).scan(type, checker.POLYFLOWSINKS);
                
             // Use poly flow sources and sinks for Parameter types (This is excluding receivers) 
                new FlowDefaultApplier(element, DefaultLocation.PARAMETERS, type).scan(type, checker.POLYFLOWSINKS);
                new FlowDefaultApplier(element, DefaultLocation.PARAMETERS, type).scan(type, checker.POLYFLOWSOURCES);

                return;

            }

            if (iter instanceof PackageElement) {
                iter = ElementUtils.parentPackage(this.elements, (PackageElement) iter);
            } else {
                iter = iter.getEnclosingElement();
            }
        }
    }

    class FlowCompletingDefaults extends QualifierDefaults {

        //Instantiated lazily
        private HashSet<SpartaPermission>     sinksFromAny = null;
        private HashSet<SpartaPermission> sourcesToAny = null;

        public FlowCompletingDefaults(Elements elements, AnnotatedTypeFactory atypeFactory) {
            super(elements, atypeFactory);
        }

        protected void completePolicyFlows(final Element element, final AnnotatedTypeMirror type) {

            if (type == null || type.getKind() == TypeKind.NONE || type.getKind() == TypeKind.NULL ) {
                return;
            }

            boolean isLocal = (element == null || element.getKind() == ElementKind.LOCAL_VARIABLE );


            if( !isLocal /*&& element != null*/ &&
                (element.getKind() == ElementKind.METHOD || element.getKind() == ElementKind.CONSTRUCTOR) &&
                type.getKind() == TypeKind.EXECUTABLE ) {
                final AnnotatedExecutableType exeType = (AnnotatedExecutableType) type;
                for ( final AnnotatedTypeMirror atm : exeType.getParameterTypes()) {
                    completePolicyFlows(false, atm);
                }

                completePolicyFlows( false, exeType.getReturnType()   );
                completePolicyFlows(false,  exeType.getReceiverType() );

            } else {
                completePolicyFlows(isLocal, type);
            }
        }

        private void completePolicyFlows(final boolean isLocal, final AnnotatedTypeMirror type) {
            if( type.getKind() == TypeKind.ARRAY )  {
                final Pair<Set<AnnotationMirror>, Set<AnnotationMirror>> arrayToComponentAnnos =
                        getArrayAnnos((AnnotatedTypeMirror.AnnotatedArrayType) type, isLocal);

                completePolicyFlows(type, arrayToComponentAnnos.first);
                completePolicyFlows(((AnnotatedTypeMirror.AnnotatedArrayType) type).getComponentType(), arrayToComponentAnnos.second);
            } else {
                //Note this only works because TreeAnnotator does not add any defaults besides literals
                completePolicyFlows(type, (isLocal) ? type.getExplicitAnnotations() : type.getAnnotations());
            }
        }

        private Pair<Set<AnnotationMirror>, Set<AnnotationMirror>> getArrayAnnos(final AnnotatedTypeMirror.AnnotatedArrayType aat, boolean isLocal) {
            if(!isLocal) {
                return Pair.of(aat.getAnnotations(), aat.getComponentType().getAnnotations());
            } //else

            final Set<AnnotationMirror> arrayAnnos = aat.getExplicitAnnotations();
            final Set<AnnotationMirror> objectAnnos    = new HashSet<AnnotationMirror>();
            final Set<AnnotationMirror> componentAnnos = new HashSet<AnnotationMirror>();

            for(final AnnotationMirror arrayAnno : arrayAnnos) {
                com.sun.tools.javac.code.Attribute.TypeCompound tc = (com.sun.tools.javac.code.Attribute.TypeCompound) arrayAnno;

                if( !tc.position.location.isEmpty() && tc.position.location.get(0) == TypeAnnotationPosition.TypePathEntry.ARRAY ) {
                    componentAnnos.add(arrayAnno);
                }  else {
                    objectAnnos.add(arrayAnno);
                }
            }

            return Pair.of(objectAnnos, componentAnnos);
        }

        //TODO: THIS SHOULD REALLY RETURN AN EITHER TYPE
        protected Pair<AnnotationMirror, AnnotationMirror> explicitAnnosToFlowQuals(final Set<AnnotationMirror> explicitAnnos) {
            AnnotationMirror flowSourcesQual = null;
            AnnotationMirror flowSinksQuals  = null;
            for(final AnnotationMirror am : explicitAnnos) {
                if( flowSourcesQual == null && AnnotationUtils.areSameIgnoringValues(am, checker.FLOW_SOURCES) ) {
                    flowSourcesQual = am;
                }else if( flowSourcesQual == null && AnnotationUtils.areSameIgnoringValues(am, checker.POLYFLOWSOURCES) ) {
                    flowSourcesQual = am;
                }else if( flowSinksQuals == null && AnnotationUtils.areSameIgnoringValues(am, checker.FLOW_SINKS) ) {
                    flowSinksQuals = am;
                }else if( flowSinksQuals == null && AnnotationUtils.areSameIgnoringValues(am, checker.POLYFLOWSINKS) ) {
                    flowSinksQuals = am;
                }

                if (flowSourcesQual != null && flowSinksQuals != null) {
                    break;
                }
            }

            return Pair.of(flowSourcesQual, flowSinksQuals);
        }

        protected Pair<Set<SpartaPermission>, Set<SpartaPermission>> getNewSourcesOrSinks(final Pair<AnnotationMirror, AnnotationMirror> sourceToSinkQuals) {
            final FlowPolicy flowPolicy = checker.getFlowPolicy();
            if(AnnotationUtils.areSameIgnoringValues(sourceToSinkQuals.first, checker.POLYFLOWSOURCES)  ||
        	    AnnotationUtils.areSameIgnoringValues(sourceToSinkQuals.second, checker.POLYFLOWSINKS) ) {
        	return Pair.of(null, null);
            }
            final Set<SpartaPermission> sources = FlowUtil.getSourcesOrEmpty(sourceToSinkQuals.first, false);
            final Set<SpartaPermission>     sinks   = FlowUtil.getSinksOrEmpty(sourceToSinkQuals.second,  false);

            final Set<SpartaPermission>  newSinks;
            final Set<SpartaPermission> newSources;

            if( !sources.isEmpty() && sourceToSinkQuals.second == null) {
                newSources = null;
                newSinks = flowPolicy.getIntersectingSinks(sources);
                newSinks.addAll(sinksFromAny);
		if (newSinks.contains(SpartaPermission.ANY) && newSinks.size() > 1) {
		    System.out.println("Drop extra sinks");
		    newSinks.clear();
		    newSinks.add(SpartaPermission.ANY);
		}

            }  else if( sourceToSinkQuals.first == null && !sinks.isEmpty() ) {
                newSources = checker.getFlowPolicy().getIntersectingSources(sinks);
                newSources.addAll(sourcesToAny);
                newSinks = null;
		if (newSources.contains(SpartaPermission.ANY)
			&& newSources.size() > 1) {
		    System.out.println("Drop extra sources");
		    newSources.clear();
		    newSources.add(SpartaPermission.ANY);
		}

            } else {
                newSources = null;
                newSinks   = null;
            }

            return Pair.of(newSources, newSinks);
        }

        protected void completePolicyFlows(final AnnotatedTypeMirror type, final Set<AnnotationMirror> explicitAnnos)  {
            if( type.getKind() == TypeKind.VOID || explicitAnnos == null || explicitAnnos.isEmpty() ) {
                return;
            }

            final FlowPolicy flowPolicy = checker.getFlowPolicy();
            if(sinksFromAny == null) {
                sinksFromAny = new HashSet<SpartaPermission>();
                sinksFromAny.addAll(flowPolicy.getSinksFromSource(SpartaPermission.ANY, false));

                sourcesToAny = new HashSet<SpartaPermission>();
                sourcesToAny.addAll(flowPolicy.getSourcesFromSink(SpartaPermission.ANY, false));
            }

            //Using pairs like Either -- the rest of this method would be much shorter with Eithers
            Pair<AnnotationMirror, AnnotationMirror> sourceToSinkQuals =
                    explicitAnnosToFlowQuals(explicitAnnos);

            Pair<Set<SpartaPermission>, Set<SpartaPermission>> newSourcesOrSinks =
                    getNewSourcesOrSinks( sourceToSinkQuals );

            final AnnotationMirror newAnno;
            if( newSourcesOrSinks.first != null) {
                newAnno = FlowUtil.createAnnoFromSources(processingEnv, newSourcesOrSinks.first );

            } else if( newSourcesOrSinks.second != null ) {
                newAnno = FlowUtil.createAnnoFromSinks(processingEnv, newSourcesOrSinks.second );

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

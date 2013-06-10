package sparta.checkers;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

import checkers.types.AnnotatedTypeFactory;
import checkers.util.*;

import com.sun.tools.javac.code.TypeAnnotationPosition;

import sparta.checkers.quals.PolyFlowReceiver;
import sparta.checkers.quals.PolyFlow;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;

import checkers.quals.DefaultLocation;
import checkers.quals.FromByteCode;
import checkers.quals.FromStubFile;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.BasicAnnotatedTypeFactory;
import checkers.util.QualifierDefaults.DefaultApplier;

import java.util.*;
import  sparta.checkers.quals.FlowPermission;

import static checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;



public class FlowAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<FlowChecker> {

    private final Map<String, Map<String, List<Element>>> notInStubFile; //List of methods that are not in a stub file 
    
    public FlowAnnotatedTypeFactory(FlowChecker checker, CompilationUnitTree root) {
        super(checker, root);
        this.notInStubFile = checker.notInStubFile;

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

        // Literals, other than null are different too
        // There are no Byte or Short literal types in java (0b is treated as an int), 
        //   so there does not need to be a mapping for them here.
        treeAnnotator.addTreeKind(Tree.Kind.INT_LITERAL, checker.LITERALFLOWSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.LONG_LITERAL, checker.LITERALFLOWSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.FLOAT_LITERAL, checker.LITERALFLOWSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.DOUBLE_LITERAL, checker.LITERALFLOWSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.BOOLEAN_LITERAL, checker.LITERALFLOWSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.CHAR_LITERAL, checker.LITERALFLOWSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.STRING_LITERAL, checker.LITERALFLOWSOURCE);

        treeAnnotator.addTreeKind(Tree.Kind.INT_LITERAL, checker.FROMLITERALFLOWSINK);
        treeAnnotator.addTreeKind(Tree.Kind.LONG_LITERAL, checker.FROMLITERALFLOWSINK);
        treeAnnotator.addTreeKind(Tree.Kind.FLOAT_LITERAL, checker.FROMLITERALFLOWSINK);
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
        boolean reviewed = false;
        while (iter != null) {
            // If a method is from a stub file, it is considered reviewed.
            reviewed = this.isFromStubFile(iter);

            if (this.isFromByteCode(iter)) {
            	//Only apply these annotations if this method has not been marked as not reviewed. 
				if (!reviewed) {
                    notAnnotated(element);
//					//All types are @Source(NOT_REVIEWED) @Sink(NOT_REVIEWED)
                    //TODO:instead of not reviewed we could issue a new error
                    //Something like Error: ByteCode method, method, has not been reviewed
					new FlowDefaultApplier(element, DefaultLocation.OTHERWISE,type).scan(type, checker.NRSINK);
					new FlowDefaultApplier(element, DefaultLocation.OTHERWISE,type).scan(type, checker.NRSOURCE);
				}
				
				return;
				
            } else if (this.getDeclAnnotation(iter, PolyFlow.class) != null) {
                // Use poly flow sources and sinks for return types .
                new FlowDefaultApplier(element, DefaultLocation.RETURNS, type).scan(type, checker.POLYFLOWSOURCES);
                new FlowDefaultApplier(element, DefaultLocation.RETURNS, type).scan(type, checker.POLYFLOWSINKS);
                
                // Use poly flow sources and sinks for Parameter types (This is excluding receivers) 
                new FlowDefaultApplier(element, DefaultLocation.PARAMETERS, type).scan(type, checker.POLYFLOWSINKS);
                new FlowDefaultApplier(element, DefaultLocation.PARAMETERS, type).scan(type, checker.POLYFLOWSOURCES);

                return;

            } else if (this.getDeclAnnotation(iter, PolyFlowReceiver.class) != null) {
                // Use poly flow sources and sinks for return types .
                new FlowDefaultApplier(element, DefaultLocation.RETURNS, type).scan(type, checker.POLYFLOWSOURCES);
                new FlowDefaultApplier(element, DefaultLocation.RETURNS, type).scan(type, checker.POLYFLOWSINKS);
                
                // Use poly flow sources and sinks for Parameter types (This is excluding receivers) 
                new FlowDefaultApplier(element, DefaultLocation.PARAMETERS, type).scan(type, checker.POLYFLOWSINKS);
                new FlowDefaultApplier(element, DefaultLocation.PARAMETERS, type).scan(type, checker.POLYFLOWSOURCES);

                // Use poly flow sources and sinks for receiver types 
                new FlowDefaultApplier(element, DefaultLocation.RECEIVERS, type).scan(type, checker.POLYFLOWSINKS);
                new FlowDefaultApplier(element, DefaultLocation.RECEIVERS, type).scan(type, checker.POLYFLOWSOURCES);
                
                return;

            }

            if (iter instanceof PackageElement) {
                iter = ElementUtils.parentPackage(this.elements, (PackageElement) iter);
            } else {
                iter = iter.getEnclosingElement();
            }
        }
    }
    /**
     * Adds the element to list of methods that need to be added 
     * to the stub file and reviewed
     * @param element element that needs to be reviewed
     */
    private void notAnnotated(final Element element) {
        if(!(element.getEnclosingElement() instanceof TypeElement)) return;
    
        TypeElement clssEle = (TypeElement) element.getEnclosingElement();
        String fullClassName = clssEle.getQualifiedName().toString();
        String pkg = "";
        String clss = "";
        if (fullClassName.indexOf('.') != -1) {
            int index = fullClassName.lastIndexOf('.');
             pkg = fullClassName.substring(0, index);
             clss = fullClassName.substring(index+1);
        }
        Map<String, List<Element>> pkgmap = this.notInStubFile.get(pkg);
        if(pkgmap == null){
            pkgmap = new HashMap<String, List<Element>>();
            List<Element> elelist = new ArrayList<>();
            pkgmap.put(clss, elelist );
            this.notInStubFile.put(pkg, pkgmap);
        }
        List<Element> elelist = pkgmap.get(clss);
        if(elelist == null){
            elelist = new ArrayList<Element>();
            pkgmap.put(clss, elelist );
        }
        
        if(!elelist.contains(element)){
            elelist.add(element);
        }
    }


    class FlowCompletingDefaults extends QualifierDefaults {

        //Instantiated lazily
        private HashSet<FlowPermission>     sinksFromAny = null;
        private HashSet<FlowPermission> sourcesToAny = null;

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
            AnnotationMirror flowSourceQual = null;
            AnnotationMirror flowSinkQuals  = null;
            for(final AnnotationMirror am : explicitAnnos) {
                if( flowSourceQual == null && AnnotationUtils.areSameIgnoringValues(am, checker.FLOW_SOURCES) ) {
                    flowSourceQual = am;
                }else if( flowSourceQual == null && AnnotationUtils.areSameIgnoringValues(am, checker.POLYFLOWSOURCES) ) {
                    flowSourceQual = am;
                }else if( flowSinkQuals == null && AnnotationUtils.areSameIgnoringValues(am, checker.FLOW_SINKS) ) {
                    flowSinkQuals = am;
                }else if( flowSinkQuals == null && AnnotationUtils.areSameIgnoringValues(am, checker.POLYFLOWSINKS) ) {
                    flowSinkQuals = am;
                }

                if (flowSourceQual != null && flowSinkQuals != null) {
                    break;
                }
            }

            return Pair.of(flowSourceQual, flowSinkQuals);
        }

        protected Pair<Set<FlowPermission>, Set<FlowPermission>> getNewSourceOrSink(final Pair<AnnotationMirror, AnnotationMirror> sourceToSinkQuals) {
            final FlowPolicy flowPolicy = checker.getFlowPolicy();
            if(AnnotationUtils.areSameIgnoringValues(sourceToSinkQuals.first, checker.POLYFLOWSOURCES)  ||
        	    AnnotationUtils.areSameIgnoringValues(sourceToSinkQuals.second, checker.POLYFLOWSINKS) ) {
        	return Pair.of(null, null);
            }
            final Set<FlowPermission> sources = FlowUtil.getSourceOrEmpty(sourceToSinkQuals.first, false);
            final Set<FlowPermission>     sinks   = FlowUtil.getSinkOrEmpty(sourceToSinkQuals.second,  false);

            final Set<FlowPermission>  newSink;
            final Set<FlowPermission> newSource;

            if( !sources.isEmpty() && sourceToSinkQuals.second == null) {
                newSource = null;
                newSink = flowPolicy.getIntersectingSink(sources);
                newSink.addAll(sinksFromAny);
		if (newSink.contains(FlowPermission.ANY) && newSink.size() > 1) {
		    System.out.println("Drop extra sinks");
		    newSink.clear();
		    newSink.add(FlowPermission.ANY);
		}

            }  else if( sourceToSinkQuals.first == null && !sinks.isEmpty() ) {
                newSource = checker.getFlowPolicy().getIntersectingSource(sinks);
                newSource.addAll(sourcesToAny);
                newSink = null;
		if (newSource.contains(FlowPermission.ANY)
			&& newSource.size() > 1) {
		    System.out.println("Drop extra sources");
		    newSource.clear();
		    newSource.add(FlowPermission.ANY);
		}

            } else {
                newSource = null;
                newSink   = null;
            }

            return Pair.of(newSource, newSink);
        }

        protected void completePolicyFlows(final AnnotatedTypeMirror type, final Set<AnnotationMirror> explicitAnnos)  {
            if( type.getKind() == TypeKind.VOID || explicitAnnos == null || explicitAnnos.isEmpty() ) {
                return;
            }

            final FlowPolicy flowPolicy = checker.getFlowPolicy();
            if(sinksFromAny == null) {
                sinksFromAny = new HashSet<FlowPermission>();
                sinksFromAny.addAll(flowPolicy.getSinkFromSource(FlowPermission.ANY, false));

                sourcesToAny = new HashSet<FlowPermission>();
                sourcesToAny.addAll(flowPolicy.getSourceFromSink(FlowPermission.ANY, false));
            }

            //Using pairs like Either -- the rest of this method would be much shorter with Eithers
            Pair<AnnotationMirror, AnnotationMirror> sourceToSinkQuals =
                    explicitAnnosToFlowQuals(explicitAnnos);

            Pair<Set<FlowPermission>, Set<FlowPermission>> newSourceOrSink =
                    getNewSourceOrSink( sourceToSinkQuals );

            final AnnotationMirror newAnno;
            if( newSourceOrSink.first != null) {
                newAnno = FlowUtil.createAnnoFromSource(processingEnv, newSourceOrSink.first );

            } else if( newSourceOrSink.second != null ) {
                newAnno = FlowUtil.createAnnoFromSink(processingEnv, newSourceOrSink.second );

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

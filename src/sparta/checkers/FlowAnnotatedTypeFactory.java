package sparta.checkers;

import static checkers.quals.DefaultLocation.LOCAL_VARIABLE;
import static checkers.quals.DefaultLocation.OTHERWISE;
import static checkers.quals.DefaultLocation.RECEIVERS;
import static checkers.quals.DefaultLocation.RESOURCE_VARIABLE;
import static checkers.quals.DefaultLocation.UPPER_BOUNDS;

import checkers.quals.DefaultLocation;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedArrayType;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.types.AnnotatedTypeMirror.AnnotatedIntersectionType;
import checkers.types.AnnotatedTypeMirror.AnnotatedPrimitiveType;
import checkers.types.AnnotatedTypeMirror.AnnotatedTypeVariable;
import checkers.types.AnnotatedTypeMirror.AnnotatedUnionType;
import checkers.types.AnnotatedTypeMirror.AnnotatedWildcardType;
import checkers.types.SubtypingAnnotatedTypeFactory;
import checkers.types.TypeAnnotator;
import checkers.util.QualifierDefaults.DefaultApplierElement;

import javacutils.AnnotationUtils;
import javacutils.ElementUtils;
import javacutils.InternalUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.PolyFlow;
import sparta.checkers.quals.PolyFlowReceiver;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;

public class FlowAnnotatedTypeFactory extends SubtypingAnnotatedTypeFactory<FlowChecker> {


    // List of methods that are not in a stub file
    private final Map<String, Map<String, Map<Element, Integer>>> notInStubFile;
    public FlowAnnotatedTypeFactory(FlowChecker checker, CompilationUnitTree root) {
        super(checker, root);

  
        
        // Use the top type for local variables and let flow refine the type.
        //Upper bounds should be top too.
        DefaultLocation[] topLocations = {LOCAL_VARIABLE,RESOURCE_VARIABLE, UPPER_BOUNDS}; 

        defaults.addAbsoluteDefaults(checker.ANYSOURCE, topLocations);
        defaults.addAbsoluteDefaults(checker.NOSINK, topLocations);

        // Default is LITERAL -> (ALL MAPPED SINKS) for everything else
        defaults.addAbsoluteDefault(checker.FROMLITERALSINK, OTHERWISE);
        defaults.addAbsoluteDefault(checker.LITERALSOURCE, OTHERWISE);


        // But let's send null down any sink and give it no sources.
        treeAnnotator.addTreeKind(Tree.Kind.NULL_LITERAL, checker.ANYSINK);
        treeAnnotator.addTreeKind(Tree.Kind.NULL_LITERAL, checker.NOSOURCE);

        // Literals, other than null are different too
        // There are no Byte or Short literal types in java (0b is treated as an
        // int),
        // so there does not need to be a mapping for them here.
        treeAnnotator.addTreeKind(Tree.Kind.INT_LITERAL, checker.LITERALSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.LONG_LITERAL, checker.LITERALSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.FLOAT_LITERAL, checker.LITERALSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.DOUBLE_LITERAL, checker.LITERALSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.BOOLEAN_LITERAL, checker.LITERALSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.CHAR_LITERAL, checker.LITERALSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.STRING_LITERAL, checker.LITERALSOURCE);

        treeAnnotator.addTreeKind(Tree.Kind.INT_LITERAL, checker.FROMLITERALSINK);
        treeAnnotator.addTreeKind(Tree.Kind.LONG_LITERAL, checker.FROMLITERALSINK);
        treeAnnotator.addTreeKind(Tree.Kind.FLOAT_LITERAL, checker.FROMLITERALSINK);
        treeAnnotator.addTreeKind(Tree.Kind.DOUBLE_LITERAL, checker.FROMLITERALSINK);
        treeAnnotator.addTreeKind(Tree.Kind.BOOLEAN_LITERAL, checker.FROMLITERALSINK);
        treeAnnotator.addTreeKind(Tree.Kind.CHAR_LITERAL, checker.FROMLITERALSINK);
        treeAnnotator.addTreeKind(Tree.Kind.STRING_LITERAL, checker.FROMLITERALSINK);

        this.notInStubFile = checker.notInStubFile;

        postInit();

    }


    @Override
    protected void annotateImplicit(Tree tree, AnnotatedTypeMirror type, boolean useFlow) {
        Element element = InternalUtils.symbol(tree);
        handleDefaulting(element, type);
        super.annotateImplicit(tree, type, useFlow);
    }

    @Override
    public void annotateImplicit(Element element, AnnotatedTypeMirror type) {
        handleDefaulting(element, type);
        super.annotateImplicit(element, type);
    }

    protected void handleDefaulting(final Element element, final AnnotatedTypeMirror type) {
        Element iter = element;
        boolean reviewed = false;

        DefaultApplierElement applier = new DefaultApplierElement(this, element, type);

        while (iter != null) {
            // If a method is from a stub file, it is considered reviewed.
            reviewed = this.isFromStubFile(iter);

            if (this.isFromByteCode(iter)) {
                if (reviewed) {
                    //Receivers are TOP in stubfiles so that API methods can 
                    //invoked on any objects, by default
                    applier.apply(checker.ANYSOURCE, RECEIVERS);
                    applier.apply(checker.NOSINK, RECEIVERS);
                } else {
                    notAnnotated(element);
                    // Checking if ignoring NOT_REVIEWED warnings
                    if (!checker.IGNORENR) {
                        // TODO:instead of not reviewed we could issue a new
                        // error
                        // Something like Error: ByteCode method, method, has
                        // not been reviewed
                        applier.apply(checker.NR_SINK, DefaultLocation.OTHERWISE);
                        applier.apply(checker.NR_SOURCE, DefaultLocation.OTHERWISE);
                    }
                }

            } else if (this.getDeclAnnotation(iter, PolyFlow.class) != null) {
                // Use poly flow sources and sinks for return types .
                applier.apply(checker.POLYSOURCE, DefaultLocation.RETURNS);
                applier.apply(checker.POLYSINK, DefaultLocation.RETURNS);

                // Use poly flow sources and sinks for Parameter types (This is
                // excluding receivers)
                applier.apply(checker.POLYSINK, DefaultLocation.PARAMETERS);
                applier.apply(checker.POLYSOURCE, DefaultLocation.PARAMETERS);

                return;

            } else if (this.getDeclAnnotation(iter, PolyFlowReceiver.class) != null) {
                // Use poly flow sources and sinks for return types .
                applier.apply(checker.POLYSOURCE, DefaultLocation.RETURNS);
                applier.apply(checker.POLYSINK, DefaultLocation.RETURNS);

                // Use poly flow sources and sinks for Parameter types (This is
                // excluding receivers)
                applier.apply(checker.POLYSINK, DefaultLocation.PARAMETERS);
                applier.apply(checker.POLYSOURCE, DefaultLocation.PARAMETERS);

                // Use poly flow sources and sinks for receiver types
                applier.apply(checker.POLYSINK, DefaultLocation.RECEIVERS);
                applier.apply(checker.POLYSOURCE, DefaultLocation.RECEIVERS);

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
     * Adds the element to list of methods that need to be added to the stub
     * file and reviewed
     *
     * @param element
     *            element that needs to be reviewed
     */
    private void notAnnotated(final Element element) {

        if (!(element.getEnclosingElement() instanceof TypeElement))
            return;

        TypeElement clssEle = (TypeElement) element.getEnclosingElement();
        String fullClassName = clssEle.getQualifiedName().toString();
        String pkg = "";
        String clss = "";
        if (fullClassName.indexOf('.') != -1) {
            int index = fullClassName.lastIndexOf('.');
            pkg = fullClassName.substring(0, index);
            clss = fullClassName.substring(index + 1);
        }
        Map<String, Map<Element, Integer>> classmap = this.notInStubFile.get(pkg);
        if (classmap == null) {
            classmap = new HashMap<>();
            Map<Element, Integer> elelist = new HashMap<Element, Integer>();
            classmap.put(clss, elelist);
            this.notInStubFile.put(pkg, classmap);
        }
        Map<Element, Integer> elementmap = classmap.get(clss);
        if (elementmap == null) {
            elementmap = new HashMap<Element, Integer>();
            classmap.put(clss, elementmap);
        }

        if (elementmap.containsKey(element)) {
            Integer i = elementmap.get(element);
            i++;
            elementmap.put(element, i);

        } else {
            elementmap.put(element, 1);
        }
    }


        
    @Override
    protected TypeAnnotator createTypeAnnotator(FlowChecker checker) {
        return new FlowCompletionAnnotator(checker, this);
    }

    /**
     * If a type only contains a flow source or flow sink, the other qualifier is
     * filled in with the most general possible information that is consistent
     * with the policy file.
     * @author smillst
     *
     */
    class FlowCompletionAnnotator extends TypeAnnotator {

        FlowChecker checker;
        public FlowCompletionAnnotator(FlowChecker checker,FlowAnnotatedTypeFactory factory) {
            super(checker, factory);
            this.checker = checker;
        }
        @Override
        public Void visitArray(AnnotatedArrayType type, Element p) {
            completePolicyFlows(type);
            return super.visitArray(type, p);
        }
        @Override
        public Void visitDeclared(AnnotatedDeclaredType type, Element p) {
            completePolicyFlows(type);
            return super.visitDeclared(type, p);
        }
        @Override
        public Void visitExecutable(AnnotatedExecutableType t, Element elem) {
            completePolicyFlows(t);
            return super.visitExecutable(t, elem);
        }
        @Override
        public Void visitIntersection(AnnotatedIntersectionType type, Element p) {
            completePolicyFlows(type);
            return super.visitIntersection(type, p);
        }

        @Override
        public Void visitPrimitive(AnnotatedPrimitiveType type, Element p) {
            completePolicyFlows(type);
            return super.visitPrimitive(type, p);
        }
        @Override
        public Void visitTypeVariable(AnnotatedTypeVariable type, Element p) {
            //Calling type.getEffectiveAnnotations() expands 
            //the upper bounds causing an infinite loop for types like
            // E extends Enum<E>
            //So visit call super, visit the extends 
            //and then complete the policy flow
            Void r = super.visitTypeVariable(type, p);
            completePolicyFlows(type);
            return r;
        }
        @Override
        public Void visitUnion(AnnotatedUnionType type, Element p) {
            completePolicyFlows(type);
            return super.visitUnion(type, p);
        }
        @Override
        public Void visitWildcard(AnnotatedWildcardType type, Element p) {
            //Calling type.getEffectiveAnnotations() expands 
            //the upper bounds causing an infinite loop for types like
            // ? extends Enum<?>
            //So visit call super, visit the extends 
            //and then complete the policy flow
            Void r =  super.visitWildcard(type, p);
            completePolicyFlows(type);
            return r;
        }


        /**
         * Only complete flows if one of the annotations is missing. (Do not 
         * complete flows if the source or sinks is {}.  Defaulting or the user 
         * may add @Source({}) and @Sink({}).)
         * 
         * Also don't complete if this is a void type
         * @param type
         * @return
         */
        private boolean shouldNotComplete(AnnotatedTypeMirror type) {
            if (type.getKind() == TypeKind.VOID) return true;
            boolean hasSource = false;
            boolean hasSink = false;
           for(AnnotationMirror anno : type.getEffectiveAnnotations()){
               if(AnnotationUtils.areSameByClass(anno, Source.class)){
                   hasSource = true;
               }else if (AnnotationUtils.areSameByClass(anno, Sink.class)){
                   hasSink = true;
               }else if (AnnotationUtils.areSameByClass(anno, PolySource.class)){
                   hasSource = true;
               }else if (AnnotationUtils.areSameByClass(anno, PolySink.class)){
                   hasSink = true;
               }
           }
            return (hasSink == hasSource);
        }

        protected void completePolicyFlows(final AnnotatedTypeMirror type) {
            if(shouldNotComplete(type)){
                return ;
            }
            
            final Set<FlowPermission> sources = Flow.getSources(type);
            final Set<FlowPermission> sinks = Flow.getSinks(type);

            AnnotationMirror newAnno;
            if (!sources.isEmpty()) {
                Set<FlowPermission> newSink = checker.getFlowPolicy().getIntersectionAllowedSinks(sources);
                newAnno=  checker.createAnnoFromSink(newSink);
                type.replaceAnnotation(newAnno);
            } else if (!sinks.isEmpty()) {
                Set<FlowPermission> newSource = checker.getFlowPolicy().getIntersectionAllowedSources(sinks);                
                newAnno=  checker.createAnnoFromSource(newSource);
                type.replaceAnnotation(newAnno);
            } 

        }

    }
  

}


package sparta.checkers;
import static org.checkerframework.framework.qual.DefaultLocation.EXCEPTION_PARAMETER;
import static org.checkerframework.framework.qual.DefaultLocation.FIELD;
import static org.checkerframework.framework.qual.DefaultLocation.LOCAL_VARIABLE;
import static org.checkerframework.framework.qual.DefaultLocation.LOWER_BOUNDS;
import static org.checkerframework.framework.qual.DefaultLocation.OTHERWISE;
import static org.checkerframework.framework.qual.DefaultLocation.PARAMETERS;
import static org.checkerframework.framework.qual.DefaultLocation.RECEIVERS;
import static org.checkerframework.framework.qual.DefaultLocation.RESOURCE_VARIABLE;
import static org.checkerframework.framework.qual.DefaultLocation.RETURNS;
import static org.checkerframework.framework.qual.DefaultLocation.UPPER_BOUNDS;

import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.InternalUtils;
import org.checkerframework.javacutil.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeKind;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.qual.DefaultLocation;
import org.checkerframework.framework.qual.PolyAll;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedIntersectionType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedPrimitiveType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedTypeVariable;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedUnionType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedWildcardType;
import org.checkerframework.framework.type.AnnotatedTypeFormatter;
import org.checkerframework.framework.type.DefaultAnnotatedTypeFormatter;
import org.checkerframework.framework.type.DefaultRawnessComparer;
import org.checkerframework.framework.type.DefaultTypeHierarchy;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.StructuralEqualityComparer;
import org.checkerframework.framework.type.TypeHierarchy;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.framework.util.AnnotationFormatter;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.framework.util.QualifierPolymorphism;
import org.checkerframework.framework.util.defaults.QualifierDefaults;

import sparta.checkers.poly.ParameterizedPermissonPolymorphism;
import sparta.checkers.poly.ReceiverPolymorphism;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.PFPermission;
import sparta.checkers.quals.PolyFlow;
import sparta.checkers.quals.PolyFlowReceiver;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySinkR;
import sparta.checkers.quals.PolySource;
import sparta.checkers.quals.PolySourceR;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;

public class FlowAnnotatedTypeFactory extends BaseAnnotatedTypeFactory{

    protected final AnnotationMirror NOSOURCE, ANYSOURCE, POLYSOURCE, POLYSOURCER;
    protected final AnnotationMirror NOSINK, ANYSINK, POLYSINK,POLYSINKR;
    protected final AnnotationMirror POLYALL;

    protected final AnnotationMirror SOURCE;
    protected final AnnotationMirror SINK;

    protected final FlowPolicy flowPolicy;

    // FlowVisitor uses these to hold flow state
    protected final FlowAnalyzer flowAnalizer;
    
    private final PFPermission ANY;
    
    protected ReceiverPolymorphism polyReceiver;
    private ParameterizedPermissonPolymorphism polyParameterPerm;

    
    //Qualifier defaults for byte code and poly flow defaulting
	final QualifierDefaults byteCodeFieldDefault = new QualifierDefaults(elements, this);
	final QualifierDefaults byteCodeDefaults = new QualifierDefaults(elements, this);
	final QualifierDefaults polyFlowDefaults = new QualifierDefaults(elements, this);
	final QualifierDefaults polyFlowReceiverDefaults = new QualifierDefaults(elements, this);
	
    public FlowAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);

        ANY = new PFPermission(FlowPermission.ANY);
        
        NOSOURCE = createAnnoFromSource(Collections.<PFPermission> emptySet());
        NOSINK = createAnnoFromSink(Collections.<PFPermission> emptySet());

        POLYSOURCE = AnnotationUtils.fromClass(elements, PolySource.class);
        POLYSINK = AnnotationUtils.fromClass(elements, PolySink.class);
        POLYSOURCER = AnnotationUtils.fromClass(elements, PolySourceR.class);
        POLYSINKR = AnnotationUtils.fromClass(elements, PolySinkR.class);
        POLYALL = AnnotationUtils.fromClass(elements, PolyAll.class);

        ANYSOURCE = createAnnoFromSource(ANY);
        ANYSINK = createAnnoFromSink( ANY);
        
        SOURCE = AnnotationUtils.fromClass(elements, Source.class);
        SINK = AnnotationUtils.fromClass(elements, Sink.class);

        // Must call super.initChecker before the lint option can be checked.
        final String pfArg = checker.getOption(FlowPolicy.POLICY_FILE_OPTION);
        if (pfArg == null || pfArg.trim().isEmpty()) {
            flowPolicy = new FlowPolicy(new File("flow-policy"), processingEnv);
        } else {
            flowPolicy = new FlowPolicy(new File(pfArg), processingEnv);
        }

        flowAnalizer = new FlowAnalyzer(getFlowPolicy());

     // Every subclass must call postInit!
        if (this.getClass().equals(FlowAnnotatedTypeFactory.class)) {
            this.postInit();
        }
    }

    @Override
    protected void postInit() {
        super.postInit();
        // Has to be called after postInit
        // has been called for every subclass.
        initQualifierDefaults();

        //Uncomment this line to check stub files
        //see StubChecker for more details.
        //StubChecker.checkStubs(indexDeclAnnos, indexTypes, checker, this, processingEnv);
        
        polyReceiver = new ReceiverPolymorphism(processingEnv, this);
        polyParameterPerm = new ParameterizedPermissonPolymorphism(processingEnv, this);
    }

    private AnnotationMirror createAnnoFromSink(
            PFPermission sinks) {
        return createAnnoFromSink(Collections.singleton(sinks));
    }

    private AnnotationMirror createAnnoFromSource(
            PFPermission source) {
        return createAnnoFromSource(Collections.singleton(source));
    }

    @Override
    protected AnnotatedTypeFormatter createAnnotatedTypeFormatter() {
        return new DefaultAnnotatedTypeFormatter(createAnnotationFormatter(),
                checker.hasOption("printVerboseGenerics"), checker.hasOption("printAllQualifiers"));
    }
    @Override
    protected AnnotationFormatter createAnnotationFormatter() {
        if (checker.hasOption(FlowChecker.PRETTY_PRINT_OPTION)) {
            return new FlowAnnotationFormatter();
        }
        return super.createAnnotationFormatter();
    }

    @Override
    public CFTransfer createFlowTransferFunction(
            CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {

        CFTransfer ret = new CFTransfer(analysis) {
            /**
             * This method overrides super so that variables are not refined in
             * conditionals see test case in flow/Conditions.java
             */
            @Override
            protected TransferResult<CFValue, CFStore> strengthenAnnotationOfEqualTo(
                    TransferResult<CFValue, CFStore> res, Node firstNode,
                    Node secondNode, CFValue firstValue, CFValue secondValue,
                    boolean notEqualTo) {
                return res;
            }
        };
        return ret;
    }


    /**
     * Initializes qualifier defaults for
     * 
     * @PolyFlow, @PolyFlowReceiver, and @FromByteCode
     */
    private void initQualifierDefaults() {
        // Final fields from byte code are {} -> ANY
        byteCodeFieldDefault.addCheckedCodeDefault(NOSOURCE, OTHERWISE);
        byteCodeFieldDefault.addCheckedCodeDefault(ANYSINK, OTHERWISE);

        // All locations besides non-final fields in byte code are
        // conservatively ANY -> ANY
        byteCodeDefaults.addCheckedCodeDefault(ANYSOURCE,
                                                      DefaultLocation.OTHERWISE);
        byteCodeDefaults.addCheckedCodeDefault(ANYSINK,
                                                      DefaultLocation.OTHERWISE);

        // Use poly flow sources and sinks for return types and
        // parameter types (This is excluding receivers).
        DefaultLocation[] polyFlowLoc = { DefaultLocation.RETURNS,
                DefaultLocation.PARAMETERS };
        polyFlowDefaults.addCheckedCodeDefaults(POLYSOURCE, polyFlowLoc);
        polyFlowDefaults.addCheckedCodeDefaults(POLYSINK, polyFlowLoc);

        // Use poly flow sources and sinks for return types and
        // parameter types and receivers).
        DefaultLocation[] polyFlowReceiverLoc = { DefaultLocation.RETURNS,
                DefaultLocation.PARAMETERS, DefaultLocation.RECEIVERS };
        polyFlowReceiverDefaults.addCheckedCodeDefaults(POLYSOURCER,
                                                               polyFlowReceiverLoc);
        polyFlowReceiverDefaults.addCheckedCodeDefaults(POLYSINKR,
                                                               polyFlowReceiverLoc);
    }

    @Override
    protected QualifierDefaults createQualifierDefaults() {
        QualifierDefaults defaults =  super.createQualifierDefaults();
        //CLIMB-to-the-top defaults
        DefaultLocation[] topLocations = { LOCAL_VARIABLE, RESOURCE_VARIABLE, UPPER_BOUNDS };
        defaults.addCheckedCodeDefaults(ANYSOURCE, topLocations);
        defaults.addCheckedCodeDefaults(NOSINK, topLocations);
        

        // Default for receivers is top
        DefaultLocation[] conditionalSinkLocs = { RECEIVERS, PARAMETERS,
                EXCEPTION_PARAMETER };
        defaults.addCheckedCodeDefaults(ANYSOURCE, conditionalSinkLocs);
        defaults.addCheckedCodeDefaults(NOSINK, conditionalSinkLocs);

        // Default for returns and fields is {}->ANY (bottom)
        DefaultLocation[] bottomLocs = { RETURNS, FIELD };
        defaults.addCheckedCodeDefaults(NOSOURCE, bottomLocs);
        defaults.addCheckedCodeDefaults(ANYSINK, bottomLocs);

        // Default is {} -> ANY for everything else
        defaults.addCheckedCodeDefault(ANYSINK, OTHERWISE);
        defaults.addCheckedCodeDefault(NOSOURCE, OTHERWISE);
        
        //Default for lower bounds is bottom
        defaults.addCheckedCodeDefault(NOSOURCE, LOWER_BOUNDS);
        defaults.addCheckedCodeDefault(ANYSINK, LOWER_BOUNDS);


        return defaults;
    }

    public AnnotationMirror createAnnoFromSink(final Set<PFPermission> sinks) {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
                Sink.class);
        return createIFlowAnnotation(sinks, builder);
    }

    public AnnotationMirror createAnnoFromSource(Set<PFPermission> sources) {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
                Source.class);
        return createIFlowAnnotation(sources, builder);
    }

    private AnnotationMirror createIFlowAnnotation(
            final Set<PFPermission> permObjects, final AnnotationBuilder builder) {
        List<String> permStrings = new ArrayList<>();
        for (PFPermission p : permObjects) {
            permStrings.add(p.toString());
        }
        builder.setValue("value", permStrings);
        return builder.build();
    }


    @Override
    protected void annotateImplicit(Tree tree, AnnotatedTypeMirror type,
            boolean useFlow) {
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
        if (element == null)
            return;
        handlePolyFlow(element, type);
        
        if (isFromByteCode(element)
                && element.getKind() == ElementKind.FIELD
                && ElementUtils.isEffectivelyFinal(element)) {
            byteCodeFieldDefault.annotate(element, type);
            return;
        }
            
        if (isFromByteCode(element)){
            byteCodeDefaults.annotate(element, type);
        } 
    }

    private void handlePolyFlow(Element element, AnnotatedTypeMirror type) {
        Element iter = element;

        while (iter != null) {
            if (this.getDeclAnnotation(iter, PolyFlow.class) != null) {
                if (element.getKind() == ElementKind.METHOD) {
                    ExecutableElement method = (ExecutableElement) element;
                    if (method.getReturnType().getKind() == TypeKind.VOID) {
                        return;
                    }
                }
                polyFlowDefaults.annotate(element, type);
                addAnnotationsToComponentTypes(type, POLYSOURCE, POLYSINK);
                return;
            } else if (this.getDeclAnnotation(iter, PolyFlowReceiver.class) != null) {
                if (ElementUtils.hasReceiver(element)) {
                    polyFlowReceiverDefaults.annotate(element, type);
                    addAnnotationsToComponentTypes(type, POLYSOURCER, POLYSINKR);
                } else {
                    polyFlowDefaults.annotate(element, type);
                    addAnnotationsToComponentTypes(type, POLYSOURCE, POLYSINK);
                }
                return;
            }

            if (iter instanceof PackageElement) {
                iter = ElementUtils.parentPackage(this.elements,
                        (PackageElement) iter);
            } else {
                iter = iter.getEnclosingElement();
            }
        }
    }

    private void addAnnotationsToComponentTypes(AnnotatedTypeMirror type,
            AnnotationMirror polySource, AnnotationMirror polySink) {
        if (type instanceof AnnotatedExecutableType) {
            for (AnnotatedTypeMirror param : ((AnnotatedExecutableType) type)
                    .getParameterTypes()) {
                addAnnotationsToComponentType(polySource, polySink, param);
            }
            addAnnotationsToComponentType(polySource, polySink,
                    ((AnnotatedExecutableType) type).getReturnType());
        }
    }

    /**
     * Adds the passed annotations to the inner most component type if atm is an
     * AnnotatedArrayType. (Otherwise, no effect)
     * 
     * @param source
     * @param sink
     * @param atm
     */
    private void addAnnotationsToComponentType(AnnotationMirror source,
            AnnotationMirror sink, AnnotatedTypeMirror atm) {
        if(atm == null) return;
        // Find the innermost component type
        AnnotatedTypeMirror innerCommponentType = null;
        while (innerCommponentType instanceof AnnotatedArrayType) {
            innerCommponentType = ((AnnotatedArrayType) atm).getComponentType();
        }
        if (innerCommponentType != null) {
            // Added annotation on the inner most component type
            if (atm.getAnnotationInHierarchy(NOSOURCE) == null)
                atm.addAnnotation(source);
            if (atm.getAnnotationInHierarchy(ANYSINK) == null)
                atm.addAnnotation(sink);
        }
    }

    @Override
    protected ListTreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                new FlowPolicyTreeAnnotator(this),
                new PropagationTreeAnnotator(this),
                getFlowCheckerImplicits()
        );
    }

    protected final ImplicitsTreeAnnotator getFlowCheckerImplicits() {
        ImplicitsTreeAnnotator implicits = new ImplicitsTreeAnnotator(this);
        
        //All literals are bottom
        implicits.addTreeKind(Tree.Kind.INT_LITERAL, NOSOURCE);
        implicits.addTreeKind(Tree.Kind.LONG_LITERAL, NOSOURCE);
        implicits.addTreeKind(Tree.Kind.FLOAT_LITERAL, NOSOURCE);
        implicits.addTreeKind(Tree.Kind.DOUBLE_LITERAL, NOSOURCE);
        implicits.addTreeKind(Tree.Kind.BOOLEAN_LITERAL, NOSOURCE);
        implicits.addTreeKind(Tree.Kind.CHAR_LITERAL, NOSOURCE);
        implicits.addTreeKind(Tree.Kind.STRING_LITERAL, NOSOURCE);
        implicits.addTreeKind(Tree.Kind.NULL_LITERAL, NOSOURCE);

        implicits.addTreeKind(Tree.Kind.INT_LITERAL, ANYSINK);
        implicits.addTreeKind(Tree.Kind.LONG_LITERAL, ANYSINK);
        implicits.addTreeKind(Tree.Kind.FLOAT_LITERAL, ANYSINK);
        implicits.addTreeKind(Tree.Kind.DOUBLE_LITERAL, ANYSINK);
        implicits.addTreeKind(Tree.Kind.BOOLEAN_LITERAL, ANYSINK);
        implicits.addTreeKind(Tree.Kind.CHAR_LITERAL, ANYSINK);
        implicits.addTreeKind(Tree.Kind.STRING_LITERAL, ANYSINK);
        implicits.addTreeKind(Tree.Kind.NULL_LITERAL, ANYSINK);
        
        return implicits;
    }
/**
 * Corrects default annotation on new class trees and flow completes type casts.
 * All other types are flow completed by FlowPolicyTypeAnnotator
 * @author smillst
 *
 */
    protected class FlowPolicyTreeAnnotator extends TreeAnnotator {

        public FlowPolicyTreeAnnotator(FlowAnnotatedTypeFactory atypeFactory) {
            super(atypeFactory);
        }

        @Override
        public Void visitNewClass(NewClassTree node, AnnotatedTypeMirror p) {
            //This is a horrible hack around the bad implementation of constructor results
            //(CF treats annotations on constructor results in stub files as if it were a 
            //default and therefore ignores it.) 
            AnnotatedTypeMirror defaulted = atypeFactory.constructorFromUse(node).first.getReturnType();
            Set<AnnotationMirror> defaultedSet = defaulted.getAnnotations();
            //The default of OTHERWISE locations such as constructor results
            //is {}{}, but for constructor results we really want bottom.
            //So if the result is {}{}, then change it to {}->ANY (bottom)
            if(Flow.getSources(defaulted).isEmpty() && Flow.getSinks(defaulted).isEmpty()){
                defaulted.replaceAnnotation(ANYSINK);
                defaultedSet = defaulted.getAnnotations();
            }
            
            p.replaceAnnotations(defaultedSet);
            return null;
        }
        @Override
        public Void visitTypeCast(TypeCastTree node, AnnotatedTypeMirror p) {
            // Fill in the missing source or sink before the propagation tree annotator
            // copies the missing annotation from the expressions.
            completePolicyFlows(p);
            return super.visitTypeCast(node, p);
        }
   }

    @Override
    protected TypeAnnotator createTypeAnnotator() {
        return new ListTypeAnnotator(
               super.createTypeAnnotator(),
                new FlowPolicyTypeAnnotator(this)
        );
    }

    /**
     * If a type only contains a flow source or flow sink, the other qualifier is
     * filled in with the most general possible information that is consistent
     * with the policy file.
     * @author smillst
     *
     */
   protected class FlowPolicyTypeAnnotator extends TypeAnnotator {

        // FlowChecker checker;
        public FlowPolicyTypeAnnotator(FlowAnnotatedTypeFactory factory) {
            super(factory);
        }
        @Override
        public Void visitArray(AnnotatedArrayType type, Void p) {
            completePolicyFlows(type);
            return super.visitArray(type, p);
        }
        @Override
        public Void visitDeclared(AnnotatedDeclaredType type, Void p) {
            completePolicyFlows(type);
            return super.visitDeclared(type, p);
        }
        @Override
        public Void visitExecutable(AnnotatedExecutableType t, Void elem) {
            completePolicyFlows(t);

            //Don't call super because it skips the receiver type for some reason
            scan(t.getReturnType(), elem);
            scanAndReduce(t.getReceiverType(), elem, null);
            scanAndReduce(t.getParameterTypes(), elem, null);
            scanAndReduce(t.getThrownTypes(), elem, null);
            scanAndReduce(t.getTypeVariables(), elem, null);
            return null;
        }
        @Override
        public Void visitIntersection(AnnotatedIntersectionType type, Void p) {
            completePolicyFlows(type);
            return super.visitIntersection(type, p);
        }

        @Override
        public Void visitPrimitive(AnnotatedPrimitiveType type, Void p) {
            completePolicyFlows(type);
            return super.visitPrimitive(type, p);
        }
        @Override
        public Void visitTypeVariable(AnnotatedTypeVariable type, Void p) {
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
        public Void visitUnion(AnnotatedUnionType type, Void p) {
            completePolicyFlows(type);
            return super.visitUnion(type, p);
        }
        @Override
        public Void visitWildcard(AnnotatedWildcardType type, Void p) {
            //Calling type.getEffectiveAnnotations() expands
            //the upper bounds causing an infinite loop for types like
            // ? extends Enum<?>
            //So visit call super, visit the extends
            //and then complete the policy flow
            Void r =  super.visitWildcard(type, p);
            completePolicyFlows(type);
            return r;
        }
    }

public static long time = 0;

    protected void completePolicyFlows(final AnnotatedTypeMirror type) {
        Set<PFPermission> sources = null;
        Set<PFPermission> sinks = null;
        if (shouldNotComplete(type.getAnnotations())) {
            return;
        }
        for (AnnotationMirror anno : type.getAnnotations()) {
            if (AnnotationUtils.areSameByClass(anno, Source.class)) {
                sources = Flow.getSources(anno);
                break;
            } else if (AnnotationUtils.areSameByClass(anno, Sink.class)) {
                sinks = Flow.getSinks(anno);
                break;
            }
        }

        AnnotationMirror newAnno;
        if (sources != null) {
            Set<PFPermission> newSink = getFlowPolicy()
                    .getIntersectionAllowedSinks(sources);
            newAnno = createAnnoFromSink(newSink);
            type.replaceAnnotation(newAnno);
        } else if (sinks != null) {
            Set<PFPermission> newSource = getFlowPolicy()
                    .getIntersectionAllowedSources(sinks);
            newAnno = createAnnoFromSource(newSource);
            type.replaceAnnotation(newAnno);
        }
    }

    /**
     * Only complete flows if one of the annotations is missing. (Do not
     * complete flows if the source or sinks is {}.  Defaulting or the user
     * may add @Source({}) and @Sink({}).)
     *
     * Also don't complete if this is a void type
     * @param set
     * @return
     */
    private static boolean shouldNotComplete(Set<AnnotationMirror> set) {
      //  if (type.getKind() == TypeKind.VOID) return true;
        boolean hasSource = false;
        boolean hasSink = false;
       for(AnnotationMirror anno : set){
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


    @Override
    public Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> methodFromUse(
            MethodInvocationTree tree) {
        Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> mfuPair = super
                .methodFromUse(tree);
        AnnotatedExecutableType method = mfuPair.first;
        polyReceiver.annotate(tree, method);
        polyParameterPerm.annotate(tree, method);
        return mfuPair;
    }

    @Override
    public Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> constructorFromUse(
            NewClassTree tree) {
        Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> mfuPair = super
                .constructorFromUse(tree);
        AnnotatedExecutableType method = mfuPair.first;
        polyReceiver.annotate(tree, method);
        polyParameterPerm.annotate(tree, method);
        return mfuPair;
    }

    @Override
    protected MultiGraphQualifierHierarchy.MultiGraphFactory createQualifierHierarchyFactory() {
        return new MultiGraphQualifierHierarchy.MultiGraphFactory(this);
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new FlowQualifierHierarchy(factory);
    }

    /**
     * TODO: The code below involving the FlowTypeHierarchy should be changed
     * once there is a hook to specify equality between qualifiers.
     * 
     * @Source(PERM) = @FineSource(PERM,{}) and @Sink(PERM) = @FineSink(PERM,{})
     *               when the refined version has no parameters and both have
     *               the same permission.
     */
    @Override
    protected TypeHierarchy createTypeHierarchy() {
        return new FlowTypeHierarchy(checker, getQualifierHierarchy(),
                                     checker.hasOption("ignoreRawTypeArguments"),
                                     checker.hasOption("invariantArrays"));
    }


    class NormalizingStructuralEqualityComparer extends StructuralEqualityComparer {

        public NormalizingStructuralEqualityComparer(
                DefaultRawnessComparer rawnessComparer) {
            super(rawnessComparer);
        }

        @Override
        protected boolean arePrimeAnnosEqual(AnnotatedTypeMirror subtype, AnnotatedTypeMirror supertype) {
            Flow subtypeFlow = new Flow(Flow.getSources(subtype.getAnnotation(Source.class)), 
                    Flow.getSinks(subtype.getAnnotation(Sink.class)));
            Flow supertypeFlow = new Flow(Flow.getSources(supertype.getAnnotation(Source.class)), 
                    Flow.getSinks(supertype.getAnnotation(Sink.class)));
            return subtypeFlow.equals(supertypeFlow);
        }
    }

    protected class FlowTypeHierarchy extends DefaultTypeHierarchy {

        public FlowTypeHierarchy(BaseTypeChecker checker,
                QualifierHierarchy qualifierHierarchy, boolean ignoreRawTypes, boolean invariantArrays) {
            super(checker, qualifierHierarchy, ignoreRawTypes, invariantArrays);
        }

        @Override
        public StructuralEqualityComparer createEqualityComparer() {
            return new NormalizingStructuralEqualityComparer(rawnessComparer);
        }
    }


    protected class FlowQualifierHierarchy extends MultiGraphQualifierHierarchy {

        protected FlowQualifierHierarchy(MultiGraphFactory f) {
            super(f);
        }

        @Override
        protected Set<AnnotationMirror> findBottoms(
                Map<AnnotationMirror, Set<AnnotationMirror>> supertypes) {
            Set<AnnotationMirror> newbottoms = AnnotationUtils.createAnnotationSet();
            newbottoms.add(NOSOURCE);
            newbottoms.add(ANYSINK);
            return newbottoms;
        }

        @Override
        protected Set<AnnotationMirror> findTops(
                Map<AnnotationMirror, Set<AnnotationMirror>> supertypes) {
            Set<AnnotationMirror> newtops = AnnotationUtils.createAnnotationSet();
            newtops.add(ANYSOURCE);
            newtops.add(NOSINK);
            return newtops;
        }


        private boolean isSourceQualifier(AnnotationMirror anno) {
            return AnnotationUtils.areSameByClass(anno, Source.class)
                    || isPolySourceQualifier(anno);
        }

        private boolean isPolySourceQualifier(AnnotationMirror anno) {
            return AnnotationUtils.areSameByClass(anno, PolySource.class)
                    || AnnotationUtils.areSameByClass(anno, PolyAll.class)
                    || AnnotationUtils.areSameByClass(anno, PolySourceR.class);
        }

        private boolean isSinkQualifier(AnnotationMirror anno) {
            return isPolySinkQualifier(anno) || AnnotationUtils.areSameByClass(anno, Sink.class);
        }

        private boolean isPolySinkQualifier(AnnotationMirror anno) {
            return AnnotationUtils.areSameByClass(anno, PolySink.class)
                    || AnnotationUtils.areSameByClass(anno, PolyAll.class)
                    || AnnotationUtils.areSameByClass(anno, PolySinkR.class);
        }

        @Override
        public AnnotationMirror getTopAnnotation(AnnotationMirror start) {
            if (isSourceQualifier(start)) {
                return ANYSOURCE;
            } else if (isSinkQualifier(start)) {
                return NOSINK;
            } else if (QualifierPolymorphism.isPolyAll(start)) {
                return POLYALL;
            } else {
                checker.errorAbort("FlowChecker: unexpected AnnotationMirror: " + start);
                return null; // dead code
            }
        }

        @Override
        public boolean isSubtype(AnnotationMirror subtype, AnnotationMirror supertype){
            if (isPolySourceQualifier(supertype) && isPolySourceQualifier(subtype)) {
                return true;
            } else if (isPolySourceQualifier(supertype) && isSourceQualifier(subtype)) {
                // If super is poly, only bottom is a subtype
                return Flow.getSources(subtype).isEmpty();
            } else if (isSourceQualifier(supertype) && isPolySourceQualifier(subtype)) {
                // if sub is poly, only top is a supertype
                return Flow.getSources(supertype).contains(ANY);
            } else if (isSourceQualifier(supertype) && isSourceQualifier(subtype)) {
                // Check the set
                Set<PFPermission> superset = Flow.getSources(supertype);
                Set<PFPermission> subset = Flow.getSources(subtype);
                return isSuperSet(superset, subset);
            } else if (isPolySinkQualifier(supertype) && isPolySinkQualifier(subtype)) {
                return true;
            } else if (isPolySinkQualifier(supertype) && isSinkQualifier(subtype)) {
                // If super is poly, only bottom is a subtype
                return Flow.getSinks(subtype).contains(ANY);
            } else if (isSinkQualifier(supertype) && isPolySinkQualifier(subtype)) {
                // if sub is poly, only top is a supertype
                return Flow.getSinks(supertype).isEmpty();
            } else if (isSinkQualifier(supertype) && isSinkQualifier(subtype)) {
                // Check the set (sinks are backward)
                Set<PFPermission> subset = Flow.getSinks(supertype);
                Set<PFPermission> superset = Flow.getSinks(subtype);
                return isSuperSet(superset, subset);
            } else {
                // annotations should either both be sources or sinks.
                return false;
            }
        }
        
        private boolean isSuperSet(Set<PFPermission> superset, Set<PFPermission> subset) {
            if (superset.containsAll(subset) || superset.contains(ANY)) {
                return true;
            }
            for (PFPermission flow : subset) {
                if (!isMatchInSet(flow, superset)) {
                    return false;
                }
            }
            return true;
        }

        
        @Override
        protected void addPolyRelations(QualifierHierarchy qualHierarchy,
                Map<AnnotationMirror, Set<AnnotationMirror>> fullMap,
                Map<AnnotationMirror, AnnotationMirror> polyQualifiers, Set<AnnotationMirror> tops,
                Set<AnnotationMirror> bottoms) {
            AnnotationUtils.updateMappingToImmutableSet(fullMap, NOSOURCE,
                    Collections.singleton(POLYALL));
            AnnotationUtils.updateMappingToImmutableSet(fullMap, NOSOURCE,
                    Collections.singleton(POLYSOURCE));
            AnnotationUtils.updateMappingToImmutableSet(fullMap, ANYSINK,
                    Collections.singleton(POLYALL));
            AnnotationUtils.updateMappingToImmutableSet(fullMap, ANYSINK,
                    Collections.singleton(POLYSINK));
            Set<AnnotationMirror> polyallTops = AnnotationUtils.createAnnotationSet();
            polyallTops.add(ANYSOURCE);
            polyallTops.add(NOSINK);
            AnnotationUtils.updateMappingToImmutableSet(fullMap, POLYALL, polyallTops);
            AnnotationUtils.updateMappingToImmutableSet(fullMap, POLYSOURCE,
                    Collections.singleton(ANYSOURCE));
            AnnotationUtils.updateMappingToImmutableSet(fullMap, POLYSINK,
                    Collections.singleton(NOSINK));
        }

        @Override
        public AnnotationMirror leastUpperBound(AnnotationMirror a1, AnnotationMirror a2) {

            if (isSubtype(a1, a2))
                return a2;
            if (isSubtype(a2, a1))
                return a1;

            if (AnnotationUtils.areSameIgnoringValues(a1, a2)) {
                if (AnnotationUtils.areSameIgnoringValues(a1, SOURCE)) {
                    final Set<PFPermission> superset = Flow.unionSources(a1, a2);
                    return boundSource(superset);

                } else if (AnnotationUtils.areSameIgnoringValues(a1, SINK)) {
                    final Set<PFPermission> intersection = Flow.intersectSinks(a1, a2);
                    return boundSink(intersection);
                }
                // Poly Flows must be handled as if they are Top Type
            } else if (AnnotationUtils.areSame(a1, POLYSINK)) {
                if (AnnotationUtils.areSameIgnoringValues(a2, SINK)) {
                    return boundSink(new TreeSet<PFPermission>());
                }

            } else if (AnnotationUtils.areSame(a2, POLYSINK)) {
                if (AnnotationUtils.areSameIgnoringValues(a1, SINK)) {
                    return boundSink(new TreeSet<PFPermission>());
                }
            } else if (AnnotationUtils.areSame(a1, POLYSOURCE)) {
                if (AnnotationUtils.areSameIgnoringValues(a2, SOURCE)) {
                    Set<PFPermission> top = new TreeSet<PFPermission>();
                    top.add(ANY);
                    return boundSource(top);
                }

            } else if (AnnotationUtils.areSame(a2, POLYSOURCE)) {
                if (AnnotationUtils.areSameIgnoringValues(a1, SOURCE)) {
                    Set<PFPermission> top = new TreeSet<PFPermission>();
                    top.add(ANY);
                    return boundSource(top);
                }
            }

            return super.leastUpperBound(a1, a2);
        }

        @Override
        public AnnotationMirror greatestLowerBound(AnnotationMirror a1, AnnotationMirror a2) {

            if (AnnotationUtils.areSame(a1, a2))
                return a1;

            if (AnnotationUtils.areSameIgnoringValues(a1, a2)) {
                if (AnnotationUtils.areSameIgnoringValues(a1, SOURCE)) {
                    final Set<PFPermission> intersection = Flow.intersectSources(a1, a2);
                    return boundSource(intersection);

                } else if (AnnotationUtils.areSameIgnoringValues(a1, SINK)) {
                    final Set<PFPermission> superSet = Flow.unionSinks(a1, a2);
                    return boundSink(superSet);

                }
                // Poly Flows must be handled as if they are Bottom Type
            } else if (AnnotationUtils.areSame(a1, POLYSINK)) {
                if (AnnotationUtils.areSameIgnoringValues(a2, SINK)) {
                    Set<PFPermission> bottom = new TreeSet<PFPermission>();
                    bottom.add(ANY);
                    return boundSink(bottom);
                }

            } else if (AnnotationUtils.areSame(a2, POLYSINK)) {
                if (AnnotationUtils.areSameIgnoringValues(a1, SINK)) {
                    Set<PFPermission> bottom = new TreeSet<PFPermission>();
                    bottom.add(ANY);
                    return boundSink(bottom);
                }
            } else if (AnnotationUtils.areSame(a1, POLYSOURCE)) {
                if (AnnotationUtils.areSameIgnoringValues(a2, SOURCE)) {
                    return boundSource(new TreeSet<PFPermission>());
                }

            } else if (AnnotationUtils.areSame(a2, POLYSOURCE)) {
                if (AnnotationUtils.areSameIgnoringValues(a1, SOURCE)) {
                    return boundSource(new TreeSet<PFPermission>());
                }
            }
            return super.greatestLowerBound(a1, a2);
        }

        private AnnotationMirror boundSource(final Set<PFPermission> flowSource) {

            final AnnotationMirror am;
            if (PFPermission.coarsePermissionExists(ANY, flowSource)) { // contains all
                                                           // Source
                am = getTopAnnotation(SOURCE);
            } else if (flowSource.isEmpty()) {
                am = getBottomAnnotation(SOURCE);
            } else {
                am = createAnnoFromSource(flowSource);
            }
            return am;
        }

        private AnnotationMirror boundSink(final Set<PFPermission> flowSink) {
            final AnnotationMirror am;
            if (flowSink.isEmpty()) {
                am = getTopAnnotation(SINK);
            } else if (PFPermission.coarsePermissionExists(ANY, flowSink)) { // contains all
                                                                // Sink
                am = getBottomAnnotation(SINK);
            } else {
                am = createAnnoFromSink(flowSink);
            }
            return am;
        }
    }

    public FlowPolicy getFlowPolicy() {
        return flowPolicy;
    }

    public FlowAnalyzer getFlowAnalizer() {
        return flowAnalizer;
    }
    
    public static boolean isMatchInSet(PFPermission flowToMatch, Set<PFPermission> flows) {
        for (PFPermission flow : flows) {
            if (flowToMatch.getPermission() == flow.getPermission()) {
                if (allParametersMatch(flowToMatch.getParameters(), flow.getParameters())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean allParametersMatch(List<String> childParams, List<String> parentParams) {
        for (String currChildParam : childParams) {
            if (!singleParametersMatch(currChildParam, parentParams)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean singleParametersMatch(String param, List<String> parameters) {
        for (String currParam : parameters) {
            if (wildcardMatch(param, currParam)) {
                return true;
            }
        }
        return false;
    }

    public static boolean wildcardMatch(String child, String parent) {
        String regex = parent.replaceAll("\\*", "(.*)");
        return child.matches(regex);
    }

    public Map<AnnotationMirror, AnnotationMirror> getPolyReceiverQuals() {
        Map<AnnotationMirror, AnnotationMirror> map = new HashMap<AnnotationMirror, AnnotationMirror>();
        map.put(ANYSOURCE,POLYSOURCER);
        map.put(NOSINK,POLYSINKR);

        return map;
    }
}

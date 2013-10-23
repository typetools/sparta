package sparta.checkers;

import static checkers.quals.DefaultLocation.LOCAL_VARIABLE;
import static checkers.quals.DefaultLocation.OTHERWISE;
import static checkers.quals.DefaultLocation.RECEIVERS;
import static checkers.quals.DefaultLocation.RESOURCE_VARIABLE;
import static checkers.quals.DefaultLocation.UPPER_BOUNDS;

import checkers.basetype.BaseAnnotatedTypeFactory;
import checkers.basetype.BaseTypeChecker;
import checkers.quals.DefaultLocation;
import checkers.quals.PolyAll;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedArrayType;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.types.AnnotatedTypeMirror.AnnotatedIntersectionType;
import checkers.types.AnnotatedTypeMirror.AnnotatedPrimitiveType;
import checkers.types.AnnotatedTypeMirror.AnnotatedTypeVariable;
import checkers.types.AnnotatedTypeMirror.AnnotatedUnionType;
import checkers.types.AnnotatedTypeMirror.AnnotatedWildcardType;
import checkers.types.QualifierHierarchy;
import checkers.types.TreeAnnotator;
import checkers.types.TypeAnnotator;
import checkers.util.AnnotationBuilder;
import checkers.util.MultiGraphQualifierHierarchy;
import checkers.util.QualifierDefaults;
import checkers.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import checkers.util.QualifierDefaults.DefaultApplierElement;
import checkers.util.QualifierPolymorphism;

import javacutils.AnnotationUtils;
import javacutils.ElementUtils;
import javacutils.InternalUtils;
import javacutils.TreeUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
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

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;

public class FlowAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    protected final AnnotationMirror NOSOURCE, ANYSOURCE, POLYSOURCE;
    protected final AnnotationMirror NOSINK, ANYSINK, POLYSINK;
    protected final AnnotationMirror POLYALL;
    protected final AnnotationMirror LITERALSOURCE,  FROMLITERALSINK;
    protected final AnnotationMirror NR_SOURCE, NR_SINK;
    protected final AnnotationMirror CONDITIONALSINK, FROMCONDITIONALSOURCE;

    protected final AnnotationMirror SOURCE;
    protected final AnnotationMirror SINK;

    protected final FlowPolicy flowPolicy;

    // FlowVisitor uses these to hold flow state
    protected final FlowAnalyzer flowAnalizer;

    // List of methods that are not in a stub file
    private final Map<String, Map<String, Map<Element, Integer>>> notInStubFile;

    public final boolean IGNORENR;

    public FlowAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);

        NOSOURCE = AnnotationUtils.fromClass(elements, Source.class);
        NOSINK = AnnotationUtils.fromClass(elements, Sink.class);

        POLYSOURCE = AnnotationUtils.fromClass(elements, PolySource.class);
        POLYSINK = AnnotationUtils.fromClass(elements, PolySink.class);
        POLYALL = AnnotationUtils.fromClass(elements, PolyAll.class);

        NR_SOURCE = createAnnoFromSource( new TreeSet<FlowPermission>(Arrays.asList(FlowPermission.NOT_REVIEWED)));
        NR_SINK = createAnnoFromSink( new TreeSet<FlowPermission>(Arrays.asList(FlowPermission.NOT_REVIEWED)));

        ANYSOURCE = createAnnoFromSource( new TreeSet<FlowPermission>(Arrays.asList(FlowPermission.ANY)));
        ANYSINK = createAnnoFromSink(  new TreeSet<FlowPermission>(Arrays.asList(FlowPermission.ANY)));

        SOURCE = AnnotationUtils.fromClass(elements, Source.class);
        SINK = AnnotationUtils.fromClass(elements, Sink.class);

        sourceValue = TreeUtils
                .getMethod("sparta.checkers.quals.Source", "value", 0, processingEnv);
        sinkValue = TreeUtils.getMethod("sparta.checkers.quals.Sink", "value", 0, processingEnv);

        // Must call super.initChecker before the lint option can be checked.
        final boolean scArg = checker.getLintOption(FlowPolicy.STRICT_CONDITIONALS_OPTION, false);
        final String pfArg = checker.getOption(FlowPolicy.POLICY_FILE_OPTION);
        if (pfArg == null || pfArg.trim().isEmpty()) {
            flowPolicy = new FlowPolicy(scArg);
        } else {
            flowPolicy = new FlowPolicy(new File(pfArg), scArg);
        }

        final String ignoreArg = checker.getOption(FlowChecker.IGNORE_NOT_REVIEWED);
        IGNORENR = (ignoreArg != null && ignoreArg.trim().equals("on"));

        LITERALSOURCE = createAnnoFromSource(new TreeSet<FlowPermission>(
                Arrays.asList(FlowPermission.LITERAL)));

        final Set<FlowPermission> literalSink = new TreeSet<FlowPermission>(
                flowPolicy.getSinkFromSource(FlowPermission.LITERAL, true));
        FROMLITERALSINK = createAnnoFromSink(literalSink);

        CONDITIONALSINK = createAnnoFromSink(new TreeSet<FlowPermission>(
                Arrays.asList(FlowPermission.CONDITIONAL)));

        final Set<FlowPermission> condtionalSource = new TreeSet<FlowPermission>(
                flowPolicy.getSourceFromSink(FlowPermission.CONDITIONAL, true));
        FROMCONDITIONALSOURCE = createAnnoFromSource(condtionalSource);

        flowAnalizer = new FlowAnalyzer(getFlowPolicy());
        this.notInStubFile = new HashMap<String, Map<String,Map<Element,Integer>>>();
        postInit();
        if (checker != null)
        ((FlowChecker)checker).notInStubFile.putAll(notInStubFile);

    }
    @Override
    protected QualifierDefaults createQualifierDefaults() {
        QualifierDefaults defaults =  super.createQualifierDefaults();
        // Use the top type for local variables and let flow refine the type.
        //Upper bounds should be top too.
        DefaultLocation[] topLocations = {LOCAL_VARIABLE,RESOURCE_VARIABLE, UPPER_BOUNDS};

        defaults.addAbsoluteDefaults(ANYSOURCE, topLocations);
        defaults.addAbsoluteDefaults(NOSINK, topLocations);

        //Default for receivers and parameters is (All sources allowed) -> CONDITIONAL
        DefaultLocation[] conditionalSinkLocs = {RECEIVERS, DefaultLocation.PARAMETERS};
        defaults.addAbsoluteDefaults(CONDITIONALSINK, conditionalSinkLocs);
        defaults.addAbsoluteDefaults(FROMCONDITIONALSOURCE, conditionalSinkLocs);


        // Default is LITERAL -> (ALL MAPPED SINKS) for everything else
        defaults.addAbsoluteDefault(FROMLITERALSINK, OTHERWISE);
        defaults.addAbsoluteDefault(LITERALSOURCE, OTHERWISE);

        return defaults;
    }


    public  AnnotationMirror createAnnoFromSink(final Set<FlowPermission> sinks) {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
                Sink.class);
        builder.setValue("value", sinks.toArray(new FlowPermission[sinks.size()]));
        return builder.build();
    }

    public  AnnotationMirror createAnnoFromSource(Set<FlowPermission> sources) {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
                Source.class);
        builder.setValue("value", sources.toArray(new FlowPermission[sources.size()]));
        return builder.build();
    }

    protected ExecutableElement sourceValue;
    protected ExecutableElement sinkValue;



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
        DefaultApplierElement applier = new DefaultApplierElement(this, element, type);

        if (iter != null && iter.getKind() == ElementKind.CONSTRUCTOR
                && type !=null && type.getKind() == TypeKind.DECLARED){
            //TODO constructor hack
            Set<FlowPermission> sources = Flow.getSources(type);
            Set<FlowPermission> sinks = Flow.getSinks(type);
            AnnotationMirror polysink = type.getAnnotation(PolySink.class);
            AnnotationMirror polysource = type.getAnnotation(PolySource.class);
            if(sources.isEmpty() && sinks.isEmpty() && polysink == null && polysource == null){
                type.addAnnotation(NOSOURCE);
                type.addAnnotation(ANYSINK);
            }
        }
        while (iter != null) {
            if (this.isFromByteCode(iter)) {
                notAnnotated(element);
                // Checking if ignoring NOT_REVIEWED warnings
                if (!IGNORENR) {
                    // TODO:instead of not reviewed we could issue a new
                    // error
                    // Something like Error: ByteCode method, method, has
                    // not been reviewed
                    applier.apply(NR_SINK, DefaultLocation.OTHERWISE);
                    applier.apply(NR_SOURCE, DefaultLocation.OTHERWISE);

                }

            } else if (this.getDeclAnnotation(iter, PolyFlow.class) != null) {
                // Use poly flow sources and sinks for return types .
                applier.apply(POLYSOURCE, DefaultLocation.RETURNS);
                applier.apply(POLYSINK, DefaultLocation.RETURNS);

                // Use poly flow sources and sinks for Parameter types (This is
                // excluding receivers)
                applier.apply(POLYSINK, DefaultLocation.PARAMETERS);
                applier.apply(POLYSOURCE, DefaultLocation.PARAMETERS);

                return;

            } else if (this.getDeclAnnotation(iter, PolyFlowReceiver.class) != null) {
                // Use poly flow sources and sinks for return types .
                applier.apply(POLYSOURCE, DefaultLocation.RETURNS);
                applier.apply(POLYSINK, DefaultLocation.RETURNS);

                // Use poly flow sources and sinks for Parameter types (This is
                // excluding receivers)
                applier.apply(POLYSINK, DefaultLocation.PARAMETERS);
                applier.apply(POLYSOURCE, DefaultLocation.PARAMETERS);

                // Use poly flow sources and sinks for receiver types
                applier.apply(POLYSINK, DefaultLocation.RECEIVERS);
                applier.apply(POLYSOURCE, DefaultLocation.RECEIVERS);

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
    protected TreeAnnotator createTreeAnnotator() {
       FlowPolicyTreeAnnotator treeAnnotator = new FlowPolicyTreeAnnotator(this);

        // But let's send null down any sink and give it no sources.
        treeAnnotator.addTreeKind(Tree.Kind.NULL_LITERAL, ANYSINK);
        treeAnnotator.addTreeKind(Tree.Kind.NULL_LITERAL, NOSOURCE);

        // Literals, other than null are different too
        // There are no Byte or Short literal types in java (0b is treated as an
        // int),
        // so there does not need to be a mapping for them here.
        treeAnnotator.addTreeKind(Tree.Kind.INT_LITERAL, LITERALSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.LONG_LITERAL, LITERALSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.FLOAT_LITERAL, LITERALSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.DOUBLE_LITERAL, LITERALSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.BOOLEAN_LITERAL, LITERALSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.CHAR_LITERAL, LITERALSOURCE);
        treeAnnotator.addTreeKind(Tree.Kind.STRING_LITERAL, LITERALSOURCE);

        treeAnnotator.addTreeKind(Tree.Kind.INT_LITERAL, FROMLITERALSINK);
        treeAnnotator.addTreeKind(Tree.Kind.LONG_LITERAL, FROMLITERALSINK);
        treeAnnotator.addTreeKind(Tree.Kind.FLOAT_LITERAL, FROMLITERALSINK);
        treeAnnotator.addTreeKind(Tree.Kind.DOUBLE_LITERAL, FROMLITERALSINK);
        treeAnnotator.addTreeKind(Tree.Kind.BOOLEAN_LITERAL, FROMLITERALSINK);
        treeAnnotator.addTreeKind(Tree.Kind.CHAR_LITERAL, FROMLITERALSINK);
        treeAnnotator.addTreeKind(Tree.Kind.STRING_LITERAL, FROMLITERALSINK);
        return treeAnnotator;
    }

    class FlowPolicyTreeAnnotator extends TreeAnnotator {

        public FlowPolicyTreeAnnotator(FlowAnnotatedTypeFactory atypeFactory) {
            super(atypeFactory);
        }

        @Override
        public Void defaultAction(Tree tree, AnnotatedTypeMirror type) {
            completePolicyFlows(type);
            return super.defaultAction(tree, type);
        }

        @Override
        public Void visitTypeCast(TypeCastTree node, AnnotatedTypeMirror type) {
            // TODO should super do this call?
            defaultAction(node, type);
            return super.visitTypeCast(node, type);
        }

        @Override
        public Void visitUnary(UnaryTree node, AnnotatedTypeMirror type) {
            // TODO should super do this call?
            defaultAction(node, type);
            return super.visitUnary(node, type);
        }

        @Override
        public Void visitBinary(BinaryTree node, AnnotatedTypeMirror type) {
            // TODO should super do this call?
            defaultAction(node, type);
            return super.visitBinary(node, type);
        }

        @Override
        public Void visitCompoundAssignment(CompoundAssignmentTree node, AnnotatedTypeMirror type) {
            // TODO should super do this call?
            defaultAction(node, type);
            return super.visitCompoundAssignment(node, type);
        }

        @Override
        public Void visitNewArray(NewArrayTree tree, AnnotatedTypeMirror type) {
            // TODO should super do this call?
            defaultAction(tree, type);
            return super.visitNewArray(tree, type);
        }

   }

    @Override
    protected TypeAnnotator createTypeAnnotator() {
        return new FlowPolicyTypeAnnotator(this);
    }

    /**
     * If a type only contains a flow source or flow sink, the other qualifier is
     * filled in with the most general possible information that is consistent
     * with the policy file.
     * @author smillst
     *
     */
    class FlowPolicyTypeAnnotator extends TypeAnnotator {

        // FlowChecker checker;
        public FlowPolicyTypeAnnotator(FlowAnnotatedTypeFactory factory) {
            super(factory);
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

            //Don't call super because it skips the receiver type for some reason
            scan(t.getReturnType(), elem);
            scanAndReduce(t.getReceiverType(), elem, null);
            scanAndReduce(t.getParameterTypes(), elem, null);
            scanAndReduce(t.getThrownTypes(), elem, null);
            scanAndReduce(t.getTypeVariables(), elem, null);
            return null;
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

    }

    protected void completePolicyFlows(final AnnotatedTypeMirror type) {
        if(shouldNotComplete(type)){
            return ;
        }

        final Set<FlowPermission> sources = Flow.getSources(type);
        final Set<FlowPermission> sinks = Flow.getSinks(type);

        AnnotationMirror newAnno;
        if (!sources.isEmpty()) {
            Set<FlowPermission> newSink = getFlowPolicy().getIntersectionAllowedSinks(sources);
            newAnno=  createAnnoFromSink(newSink);
            type.replaceAnnotation(newAnno);
        } else if (!sinks.isEmpty()) {
            Set<FlowPermission> newSource = getFlowPolicy().getIntersectionAllowedSources(sinks);
            newAnno=  createAnnoFromSource(newSource);
            type.replaceAnnotation(newAnno);
        }

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
    private static boolean shouldNotComplete(AnnotatedTypeMirror type) {
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



    @Override
    protected MultiGraphQualifierHierarchy.MultiGraphFactory createQualifierHierarchyFactory() {
        return new MultiGraphQualifierHierarchy.MultiGraphFactory(this);
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new FlowQualifierHierarchy(factory);
    }

    private class FlowQualifierHierarchy extends MultiGraphQualifierHierarchy {

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
            return NOSOURCE.getAnnotationType().equals(anno.getAnnotationType())
                    || isPolySourceQualifier(anno);
        }

        private boolean isPolySourceQualifier(AnnotationMirror anno) {
            return POLYSOURCE.getAnnotationType().equals(anno.getAnnotationType());
        }

        private boolean isSinkQualifier(AnnotationMirror anno) {
            return NOSINK.getAnnotationType().equals(anno.getAnnotationType())
                    || isPolySinkQualifier(anno);
        }

        private boolean isPolySinkQualifier(AnnotationMirror anno) {
            return POLYSINK.getAnnotationType().equals(anno.getAnnotationType());
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
        public boolean isSubtype(Collection<? extends AnnotationMirror> rhs,
                Collection<? extends AnnotationMirror> lhs) {
            if (rhs.isEmpty() ^ lhs.isEmpty()) {
                // TODO: more general fix
                // This happens when casting:
                /**
                 * class TypeAsKeyHashMap<T> { public <S extends T> S get(T
                 * type) { return (S) type; } }
                 */

                // super will give this error
                // error: MultiGraphQualifierHierarchy: empty annotations in
                // lhs: or rhs:

                return false;
            }
            return super.isSubtype(rhs, lhs);
        }

        @Override
        public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
            try {
                checkAny(rhs);
                checkAny(lhs);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                System.out.println(e.getMessage());
                // e.printStackTrace();
                // System.exit(0);
            }

            if (isSourceQualifier(rhs)) {
                if (isPolySourceQualifier(lhs)) {
                    // If LHS is poly, rhs has to be bottom or poly qualifier.
                    return AnnotationUtils.areSame(rhs, NOSOURCE)
                            || AnnotationUtils.areSame(rhs, POLYSOURCE)
                            || AnnotationUtils.areSame(rhs, POLYALL);
                } else if (isPolySourceQualifier(rhs)) {
                    // If RHS is poly, lhs has to be top or poly qualifier.
                    return AnnotationUtils.areSame(lhs, ANYSOURCE)
                            || AnnotationUtils.areSame(lhs, POLYSOURCE)
                            || AnnotationUtils.areSame(lhs, POLYALL);
                } else {
                    if (!isSourceQualifier(lhs)) {
                        return false;
                    }
                    Set<FlowPermission> lhssrc = Flow.getSources(lhs);
                    Set<FlowPermission> rhssrc = Flow.getSources(rhs);
                 // TODO: Remove the ANY below when we start warning about Source(ANY, Something else)
                    return AnnotationUtils.areSame(lhs, ANYSOURCE) ||
                            lhssrc.containsAll(rhssrc) || lhssrc.contains(FlowPermission.ANY);
                }
            } else if (isSinkQualifier(rhs)) {
                if (isPolySinkQualifier(lhs)) {
                    // If LHS is poly, rhs has to be bottom or poly qualifier.
                    return AnnotationUtils.areSame(rhs, ANYSINK)
                            || AnnotationUtils.areSame(rhs, POLYSINK);
                } else if (isPolySinkQualifier(rhs)) {
                    // If RHS is poly, lhs has to be top or poly qualifier.
                    return AnnotationUtils.areSame(lhs, NOSINK)
                            || AnnotationUtils.areSame(lhs, POLYSINK);
                } else {
                    if (!isSinkQualifier(lhs)) {
                        return false;
                    }
                    Set<FlowPermission> lhssnk = Flow.getSinks(lhs);
                    Set<FlowPermission> rhssnk = Flow.getSinks(rhs);
                    return lhssnk.isEmpty() || rhssnk.containsAll(lhssnk)
                            || (rhssnk.contains(FlowPermission.ANY) && rhssnk.size() == 1);
                }
            } else if (QualifierPolymorphism.isPolyAll(rhs)) {
                // If RHS is polyall, the LHS has to be a top qualifier or also
                // poly.
                return AnnotationUtils.areSame(lhs, NOSINK)
                        || AnnotationUtils.areSame(lhs, ANYSOURCE)
                        || AnnotationUtils.areSame(lhs, POLYSINK)
                        || AnnotationUtils.areSame(lhs, POLYSOURCE);

            } else {
                checker.errorAbort("FlowChecker: unexpected AnnotationMirrors: " + rhs + " and " + lhs);
                return false; // dead code
            }
        }

        private void checkAny(AnnotationMirror anm) throws Exception {
            boolean isPolySink = AnnotationUtils.areSame(anm, POLYSINK);
            boolean isPolySource = AnnotationUtils.areSame(anm, POLYSOURCE);

            if (!isPolySource && isSourceQualifier(anm)) {
                Set<FlowPermission> sources = Flow.getSources(anm);
                if (sources.contains(FlowPermission.ANY) && sources.size() > 1) {
                    throw new Exception("Found FlowPermission.ANY and something else");
                }
            }
            if (!isPolySink && isSinkQualifier(anm)) {
                Set<FlowPermission> sinks = Flow.getSinks(anm);
                if (sinks.contains(FlowPermission.ANY) && sinks.size() > 1) {
                    throw new Exception("Found FlowPermission.ANY and something else");
                }
            }

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
                    final Set<FlowPermission> superset = Flow.unionSources(a1, a2);
                    return boundSource(superset);

                } else if (AnnotationUtils.areSameIgnoringValues(a1, SINK)) {
                    final Set<FlowPermission> intersection = Flow.intersectSinks(a1, a2);
                    return boundSink(intersection);
                }
                // Poly Flows must be handled as if they are Top Type
            } else if (AnnotationUtils.areSame(a1, POLYSINK)) {
                if (AnnotationUtils.areSameIgnoringValues(a2, SINK)) {
                    return boundSink(new TreeSet<FlowPermission>());
                }

            } else if (AnnotationUtils.areSame(a2, POLYSINK)) {
                if (AnnotationUtils.areSameIgnoringValues(a1, SINK)) {
                    return boundSink(new TreeSet<FlowPermission>());
                }
            } else if (AnnotationUtils.areSame(a1, POLYSOURCE)) {
                if (AnnotationUtils.areSameIgnoringValues(a2, SOURCE)) {
                    Set<FlowPermission> top = new TreeSet<FlowPermission>();
                    top.add(FlowPermission.ANY);
                    return boundSource(top);
                }

            } else if (AnnotationUtils.areSame(a2, POLYSOURCE)) {
                if (AnnotationUtils.areSameIgnoringValues(a1, SOURCE)) {
                    Set<FlowPermission> top = new TreeSet<FlowPermission>();
                    top.add(FlowPermission.ANY);
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
                    final Set<FlowPermission> intersection = Flow.intersectSources(a1, a2);
                    return boundSource(intersection);

                } else if (AnnotationUtils.areSameIgnoringValues(a1, SINK)) {
                    final Set<FlowPermission> superSet = Flow.unionSinks(a1, a2);
                    return boundSink(superSet);

                }
                // Poly Flows must be handled as if they are Bottom Type
            } else if (AnnotationUtils.areSame(a1, POLYSINK)) {
                if (AnnotationUtils.areSameIgnoringValues(a2, SINK)) {
                    Set<FlowPermission> bottom = new TreeSet<FlowPermission>();
                    bottom.add(FlowPermission.ANY);
                    return boundSink(bottom);
                }

            } else if (AnnotationUtils.areSame(a2, POLYSINK)) {
                if (AnnotationUtils.areSameIgnoringValues(a1, SINK)) {
                    Set<FlowPermission> bottom = new TreeSet<FlowPermission>();
                    bottom.add(FlowPermission.ANY);
                    return boundSink(bottom);
                }
            } else if (AnnotationUtils.areSame(a1, POLYSOURCE)) {
                if (AnnotationUtils.areSameIgnoringValues(a2, SOURCE)) {
                    return boundSource(new TreeSet<FlowPermission>());
                }

            } else if (AnnotationUtils.areSame(a2, POLYSOURCE)) {
                if (AnnotationUtils.areSameIgnoringValues(a1, SOURCE)) {
                    return boundSource(new TreeSet<FlowPermission>());
                }
            }
            return super.greatestLowerBound(a1, a2);
        }

        private AnnotationMirror boundSource(final Set<FlowPermission> flowSource) {

            final AnnotationMirror am;
            if (flowSource.contains(FlowPermission.ANY)) { // contains all
                                                           // Source
                am = getTopAnnotation(SOURCE);
            } else if (flowSource.isEmpty()) {
                am = getBottomAnnotation(SOURCE);
            } else {
                am = createAnnoFromSource(flowSource);
            }
            return am;
        }

        private AnnotationMirror boundSink(final Set<FlowPermission> flowSink) {
            final AnnotationMirror am;
            if (flowSink.isEmpty()) {
                am = getTopAnnotation(SINK);
            } else if (flowSink.contains(FlowPermission.ANY)) { // contains all
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
}

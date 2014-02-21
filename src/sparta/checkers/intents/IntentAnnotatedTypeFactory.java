package sparta.checkers.intents;

import static checkers.quals.DefaultLocation.LOCAL_VARIABLE;
import static checkers.quals.DefaultLocation.OTHERWISE;
import static checkers.quals.DefaultLocation.RECEIVERS;
import static checkers.quals.DefaultLocation.RESOURCE_VARIABLE;
import static checkers.quals.DefaultLocation.UPPER_BOUNDS;

import checkers.basetype.BaseTypeChecker;
import checkers.quals.DefaultLocation;
import checkers.source.Result;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedArrayType;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.types.QualifierHierarchy;
import checkers.types.TreeAnnotator;
import checkers.util.AnnotationBuilder;
import checkers.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import checkers.util.QualifierDefaults.DefaultApplierElement;
import checkers.util.QualifierDefaults;

import javacutils.AnnotationUtils;
import javacutils.ElementUtils;
import javacutils.Pair;
import javacutils.TreeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;

import sparta.checkers.Flow;
import sparta.checkers.FlowAnnotatedTypeFactory;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.ParameterizedFlowPermission;
import sparta.checkers.quals.Extra;
import sparta.checkers.quals.IntentMap;
import sparta.checkers.quals.PolyFlow;
import sparta.checkers.quals.PolyFlowReceiver;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;

public class IntentAnnotatedTypeFactory extends FlowAnnotatedTypeFactory {

    protected final AnnotationMirror INTENTEXTRAS, IEXTRA, EMPTYINTENTEXTRAS, INTENTEXTRASALL;
    protected final ComponentMap componentMap;

    public IntentAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        // Must call super.initChecker before the lint option can be checked.
        final String ipArg = checker
            .getOption(ComponentMap.COMPONENT_MAP_FILE_OPTION);
        componentMap = new ComponentMap(ipArg);

        INTENTEXTRAS = AnnotationUtils.fromClass(elements, IntentMap.class);
        IEXTRA = AnnotationUtils.fromClass(elements, Extra.class);
        EMPTYINTENTEXTRAS = createEmptyIntentExtras(); // top
        INTENTEXTRASALL = createAllIntentExtras(); // bottom
        if (this.getClass().equals(IntentAnnotatedTypeFactory.class)) {
            this.postInit();
        }
    }

    @Override
    protected void handleDefaulting(final Element element, final AnnotatedTypeMirror type) {
        Element iter = element;
        DefaultApplierElement applier = new DefaultApplierElement(this, element, type);
        while (iter != null) {
            if (this.isFromByteCode(iter)) {
                if(IntentUtils.RECEIVE_INTENT_METHODS.contains(element.getSimpleName().toString()) ||
                        type.getUnderlyingType().toString().equals("android.content.Intent")) {
                    applier.apply(LITERALSOURCE, DefaultLocation.RETURNS);
                    applier.apply(FROMLITERALSINK, DefaultLocation.RETURNS);
                }
                // Checking if ignoring NOT_REVIEWED warnings
                else if (!IGNORENR) {
                    applier.apply(NR_SINK, DefaultLocation.OTHERWISE);
                    applier.apply(NR_SOURCE, DefaultLocation.OTHERWISE);
                }

            } else if (this.getDeclAnnotation(iter, PolyFlow.class) != null || 
                    this.getDeclAnnotation(iter, PolyFlowReceiver.class) != null) {
                super.handleDefaulting(iter,type);
            } 

            if (iter instanceof PackageElement) {
                iter = ElementUtils.parentPackage(this.elements, (PackageElement) iter);
            } else {
                iter = iter.getEnclosingElement();
            }
        }
    }

    private AnnotationMirror createAllIntentExtras() {
        // TODO: Define the bottom type of @IntentMap
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
            IntentMap.class);
        return builder.build();
    }

    private AnnotationMirror createEmptyIntentExtras() {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
            IntentMap.class);
        return builder.build();
    }

    /**
     * This method changes @Source(INTENT) and @Sink(INTENT) to
     * @Source(ANY), @Sink({}) and
     *  changes the @Source and @Sink from the return type of getExtra calls. 
     *  This is necessary because the stub files will maintain @Source(INTENT) and
     * @Sink(INTENT) annotations to perform flow check analysis without the
     * intent analysis.
     */
    @Override
    public Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> methodFromUse(
            MethodInvocationTree tree) {
        Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> mfuPair = super
            .methodFromUse(tree);
        if (checker instanceof IntentChecker) {
            if (IntentUtils.isGetExtraMethod(tree,getProcessingEnv())) {
                // Modifying type of getExtra call
                mfuPair = changeMethodReturnType(tree, mfuPair);
                // Modifying @Source and @Sink types for parameters
                hostChangeParametersToTop(mfuPair.first.getParameterTypes());
            } else if (IntentUtils.isPutExtraMethod(tree) || IntentUtils.isIntentPayloadMethod(tree)) {
                // Modifying @Source and @Sink types for parameters
                hostChangeParametersToTop(mfuPair.first.getParameterTypes());
            }
        }
        return mfuPair;

    }

    /**
     * Changes the parameters to TOP in the host type system.
     * 
     * For the Flow Checker:
     * This method changes the @Source(INTENT) and @Sink(INTENT) to
     * @Source(ANY) and @Sink({})
     * @param parametersAnnotations
     */

    private void hostChangeParametersToTop(
            List<AnnotatedTypeMirror> parametersAnnotations) {
        for (AnnotatedTypeMirror parameterAnnotation : parametersAnnotations) {
            if (parameterAnnotation.hasAnnotation(Source.class)) {
                // Modifying @Source type
                if(parameterAnnotation instanceof AnnotatedArrayType) {
                    ((AnnotatedArrayType) parameterAnnotation).getComponentType().removeAnnotation(Source.class);
                    ((AnnotatedArrayType) parameterAnnotation).getComponentType().addAnnotation(ANYSOURCE);
                }
                parameterAnnotation.removeAnnotation(Source.class);
                parameterAnnotation.addAnnotation(ANYSOURCE);
            }
            if (parameterAnnotation.hasAnnotation(Sink.class)) {
                // Modifying @Sink type
                if(parameterAnnotation instanceof AnnotatedArrayType) {
                    ((AnnotatedArrayType) parameterAnnotation).getComponentType().removeAnnotation(Sink.class);
                    ((AnnotatedArrayType) parameterAnnotation).getComponentType().addAnnotation(NOSINK);
                }
                parameterAnnotation.removeAnnotation(Sink.class);
                parameterAnnotation.addAnnotation(NOSINK);
            }
        }
    }

    @Override
    protected QualifierDefaults createQualifierDefaults() {
        QualifierDefaults defaults = super.createQualifierDefaults();
        DefaultLocation[] topLocations = { LOCAL_VARIABLE, RESOURCE_VARIABLE,
            UPPER_BOUNDS };
        defaults.addAbsoluteDefaults(EMPTYINTENTEXTRAS, topLocations);
        defaults.addAbsoluteDefault(INTENTEXTRASALL, OTHERWISE);
        
        DefaultLocation[] conditionalSinkLocs = {RECEIVERS, DefaultLocation.PARAMETERS};
        defaults.addAbsoluteDefaults(EMPTYINTENTEXTRAS, conditionalSinkLocs);
        defaults.addAbsoluteDefaults(EMPTYINTENTEXTRAS, conditionalSinkLocs);
        return defaults;
    }

    @Override
    protected TreeAnnotator createTreeAnnotator() {
        TreeAnnotator treeAnnotator = super.createTreeAnnotator();
        treeAnnotator.addTreeKind(Tree.Kind.NULL_LITERAL, EMPTYINTENTEXTRAS);
        treeAnnotator.addTreeKind(Tree.Kind.INT_LITERAL, EMPTYINTENTEXTRAS);
        treeAnnotator.addTreeKind(Tree.Kind.LONG_LITERAL, EMPTYINTENTEXTRAS);
        treeAnnotator.addTreeKind(Tree.Kind.FLOAT_LITERAL, EMPTYINTENTEXTRAS);
        treeAnnotator.addTreeKind(Tree.Kind.DOUBLE_LITERAL, EMPTYINTENTEXTRAS);
        treeAnnotator.addTreeKind(Tree.Kind.BOOLEAN_LITERAL, EMPTYINTENTEXTRAS);
        treeAnnotator.addTreeKind(Tree.Kind.CHAR_LITERAL, EMPTYINTENTEXTRAS);
        treeAnnotator.addTreeKind(Tree.Kind.STRING_LITERAL, EMPTYINTENTEXTRAS);
        return treeAnnotator;
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new IntentQualifierHierarchy(factory);
    }

    private class IntentQualifierHierarchy extends FlowQualifierHierarchy {

        protected IntentQualifierHierarchy(MultiGraphFactory f) {
            super(f);
        }

        @Override
        protected Set<AnnotationMirror> findBottoms(
                Map<AnnotationMirror, Set<AnnotationMirror>> supertypes) {
            Set<AnnotationMirror> newBottoms = super.findBottoms(supertypes);
            newBottoms.add(INTENTEXTRASALL);
            return newBottoms;
        }

        @Override
        protected Set<AnnotationMirror> findTops(
                Map<AnnotationMirror, Set<AnnotationMirror>> supertypes) {
            Set<AnnotationMirror> newTops = super.findTops(supertypes);
            newTops.add(EMPTYINTENTEXTRAS);
            return newTops;
        }

        private boolean isIntentExtrasQualifier(AnnotationMirror anno) {
            if (INTENTEXTRAS.getAnnotationType() != null
                    && anno.getAnnotationType() != null) {
                return INTENTEXTRAS.getAnnotationType().asElement()
                    .equals(anno.getAnnotationType().asElement());
            }
            return INTENTEXTRAS.getAnnotationType().equals(
                anno.getAnnotationType());
        }

        private boolean isIExtraQualifier(AnnotationMirror anno) {
            return IEXTRA.getAnnotationType().equals(anno.getAnnotationType());
        }

        @Override
        public AnnotationMirror getTopAnnotation(AnnotationMirror start) {
            if (isIntentExtrasQualifier(start)) {
                return EMPTYINTENTEXTRAS;
            } else if (isIExtraQualifier(start)) {
                return IEXTRA; // What to do here?
            } else {
                return super.getTopAnnotation(start);
            }
        }

        @Override
        public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
            if (isIExtraQualifier(rhs)) {
                checker.errorAbort("IntentChecker: unexpected AnnotationMirrors: "
                    + rhs + " and " + lhs);
                return false;
            } else if (isIntentExtrasQualifier(rhs)) {
                if (rhs == null || lhs == null || !isIntentExtrasQualifier(lhs)) {
                    return false;
                }
                List<AnnotationMirror> lhsIExtrasList = IntentUtils.getIExtras(lhs);
                if (lhsIExtrasList.isEmpty()) {
                    return true;
                }
                for (AnnotationMirror lhsIExtra : lhsIExtrasList) {
                    String leftKey = IntentUtils.getKeyName(lhsIExtra);;

                    if(IntentUtils.hasKey(rhs, leftKey)) {
                        if(!hostIsExactType(rhs, lhsIExtra, leftKey)) {
                            return false;
                        }
                        
                    } else {
                        return false;
                    }
                }
                return true;
            }
            return super.isSubtype(rhs, lhs);
        }


        /**
         * temporary auxiliary method used to check whether the types
         * of the keys are the same.
         * rule for information flow analysis on intents.
         * @param rhs
         * @param lhsIExtra
         * @param leftKey
         * @return
         */
        private boolean hostIsExactType(AnnotationMirror rhs,
                AnnotationMirror lhsIExtra, String leftKey) {
            AnnotationMirror rhsIExtra = IntentUtils.getIExtra(rhs, leftKey);

            Set<FlowPermission> lhsAnnotatedSources = IntentUtils.getSources(lhsIExtra);
            Set<FlowPermission> lhsAnnotatedSinks = IntentUtils.getSinks(lhsIExtra);
            Set<FlowPermission> rhsAnnotatedSources = IntentUtils.getSources(rhsIExtra);
            Set<FlowPermission> rhsAnnotatedSinks = IntentUtils.getSinks(rhsIExtra);

            return(lhsAnnotatedSources.containsAll(rhsAnnotatedSources)
                    && rhsAnnotatedSources.containsAll(lhsAnnotatedSources)
                    && lhsAnnotatedSinks.containsAll(rhsAnnotatedSinks) 
                    && rhsAnnotatedSinks.containsAll(lhsAnnotatedSinks)); 
        }

       
        /**
         * The LUB between 2 @IntentMap is an @IntentMap containing all
         * the @Extra with keys both have in common. For each pair of 2
         * 
         * @Extra with the same key in the 2 @IntentMap, the resulting @Source
         *         is the union of the @Source of both @Extra, and the
         *         resulting @Sink is the intersection of @Sink in both @Extra.
         */

        @Override
        public AnnotationMirror leastUpperBound(AnnotationMirror a1,
                AnnotationMirror a2) {
            if (isSubtype(a1, a2)) {
                return a2;
            }
            if (isSubtype(a2, a1)) {
                return a1;
            }

            if (AnnotationUtils.areSameIgnoringValues(a1, a2)) {
                if (AnnotationUtils.areSameIgnoringValues(a1, INTENTEXTRAS)) {
                    List<AnnotationMirror> a1IExtrasList = IntentUtils.getIExtras(a1);
                    List<AnnotationMirror> IExtraOutputSet = 
                        new ArrayList<AnnotationMirror>();

                    for (AnnotationMirror a1IExtra : a1IExtrasList) {
                        String a1IExtraKey = IntentUtils.getKeyName(a1IExtra);
                        if (IntentUtils.hasKey(a2, a1IExtraKey)) {
                            AnnotationMirror a2IExtra = IntentUtils
                                .getIExtra(a2, a1IExtraKey);
                            // Here we have found matching keys.
                            AnnotationMirror newIExtra = hostLeastUpperBounds(
                                a1IExtra, a1IExtraKey, a2IExtra);
                            IExtraOutputSet.add(newIExtra);
                        }

                    }
                    AnnotationMirror output = IntentUtils
                        .addIExtrasToIntentExtras(EMPTYINTENTEXTRAS,
                            IExtraOutputSet, processingEnv);
                    return output;
                } else if (AnnotationUtils.areSameIgnoringValues(a1, IEXTRA)) {
                    // is this one necessary?
                }
            }

            return super.leastUpperBound(a1, a2);
        }
        /**
         * temporary auxiliary method used to calculate the LUB
         * between 2 @IntentMap whose @Extra contains information flow.
         */

        private AnnotationMirror hostLeastUpperBounds(
                AnnotationMirror a1IExtra, String a1IExtraKey,
                AnnotationMirror a2IExtra) {
            // First do the union of sources:
            Set<ParameterizedFlowPermission> unionSources = IntentUtils
                .unionSourcesIExtras(a1IExtra, a2IExtra);

            // Intersection of sinks:
            Set<ParameterizedFlowPermission> intersectedSinks = IntentUtils
                .intersectionSinksIExtras(a1IExtra,a2IExtra);

            // Create a new IExtra with the results of sources
            // and sinks
            AnnotationMirror newIExtra = IntentUtils
            .createIExtraAnno(a1IExtraKey, 
                createAnnoFromSource(unionSources),
                createAnnoFromSink(intersectedSinks),
                processingEnv);
            return newIExtra;
        }

        /**
         * The GLB between 2 @IntentMap will contain the union of keys that
         * these annotations contain, and the @Source of the @Extra with this
         * key will be the intersection of sources and the @Sink will be the
         * union of sinks.
         */

        @Override
        public AnnotationMirror greatestLowerBound(AnnotationMirror a1,
                AnnotationMirror a2) {
            // What would be the GLB between (Key k1, source s1) and (Key k1,
            // source s2) ?
            // (Key k1, source empty) ? I think so. Need to do the same on LUB.
            if (AnnotationUtils.areSame(a1, a2))
                return a1;

            if (AnnotationUtils.areSameIgnoringValues(a1, a2)) {
                if (AnnotationUtils.areSameIgnoringValues(a1, INTENTEXTRAS)) {
                    List<AnnotationMirror> a1IExtrasList = IntentUtils.getIExtras(a1);
                    List<AnnotationMirror> a2IExtrasList = IntentUtils.getIExtras(a2);
                    List<AnnotationMirror> IExtraOutputSet = 
                        new ArrayList<AnnotationMirror>();
                    for (AnnotationMirror a1IExtra : a1IExtrasList) {
                        String a1IExtraKey = IntentUtils.getKeyName(a1IExtra);
                        if (IntentUtils.hasKey(a2, a1IExtraKey)) {
                            AnnotationMirror a2IExtra = IntentUtils
                                .getIExtra(a2, a1IExtraKey);
                            // If we have found matching keys:
                            AnnotationMirror newIExtra = hostGreatestLowerBound(
                                a1IExtra, a1IExtraKey, a2IExtra);
                            IExtraOutputSet.add(newIExtra);
                        } else {
                            // If we could not find the key in a2, add the @Extra
                            // with this key to the resulting @IntentMap
                            IExtraOutputSet.add(a1IExtra);
                        }

                    }
                    // Now we need to fill the resulting set with @Extra
                    // containing keys
                    // that are in a2 but not in a1.
                    for (AnnotationMirror a2IExtra : a2IExtrasList) {
                        String a2IExtraKey = IntentUtils.getKeyName(a2IExtra);;
                        if (!IntentUtils.hasKey(a1, a2IExtraKey)) {
                            IExtraOutputSet.add(a2IExtra);
                        }
                    }

                    AnnotationMirror output = IntentUtils
                        .addIExtrasToIntentExtras(EMPTYINTENTEXTRAS,
                            IExtraOutputSet, processingEnv);
                    return output;
                } else if (AnnotationUtils.areSameIgnoringValues(a1, IEXTRA)) {
                    // is this one necessary?
                }
            }

            return super.greatestLowerBound(a1, a2);
        }

        /**
         * temporary auxiliary method used to type-check the calculate the LUB
         * between 2 @IntentMap whose @Extra contains information flow.
         */
        
        private AnnotationMirror hostGreatestLowerBound(
                AnnotationMirror a1IExtra, String a1IExtraKey,
                AnnotationMirror a2IExtra) {
            // First do the intersection of sources:
            Set<ParameterizedFlowPermission> intersectedSources = IntentUtils
                .intersectionSourcesIExtras(a1IExtra, a2IExtra);

            // Union of sinks:
            Set<ParameterizedFlowPermission> unionSinks = IntentUtils
                .unionSinksIExtras(a1IExtra, a2IExtra);

            // Create a new IExtra with the results of sources
            // and sinks
            AnnotationMirror newIExtra = IntentUtils.createIExtraAnno(
                a1IExtraKey,createAnnoFromSource(intersectedSources),
                createAnnoFromSink(unionSinks),processingEnv);
            return newIExtra;
        }

    }

    /**
     * This method changes the return type of a
     * <code>Intent.getExtra(key)</code> call depending on the @IntentMap
     * type of Intent and <code>key</code>.
     * 
     * @param tree
     *            The method tree
     * @param origResult
     *            The original result
     * @return
     */

    public Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> changeMethodReturnType(
            MethodInvocationTree tree,
            Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> origResult) {
        if (tree != null) {
            ExpressionTree receiver = TreeUtils.getReceiverTree(tree);
            AnnotatedTypeMirror receiverType = getAnnotatedType(receiver);
            if (receiverType.hasAnnotation(IntentMap.class)) {
                AnnotationMirror stringValAnno = getAnnotationMirror(tree.getArguments().get(0), 
                        checkers.value.quals.StringVal.class);
               
                if (stringValAnno != null) {                                            
                    List<String> keys = AnnotationUtils.getElementValueArray(stringValAnno, "value", String.class, true);   
                    AnnotationMirror receiverIntentAnnotation = receiverType
                            .getAnnotation(IntentMap.class);
                        List<AnnotationMirror> iExtrasList = AnnotationUtils
                            .getElementValueArray(receiverIntentAnnotation,
                                "value", AnnotationMirror.class, true);
                    for(String keyName : keys) {
                        // Here we have the list of IExtra from the Intent
                        // We iterate this list until we find the key we need.
                        for (AnnotationMirror iExtra : iExtrasList) {
                            String key = IntentUtils.getKeyName(iExtra);
                            if (key.equals(keyName)) {
                                // Found the key, now change the annotation of the
                                // return of getExtra()
                                return hostChangeMethodReturn(origResult,
                                    iExtra);
                            }
                        }
                    }
                } else {
                    //Handle keys which are not constants
                    checker.report(Result.failure("intent.key.variable", tree.getArguments().get(0)), tree);
                }
                
            }
        }
        /*
         * Return original result if resolution failed
         */
        return origResult;
    }

    private Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> hostChangeMethodReturn(
            Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> origResult,
            AnnotationMirror iExtra) {
        // correct @Source and @Sink annotations
        Set<ParameterizedFlowPermission> annotatedSources = IntentUtils.getSourcesPFP(iExtra);
        Set<ParameterizedFlowPermission> annotatedSinks = IntentUtils.getSinksPFP(iExtra);

        AnnotationMirror sourceAnnotation = createAnnoFromSource(annotatedSources);
        AnnotationMirror sinkAnnotation = createAnnoFromSink(annotatedSinks);
        origResult.first.getReturnType().clearAnnotations();
        origResult.first.getReturnType().addAnnotation(sourceAnnotation);
        origResult.first.getReturnType().addAnnotation(sinkAnnotation);
        
        
        if (!origResult.first.getReturnType().hasAnnotation(
                IntentMap.class)) {
            origResult.first.getReturnType().addAnnotation(
                EMPTYINTENTEXTRAS);
        }
        
        //Handling array types.
        if(origResult.first.getReturnType() instanceof AnnotatedArrayType) {
            ((AnnotatedArrayType)origResult.first.getReturnType()).getComponentType().clearAnnotations();
            ((AnnotatedArrayType)origResult.first.getReturnType()).getComponentType().addAnnotation(sourceAnnotation);
            ((AnnotatedArrayType)origResult.first.getReturnType()).getComponentType().addAnnotation(sinkAnnotation);
            
            if (!((AnnotatedArrayType)origResult.first.getReturnType()).getComponentType().hasAnnotation(
                    IntentMap.class)) {
                ((AnnotatedArrayType)origResult.first.getReturnType()).getComponentType().addAnnotation(
                    EMPTYINTENTEXTRAS);
            }
        }
        
        
        return origResult;
    }


    public ComponentMap getComponentMap() {
        return componentMap;
    }

    /**
     * Read the component map file and returns the receivers from a sender
     * @param sender sender name
     * @return
     */

    public Set<String> getReceiversFromSender(String sender) {
        Set<String> receivers = getComponentMap().getIntentMap().get(sender);
        if (receivers == null || receivers.isEmpty()) {
            checker.errorAbort("Could not find receivers for class: " + sender);
        }
        return receivers;
    }
    
}

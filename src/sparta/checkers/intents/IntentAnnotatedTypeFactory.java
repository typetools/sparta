package sparta.checkers.intents;

import static checkers.quals.DefaultLocation.LOCAL_VARIABLE;
import static checkers.quals.DefaultLocation.OTHERWISE;
import static checkers.quals.DefaultLocation.RESOURCE_VARIABLE;
import static checkers.quals.DefaultLocation.UPPER_BOUNDS;

import checkers.basetype.BaseTypeChecker;
import checkers.quals.DefaultLocation;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.types.QualifierHierarchy;
import checkers.types.TreeAnnotator;
import checkers.util.AnnotationBuilder;
import checkers.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import checkers.util.QualifierDefaults;

import javacutils.AnnotationUtils;
import javacutils.Pair;
import javacutils.TreeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import sparta.checkers.FlowAnnotatedTypeFactory;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.IExtra;
import sparta.checkers.quals.IntentExtras;
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
        componentMap = new ComponentMap(new File(ipArg));

        INTENTEXTRAS = AnnotationUtils.fromClass(elements, IntentExtras.class);
        IEXTRA = AnnotationUtils.fromClass(elements, IExtra.class);
        EMPTYINTENTEXTRAS = createEmptyIntentExtras(); // top
        INTENTEXTRASALL = createAllIntentExtras(); // bottom
        if (this.getClass().equals(IntentAnnotatedTypeFactory.class)) {
            this.postInit();
        }
    }

    private AnnotationMirror createAllIntentExtras() {
        // TODO: Define the bottom type of @IntentExtras
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
            IntentExtras.class);
        return builder.build();
    }

    private AnnotationMirror createEmptyIntentExtras() {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
            IntentExtras.class);
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
                // Modifying @Source and @Sink types for parameters in getExtra
                // calls
                hostChangeParametersToTop(mfuPair.first.getParameterTypes());
            } else if (IntentUtils.isPutExtraMethod(tree)) {
                // Modifying @Source and @Sink types for parameters in putExtra
                // calls
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
                parameterAnnotation.removeAnnotation(Source.class);
                parameterAnnotation.addAnnotation(ANYSOURCE);
            }
            if (parameterAnnotation.hasAnnotation(Sink.class)) {
                // Modifying @Sink type
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
        defaults.addAbsoluteDefault(EMPTYINTENTEXTRAS, OTHERWISE);
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
                List<AnnotationMirror> lhsIExtrasList = AnnotationUtils
                    .getElementValueArray(lhs, "value",AnnotationMirror.class, true);
                if (lhsIExtrasList.isEmpty()) {
                    return true;
                }
                for (AnnotationMirror lhsIExtra : lhsIExtrasList) {
                    String leftKey = AnnotationUtils.getElementValue(lhsIExtra,
                        "key", String.class, true);

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
            AnnotationMirror rhsIExtra = IntentUtils.getIExtraWithKey(rhs, leftKey);

            Set<FlowPermission> lhsAnnotatedSources = new HashSet<FlowPermission>(
                AnnotationUtils.getElementValueEnumArray(
                    lhsIExtra, "source", FlowPermission.class, true));
            Set<FlowPermission> lhsAnnotatedSinks = new HashSet<FlowPermission>(
                AnnotationUtils.getElementValueEnumArray(
                    lhsIExtra, "sink", FlowPermission.class, true));
            Set<FlowPermission> rhsAnnotatedSources = new HashSet<FlowPermission>(
                AnnotationUtils.getElementValueEnumArray(
                    rhsIExtra, "source",
                    FlowPermission.class, true));
            Set<FlowPermission> rhsAnnotatedSinks = new HashSet<FlowPermission>(
                AnnotationUtils.getElementValueEnumArray(
                    rhsIExtra, "sink",
                    FlowPermission.class, true));

            return(lhsAnnotatedSources.containsAll(rhsAnnotatedSources)
                    && rhsAnnotatedSources.containsAll(lhsAnnotatedSources)
                    && lhsAnnotatedSinks.containsAll(rhsAnnotatedSinks) 
                    && rhsAnnotatedSinks.containsAll(lhsAnnotatedSinks)); 
        }

        /**
         * The LUB between 2 @IntentExtras is an @IntentExtras containing all
         * the @IExtra with keys both have in common. For each pair of 2
         * 
         * @IExtra with the same key in the 2 @IntentExtras, the resulting @Source
         *         is the union of the @Source of both @IExtra, and the
         *         resulting @Sink is the intersection of @Sink in both @IExtra.
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
                    List<AnnotationMirror> a1IExtrasList = AnnotationUtils
                        .getElementValueArray(a1, "value",
                            AnnotationMirror.class, true);
                    List<AnnotationMirror> IExtraOutputSet = 
                        new ArrayList<AnnotationMirror>();

                    for (AnnotationMirror a1IExtra : a1IExtrasList) {
                        String a1IExtraKey = AnnotationUtils.getElementValue(
                            a1IExtra, "key", String.class, true);
                        if (IntentUtils.hasKey(a2, a1IExtraKey)) {
                            AnnotationMirror a2IExtra = IntentUtils
                                .getIExtraWithKey(a2, a1IExtraKey);
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
         * between 2 @IntentExtras whose @IExtra contains information flow.
         */

        private AnnotationMirror hostLeastUpperBounds(
                AnnotationMirror a1IExtra, String a1IExtraKey,
                AnnotationMirror a2IExtra) {
            // First do the union of sources:
            Set<FlowPermission> unionSources = IntentUtils
                .unionSourcesIExtras(a1IExtra, a2IExtra);

            // Intersection of sinks:
            Set<FlowPermission> intersectedSinks = IntentUtils
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
         * The GLB between 2 @IntentExtras will contain the union of keys that
         * these annotations contain, and the @Source of the @IExtra with this
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
                    List<AnnotationMirror> a1IExtrasList = AnnotationUtils
                        .getElementValueArray(a1, "value",
                            AnnotationMirror.class, true);
                    List<AnnotationMirror> a2IExtrasList = AnnotationUtils
                        .getElementValueArray(a2, "value",
                            AnnotationMirror.class, true);
                    List<AnnotationMirror> IExtraOutputSet = 
                        new ArrayList<AnnotationMirror>();
                    for (AnnotationMirror a1IExtra : a1IExtrasList) {
                        String a1IExtraKey = AnnotationUtils.getElementValue(
                            a1IExtra, "key", String.class, true);
                        if (IntentUtils.hasKey(a2, a1IExtraKey)) {
                            AnnotationMirror a2IExtra = IntentUtils
                                .getIExtraWithKey(a2, a1IExtraKey);
                            // If we have found matching keys:
                            AnnotationMirror newIExtra = hostGreatestLowerBound(
                                a1IExtra, a1IExtraKey, a2IExtra);
                            IExtraOutputSet.add(newIExtra);
                        } else {
                            // If we could not find the key in a2, add the @IExtra
                            // with this key to the resulting @IntentExtras
                            IExtraOutputSet.add(a1IExtra);
                        }

                    }
                    // Now we need to fill the resulting set with @IExtra
                    // containing keys
                    // that are in a2 but not in a1.
                    for (AnnotationMirror a2IExtra : a2IExtrasList) {
                        String a2IExtraKey = AnnotationUtils.getElementValue(
                            a2IExtra, "key", String.class, true);
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
         * between 2 @IntentExtras whose @IExtra contains information flow.
         */
        
        private AnnotationMirror hostGreatestLowerBound(
                AnnotationMirror a1IExtra, String a1IExtraKey,
                AnnotationMirror a2IExtra) {
            // First do the intersection of sources:
            Set<FlowPermission> intersectedSources = IntentUtils
                .intersectionSourcesIExtras(a1IExtra, a2IExtra);

            // Union of sinks:
            Set<FlowPermission> unionSinks = IntentUtils
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
     * <code>Intent.getExtra(key)</code> call depending on the @IntentExtras
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
            String keyName = tree.getArguments().get(0).toString();
            // Removing "" from key. "key" -> key
            keyName = keyName.substring(1, keyName.length() - 1);
            if (receiverType.hasAnnotation(IntentExtras.class)) {

                AnnotationMirror receiverIntentAnnotation = receiverType
                    .getAnnotation(IntentExtras.class);
                List<AnnotationMirror> iExtrasList = AnnotationUtils
                    .getElementValueArray(receiverIntentAnnotation,
                        "value", AnnotationMirror.class, true);
                // Here we have the list of IExtra from the Intent
                // We iterate this list until we find the key we need.
                for (AnnotationMirror iExtra : iExtrasList) {
                    String key = AnnotationUtils.getElementValue(iExtra, "key",
                        String.class, true);
                    if (key.equals(keyName)) {
                        // Found the key, now change the annotation of the
                        // return of getExtra()
                        return hostChangeMethodReturn(origResult,
                            iExtra);
                    }
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
        Set<FlowPermission> annotatedSources = new HashSet<FlowPermission>(
            AnnotationUtils.getElementValueEnumArray(
                iExtra, "source", FlowPermission.class,true));
        Set<FlowPermission> annotatedSinks = new HashSet<FlowPermission>(
            AnnotationUtils.getElementValueEnumArray(iExtra, "sink", 
                FlowPermission.class, true));

        AnnotationMirror sourceAnnotation = createAnnoFromSource(annotatedSources);
        AnnotationMirror sinkAnnotation = createAnnoFromSink(annotatedSinks);
        origResult.first.getReturnType().clearAnnotations();
        origResult.first.getReturnType().addAnnotation(sourceAnnotation);
        origResult.first.getReturnType().addAnnotation(sinkAnnotation);

        if (!origResult.first.getReturnType().hasAnnotation(
                IntentExtras.class)) {
            origResult.first.getReturnType().addAnnotation(
                EMPTYINTENTEXTRAS);
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

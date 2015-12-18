package sparta.checkers.intents;

import static org.checkerframework.framework.qual.DefaultLocation.LOCAL_VARIABLE;
import static org.checkerframework.framework.qual.DefaultLocation.OTHERWISE;
import static org.checkerframework.framework.qual.DefaultLocation.RESOURCE_VARIABLE;
import static org.checkerframework.framework.qual.DefaultLocation.UPPER_BOUNDS;

import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.InternalUtils;
import org.checkerframework.javacutil.Pair;
import org.checkerframework.javacutil.TreeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import org.checkerframework.common.aliasing.AliasingChecker;
import org.checkerframework.common.aliasing.qual.Unique;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.value.ValueChecker;
import org.checkerframework.common.value.qual.StringVal;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.qual.DefaultLocation;
import org.checkerframework.framework.qual.PolyAll;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedIntersectionType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedPrimitiveType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedTypeVariable;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedUnionType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedWildcardType;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.framework.type.visitor.AnnotatedTypeScanner;
import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.framework.util.AnnotationFormatter;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.framework.util.defaults.QualifierDefaults;

import sparta.checkers.Flow;
import sparta.checkers.FlowAnnotatedTypeFactory;
import sparta.checkers.FlowChecker;
import sparta.checkers.quals.Extra;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.IntentMap;
import sparta.checkers.quals.IntentMapBottom;
import sparta.checkers.quals.IntentMapNew;
import sparta.checkers.quals.PFPermission;
import sparta.checkers.quals.PolyIntentMap;
import sparta.checkers.quals.ReceiveIntent;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.tree.JCTree;

public class IntentAnnotatedTypeFactory extends FlowAnnotatedTypeFactory {

    protected final AnnotationMirror INTENT_MAP, IEXTRA, TOP_INTENT_MAP,
            BOTTOM_INTENT_MAP, HOST_IEXTRA_BOTTOM, HOST_IEXTRA_TOP,
            POLY_INTENT_MAP;
    protected final ExecutableElement getIntent;
    protected final ExecutableElement setIntent;
    protected final AnnotatedTypeFactory aliasingATF;
    protected final ComponentMap componentMap;
    private final HostSetTypeVisitor hostSetTypeVisitor;
    private final PFPermission EXTRA_DEFAULT;

    public IntentAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        // Must call super.initChecker before the lint option can be checked.
        final String ipArg = checker
                .getOption(ComponentMap.COMPONENT_MAP_FILE_OPTION);
        aliasingATF = checker.getTypeFactoryOfSubchecker(AliasingChecker.class);
        componentMap = new ComponentMap(ipArg);
        checkForRefinementCM();
        this.hostSetTypeVisitor = new HostSetTypeVisitor();
        getIntent = TreeUtils.getMethod("android.app.Activity", "getIntent", 0, processingEnv);
        setIntent = TreeUtils.getMethod("android.app.Activity", "setIntent", 1, processingEnv);

        INTENT_MAP = AnnotationUtils.fromClass(elements, IntentMap.class);
        IEXTRA = AnnotationUtils.fromClass(elements, Extra.class);
        HOST_IEXTRA_BOTTOM = IntentUtils.createIExtraAnno("",
                Flow.getSources(NOSOURCE), Flow.getSinks(ANYSINK),
                getProcessingEnv());
        HOST_IEXTRA_TOP = IntentUtils.createIExtraAnno("",
                Flow.getSources(ANYSOURCE), Flow.getSinks(NOSINK),
                getProcessingEnv());
        TOP_INTENT_MAP = createTopIntentMap(); // top
        BOTTOM_INTENT_MAP = AnnotationUtils.fromClass(elements, IntentMapBottom.class); // bottom
        POLY_INTENT_MAP = AnnotationUtils.fromClass(elements, PolyIntentMap.class);

        EXTRA_DEFAULT = new PFPermission(FlowPermission.EXTRA_DEFAULT);

        if (this.getClass().equals(IntentAnnotatedTypeFactory.class)) {
            this.postInit();
        }
    }

    @Override
    protected AnnotationFormatter createAnnotationFormatter() {
        if (checker.hasOption(FlowChecker.PRETTY_PRINT_OPTION)) {
            return new IntentAnnotationFormatter();
        }
        return super.createAnnotationFormatter();
    }

    /**
     * This method is used to immediately stop the analysis and ask the user to
     * refine the component map, when necessary.
     */
    
    private void checkForRefinementCM() {
        if (componentMap != null && componentMap.linesToUpdate != null && 
                !componentMap.linesToUpdate.isEmpty()) {
            String updateMsg = "Please update the following lines in the " +
                "component map:" + "\n";
            for (String lineToUpdate : componentMap.linesToUpdate) {
                updateMsg += lineToUpdate + "\n";
            }
            checker.userErrorAbort(updateMsg);
        }
    }

    protected AnnotationMirror createTopIntentMap() {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
                IntentMap.class);
        return builder.build();
    }

    /**
     * This method changes @Source(INTENT) and @Sink(INTENT) to
     * 
     * @Source(ANY), @Sink({}) and changes the @Source and @Sink from the return
     *               type of getExtra calls. This is necessary because the stub
     *               files will maintain @Source(INTENT) and
     * @Sink(INTENT) annotations to perform flow check analysis without the
     *               intent analysis.
     */
    @Override
    public Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> methodFromUse(
            MethodInvocationTree tree) {
        Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> mfuPair = super
                .methodFromUse(tree);
        if (IntentUtils.isGetExtra(tree, this)) {
            // Modifying type of getExtra call
            changeMethodReturnType(tree, mfuPair);
            // Modifying @Source and @Sink types for parameters
            setTypeToTop(mfuPair.first.getParameterTypes());
        } else if (IntentUtils.isPutExtra(tree, this)) {
            // Modifying @Source and @Sink types for parameters
            changeParametersToIntentMapKeyType(tree, mfuPair);
        } else if (IntentUtils.isSetIntentFilter(tree, this)) {
            setTypeToTop(mfuPair.first.getParameterTypes());
        } else if (IntentUtils.isGetIntentFilter(tree, this)) {
            hostSetTypeVisitor.visit(mfuPair.first.getReturnType(),
                    HOST_IEXTRA_BOTTOM);
        }
        return mfuPair;

    }

    @Override
    public CFTransfer createFlowTransferFunction(
            CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        return new IntentTransfer(analysis);
    }

    /**
     * Changes the parameters to TOP in the host type system.
     *
     * For the Flow Checker: This method changes the @Source(INTENT) and
     * @Sink(INTENT) to
     *
     * @Source(ANY) and @Sink({})
     * @param parametersAnnotations
     */

    private void setTypeToTop(List<AnnotatedTypeMirror> parametersATMs) {
        for (AnnotatedTypeMirror parameterATM : parametersATMs) {
            hostSetTypeVisitor.visit(parameterATM, HOST_IEXTRA_TOP);
        }
    }

    /**
     * Changes the parameters to the type of the key in the @IntentMap.
     * Keys that cannot be resolved are set to Bottom.
     * 
     * @param parametersAnnotations
     */

    private void changeParametersToIntentMapKeyType(MethodInvocationTree tree,
            Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> origResult) {
        ExpressionTree receiver = TreeUtils.getReceiverTree(tree);
        AnnotatedTypeMirror receiverType = getAnnotatedType(receiver);
        AnnotatedTypeMirror receiverAliasingType = aliasingATF.
                getAnnotatedType(receiver);
        if(origResult.first.getParameterTypes().size() < 2) {
            throw new RuntimeException("tree must be a putExtra");
        }
        //Replacing annotations for the key parameter
        AnnotatedTypeMirror keyATM = origResult.first.getParameterTypes().get(0);
        //Setting key host type to top
        hostSetTypeVisitor.visit(keyATM, HOST_IEXTRA_TOP);

        //Replacing annotations for the value parameter
        AnnotatedTypeMirror valueATM = origResult.first.getParameterTypes().get(1);
        if (IntentUtils.isIntentMapBottom(receiverType)) {
            //Setting value host type to bottom.
            hostSetTypeVisitor.visit(valueATM, HOST_IEXTRA_BOTTOM);
        } else if (IntentUtils.isIntentMapNew(receiverType)) {
            //Setting value host type to top.
            hostSetTypeVisitor.visit(valueATM, HOST_IEXTRA_TOP);
        } else if (IntentUtils.isIntentMap(receiverType)) {
            AnnotationMirror iExtraLUB = getLUBIExtraFromPutExtraOrGetExtra(
                    tree, receiverType);
            if (iExtraLUB != null) {
                //Setting the value host type to the host type of iExtraLUB.
                hostSetTypeVisitor.visit(valueATM, iExtraLUB);
            } else if (!receiverAliasingType.hasAnnotation(Unique.class)) {
                // @Extra with that key not found and receiver is not @Unique.
                // Set it to bottom.
                hostSetTypeVisitor.visit(valueATM, HOST_IEXTRA_BOTTOM);
            } else {
                // @Extra with that key not found, but receiver is unique,
                // so the value may be anything. So, the parameter is set
                // to top so it can be refined later.
                hostSetTypeVisitor.visit(valueATM, HOST_IEXTRA_TOP);
            }
        }
    }

    private AnnotationMirror getLUBIExtraFromPutExtraOrGetExtra(
            MethodInvocationTree tree, AnnotatedTypeMirror receiverType) {
        List<String> keys = getKeysFromPutExtraOrGetExtraCall(tree);
        AnnotationMirror iExtraLUB = null;
        for(String key : keys){
            AnnotationMirror iExtra = IntentUtils.getIExtra(receiverType, key);
            // If the key in a getExtra call could have more than one literal
            // value, then the return type of the call to getExtra should be the
            // LUB of all the types the keys map to in @IntentMap.
            if (iExtra != null) {
                if(iExtraLUB == null) {
                    iExtraLUB = iExtra;
                } else {
                    iExtraLUB = qualHierarchy.leastUpperBound(iExtraLUB, iExtra);
                }
            }
        }
        return iExtraLUB;
    }

    @Override
    protected QualifierDefaults createQualifierDefaults() {
        QualifierDefaults defaults = super.createQualifierDefaults();
        DefaultLocation[] topLocations = {LOCAL_VARIABLE, RESOURCE_VARIABLE, UPPER_BOUNDS};
        
        defaults.addCheckedCodeDefaults(TOP_INTENT_MAP, topLocations);
        defaults.addCheckedCodeDefault(TOP_INTENT_MAP, OTHERWISE);

        return defaults;
    }

    @Override
    protected ListTreeAnnotator createTreeAnnotator() {
        ImplicitsTreeAnnotator implicits = getFlowCheckerImplicits();
        implicits.addTreeKind(Tree.Kind.NULL_LITERAL, BOTTOM_INTENT_MAP);
        implicits.addTreeKind(Tree.Kind.INT_LITERAL, TOP_INTENT_MAP);
        implicits.addTreeKind(Tree.Kind.LONG_LITERAL, TOP_INTENT_MAP);
        implicits.addTreeKind(Tree.Kind.FLOAT_LITERAL, TOP_INTENT_MAP);
        implicits.addTreeKind(Tree.Kind.DOUBLE_LITERAL, TOP_INTENT_MAP);
        implicits.addTreeKind(Tree.Kind.BOOLEAN_LITERAL, TOP_INTENT_MAP);
        implicits.addTreeKind(Tree.Kind.CHAR_LITERAL, TOP_INTENT_MAP);
        implicits.addTreeKind(Tree.Kind.STRING_LITERAL, TOP_INTENT_MAP);

        return new ListTreeAnnotator(
                new IntentTreeAnnotator(this),
                new PropagationTreeAnnotator(this),
                implicits
        );
    }


    protected class IntentTreeAnnotator extends FlowPolicyTreeAnnotator {

        public IntentTreeAnnotator(FlowAnnotatedTypeFactory atypeFactory) {
            super(atypeFactory);
        }

        @Override
        public Void defaultAction(Tree tree, AnnotatedTypeMirror type) {
            completeIExtraFlows(tree, type);
            return super.defaultAction(tree, type);
        }
        /**
         * This method replaces occurrences of the permission
         * <code>EXTRA_DEFAULT</code> in the source or sink of an @Extra according
         * to the flow policy. E.g. @Extra(key="k", sink="INTERNET") and flow
         * policy: FILESYSTEM -> INTERNET. The @Extra is updated to @Extra(key="k",
         * source="FILESYSTEM", sink="INTERNET"). This does not work for intent
         * types that are parameters of a @ReceiveIntent method. For those cases,
         * both a source and a sink must be written explicitly.
         * 
         * @see sparta.checkers.intents.IntentVisitor#checkReceiveIntentDefaulting.
         */
        private void completeIExtraFlows(Tree tree, AnnotatedTypeMirror type) {
            // Do not apply defaulting for parameters of @ReceiveIntent methods
            if (tree instanceof VariableTree) {
                Element elt = TreeUtils.elementFromDeclaration((VariableTree) tree);
                Element parentElt = elt.getEnclosingElement();
                if (getDeclAnnotation(parentElt, ReceiveIntent.class) != null) {
                    return;
                }
            }

            completeFlowsInIExtra(type);
        }
    }
    @Override
    protected TypeAnnotator createTypeAnnotator() {
        return new IntentTypeAnnotator(this);
    }

    protected class IntentTypeAnnotator extends FlowPolicyTypeAnnotator {
        public IntentTypeAnnotator(FlowAnnotatedTypeFactory factory) {
            super(factory);
        }

        @Override
        public Void visitExecutable(AnnotatedExecutableType t, Void p) {
            if (getDeclAnnotation(t.getElement(), ReceiveIntent.class) != null) {
                // Don't complete flow in @Extra for ReceiveIntent methods.
                // Can't call super.visitExecutable because it would complete
                // the flows in this method, but the @Source and @Sink flow
                // completion should still happen.
                completePolicyFlows(t.getReturnType());
                completePolicyFlows(t.getReceiverType());
                for (AnnotatedTypeMirror anno : t.getParameterTypes()) {
                    completePolicyFlows(anno);
                }
                for (AnnotatedTypeMirror anno : t.getThrownTypes()) {
                    completePolicyFlows(anno);
                }
                for (AnnotatedTypeMirror anno : t.getTypeVariables()) {
                    completePolicyFlows(anno);
                }

                return null;
            }
            completeFlowsInIExtra(t);
            return super.visitExecutable(t, p);
        }

        @Override
        public Void visitArray(AnnotatedArrayType type, Void p) {
            completeFlowsInIExtra(type);
            return super.visitArray(type, p);
        }
        @Override
        public Void visitDeclared(AnnotatedDeclaredType type, Void p) {
            completeFlowsInIExtra(type);
            return super.visitDeclared(type, p);
        }
       
        @Override
        public Void visitIntersection(AnnotatedIntersectionType type, Void p) {
            completeFlowsInIExtra(type);
            return super.visitIntersection(type, p);
        }

        @Override
        public Void visitPrimitive(AnnotatedPrimitiveType type, Void p) {
            completeFlowsInIExtra(type);
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
            completeFlowsInIExtra(type);
            return r;
        }
        @Override
        public Void visitUnion(AnnotatedUnionType type, Void p) {
            completeFlowsInIExtra(type);
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
            completeFlowsInIExtra(type);
            return r;
        }
    }


    private void completeFlowsInIExtra(AnnotatedTypeMirror type) {
        // Apply defaulting for other cases.
        AnnotationMirror anno = type.getAnnotation(IntentMap.class);
        if (anno != null) {
            List<AnnotationMirror> iExtras = IntentUtils.getIExtras(anno);
            for (AnnotationMirror iExtra : iExtras) {
                Set<PFPermission> sources = IntentUtils.getSourcesPFP(iExtra);
                Set<PFPermission> sinks = IntentUtils.getSinksPFP(iExtra);
                if (sinks.contains(EXTRA_DEFAULT)
                        && sources.contains(EXTRA_DEFAULT)) {
                    anno = IntentUtils.getNewIMapWithExtra(anno,
                            IntentUtils.getKeyName(iExtra),
                            Flow.getSources(NOSOURCE), Flow.getSinks(ANYSINK),
                            processingEnv);
                } else if (sinks.contains(EXTRA_DEFAULT)) {
                    Set<PFPermission> newSink = getFlowPolicy()
                            .getIntersectionAllowedSinks(sources);
                    anno = IntentUtils.getNewIMapWithExtra(anno,
                            IntentUtils.getKeyName(iExtra), sources, newSink,
                            processingEnv);
                } else if (sources.contains(EXTRA_DEFAULT)) {
                    Set<PFPermission> newSource = getFlowPolicy()
                            .getIntersectionAllowedSources(sinks);
                    anno = IntentUtils.getNewIMapWithExtra(anno,
                            IntentUtils.getKeyName(iExtra), newSource, sinks,
                            processingEnv);
                }
            }
            type.replaceAnnotation(anno);
        }
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
            newBottoms.add(BOTTOM_INTENT_MAP);
            return newBottoms;
        }

        @Override
        protected Set<AnnotationMirror> findTops(
                Map<AnnotationMirror, Set<AnnotationMirror>> supertypes) {
            Set<AnnotationMirror> newTops = super.findTops(supertypes);
            newTops.add(TOP_INTENT_MAP);
            return newTops;
        }

        private boolean isIntentMapQualifier(AnnotationMirror anno) {
            return AnnotationUtils.areSameByClass(anno, IntentMap.class) ||
                    isIntentMapBottomQualifier(anno) ||
                    isIntentMapNewQualifier(anno) ||
                    isPolyIntentMapQualifier(anno);
        }

        private boolean isIExtraQualifier(AnnotationMirror anno) {
            return AnnotationUtils.areSameByClass(anno, Extra.class);
        }

        private boolean isIntentMapBottomQualifier(AnnotationMirror anno) {
            return AnnotationUtils.areSameByClass(anno, IntentMapBottom.class);
        }

        private boolean isIntentMapNewQualifier(AnnotationMirror anno) {
            return AnnotationUtils.areSameByClass(anno, IntentMapNew.class);
        }

        private boolean isPolyIntentMapQualifier(AnnotationMirror anno) {
            return AnnotationUtils.areSameByClass(anno, PolyIntentMap.class) ||
                    AnnotationUtils.areSameByClass(anno, PolyAll.class);
        }
        @Override
        public AnnotationMirror getTopAnnotation(AnnotationMirror start) {
            if (isIntentMapQualifier(start)) {
                return TOP_INTENT_MAP;
            } else if (isIExtraQualifier(start)) {
                return IEXTRA; // What to do here?
            } else {
                return super.getTopAnnotation(start);
            }
        }

        @Override
        public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
            if (isPolyIntentMapQualifier(rhs) && isPolyIntentMapQualifier(lhs)) {
                return true;
            } else if (isPolyIntentMapQualifier(lhs) && isIntentMapQualifier(rhs)) {
                // If lhs is poly, only bottom is a subtype.
                return isIntentMapBottomQualifier(rhs);
            } else if (isPolyIntentMapQualifier(rhs) && isIntentMapQualifier(lhs)) {
                // if rhs is poly, only top is a supertype
                return AnnotationUtils.areSame(lhs, TOP_INTENT_MAP);
            } else if (isIntentMapNewQualifier(rhs) && isIntentMapQualifier(lhs)) {
                return true;
            } else if (isIntentMapBottomQualifier(rhs) && isIntentMapQualifier(lhs)) {
                return true;
            } else if (isIntentMapBottomQualifier(lhs) && isIntentMapQualifier(rhs)) {
                return false;
            } else if (isIntentMapNewQualifier(lhs) && isIntentMapQualifier(rhs)) {
                return false;
            } else if (isIExtraQualifier(rhs)) {
                if (rhs == null || lhs == null || !isIExtraQualifier(lhs)) {
                    return false;
                }
                Set<PFPermission> lhsAnnotatedSources = IntentUtils
                        .getSourcesPFP(lhs);
                Set<PFPermission> lhsAnnotatedSinks = IntentUtils
                        .getSinksPFP(lhs);
                Set<PFPermission> rhsAnnotatedSources = IntentUtils
                        .getSourcesPFP(rhs);
                Set<PFPermission> rhsAnnotatedSinks = IntentUtils
                        .getSinksPFP(rhs);
                
                AnnotationMirror lhsSinks = createAnnoFromSink(lhsAnnotatedSinks);
                AnnotationMirror rhsSinks = createAnnoFromSink(rhsAnnotatedSinks);
                AnnotationMirror lhsSources = createAnnoFromSource(lhsAnnotatedSources);
                AnnotationMirror rhsSources = createAnnoFromSource(rhsAnnotatedSources);
                return isSubtype(rhsSources, lhsSources) && isSubtype(rhsSinks, lhsSinks);                
                
            } else if (isIntentMapQualifier(rhs)) {
                if (rhs == null || lhs == null || !isIntentMapQualifier(lhs)) {
                    return false;
                }
                List<AnnotationMirror> lhsIExtrasList = IntentUtils
                        .getIExtras(lhs);
                if (lhsIExtrasList.isEmpty()) {
                    return true;
                }
                for (AnnotationMirror lhsIExtra : lhsIExtrasList) {
                    String leftKey = IntentUtils.getKeyName(lhsIExtra);

                    if (IntentUtils.getIExtra(rhs, leftKey) != null) {
                        if (!hostIsExactType(rhs, lhsIExtra, leftKey)) {
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

        @Override
        protected void addPolyRelations(QualifierHierarchy qualHierarchy,
                Map<AnnotationMirror, Set<AnnotationMirror>> fullMap,
                Map<AnnotationMirror, AnnotationMirror> polyQualifiers,
                Set<AnnotationMirror> tops, Set<AnnotationMirror> bottoms) {
            AnnotationUtils.updateMappingToImmutableSet(fullMap, BOTTOM_INTENT_MAP,
                    Collections.singleton(POLYALL));
            AnnotationUtils.updateMappingToImmutableSet(fullMap, BOTTOM_INTENT_MAP,
                    Collections.singleton(POLY_INTENT_MAP));
            Set<AnnotationMirror> polyallTops = AnnotationUtils.createAnnotationSet();
            polyallTops.add(TOP_INTENT_MAP);
            AnnotationUtils.updateMappingToImmutableSet(fullMap, POLYALL, polyallTops);
            AnnotationUtils.updateMappingToImmutableSet(fullMap, POLY_INTENT_MAP,
                    Collections.singleton(TOP_INTENT_MAP));
            super.addPolyRelations(qualHierarchy, fullMap, polyQualifiers, tops, bottoms);
        }

        /**
         * temporary auxiliary method used to check whether the types of the
         * keys are the same. rule for information flow analysis on intents.
         * 
         * @param rhs
         * @param lhsIExtra
         * @param leftKey
         * @return
         */
        private boolean hostIsExactType(AnnotationMirror rhs,
                AnnotationMirror lhsIExtra, String leftKey) {
            AnnotationMirror rhsIExtra = IntentUtils.getIExtra(rhs, leftKey);

            Set<FlowPermission> lhsAnnotatedSources = IntentUtils
                    .getSources(lhsIExtra);
            Set<FlowPermission> lhsAnnotatedSinks = IntentUtils
                    .getSinks(lhsIExtra);
            Set<FlowPermission> rhsAnnotatedSources = IntentUtils
                    .getSources(rhsIExtra);
            Set<FlowPermission> rhsAnnotatedSinks = IntentUtils
                    .getSinks(rhsIExtra);

            return lhsAnnotatedSources.equals(rhsAnnotatedSources)
                    && lhsAnnotatedSinks.equals(rhsAnnotatedSinks);
        }

        /**
         * The LUB between 2 @IntentMap is an @IntentMap containing all the @Extra
         * with keys both have in common. If the types differ for the @Extra with
         * same key in both @IntentMap, the @Extra with that key is not in the LUB.
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
                if (AnnotationUtils.areSameIgnoringValues(a1, INTENT_MAP)) {
                    List<AnnotationMirror> a1IExtrasList = IntentUtils
                            .getIExtras(a1);
                    List<AnnotationMirror> IExtraOutputSet = new ArrayList<AnnotationMirror>();

                    for (AnnotationMirror a1IExtra : a1IExtrasList) {
                        String a1IExtraKey = IntentUtils.getKeyName(a1IExtra);
                        AnnotationMirror a2IExtra = IntentUtils.getIExtra(
                                a2, a1IExtraKey);
                        if (a2IExtra != null) {
                            // Here we have found matching keys.
                            // Only add to the LUB if they have matching types.
                            if (hostKeysMapToSameType(a1IExtra, a1IExtraKey,
                                    a2IExtra)) {
                                IExtraOutputSet.add(a1IExtra);
                            }
                        }

                    }
                    AnnotationMirror output = IntentUtils
                            .addIExtrasToIntentExtras(TOP_INTENT_MAP,
                                    IExtraOutputSet, processingEnv);
                    return output;
                } else if (AnnotationUtils.areSameIgnoringValues(a1, IEXTRA)) {
                    if (a1 == null || a2 == null || !isIExtraQualifier(a2)) {
                        return super.leastUpperBound(a1, a2);
                    }
                    Set<PFPermission> lhsAnnotatedSources = IntentUtils
                            .getSourcesPFP(a1);
                    Set<PFPermission> lhsAnnotatedSinks = IntentUtils
                            .getSinksPFP(a1);
                    Set<PFPermission> rhsAnnotatedSources = IntentUtils
                            .getSourcesPFP(a2);
                    Set<PFPermission> rhsAnnotatedSinks = IntentUtils
                            .getSinksPFP(a2);

                    AnnotationMirror lhsSinks = createAnnoFromSink(lhsAnnotatedSinks);
                    AnnotationMirror rhsSinks = createAnnoFromSink(rhsAnnotatedSinks);
                    AnnotationMirror lhsSources = createAnnoFromSource(lhsAnnotatedSources);
                    AnnotationMirror rhsSources = createAnnoFromSource(rhsAnnotatedSources);
                    return IntentUtils.createIExtraAnno("",
                            Flow.getSources(leastUpperBound(rhsSources,
                                    lhsSources)), Flow
                                    .getSinks(leastUpperBound(rhsSinks,
                                            lhsSinks)), processingEnv);
                }
                    
            } else if ((AnnotationUtils.areSameIgnoringValues(a1, POLY_INTENT_MAP) &&
                    AnnotationUtils.areSameIgnoringValues(a2, INTENT_MAP)) ||
                    (AnnotationUtils.areSameIgnoringValues(a2, POLY_INTENT_MAP) &&
                    AnnotationUtils.areSameIgnoringValues(a1, INTENT_MAP))) {
                return TOP_INTENT_MAP;
            }

            return super.leastUpperBound(a1, a2);
        }

        /**
         * The GLB between 2 @IntentMap will contain the union of keys that
         * these annotations contain and the respective type. If the types
         * differ for the @Extra with same key in both @IntentMap, the GLB is
         * bottom.
         */

        @Override
        public AnnotationMirror greatestLowerBound(AnnotationMirror a1,
                AnnotationMirror a2) {
            if (AnnotationUtils.areSame(a1, a2))
                return a1;

            if (AnnotationUtils.areSameIgnoringValues(a1, BOTTOM_INTENT_MAP) 
                    || AnnotationUtils.areSameIgnoringValues(a2, BOTTOM_INTENT_MAP)) {
                return BOTTOM_INTENT_MAP;
            }

            if (AnnotationUtils.areSameIgnoringValues(a1, a2)) {
                if (AnnotationUtils.areSameIgnoringValues(a1, INTENT_MAP)) {
                    List<AnnotationMirror> a1IExtrasList = IntentUtils
                            .getIExtras(a1);
                    List<AnnotationMirror> a2IExtrasList = IntentUtils
                            .getIExtras(a2);
                    List<AnnotationMirror> IExtraOutputSet = new ArrayList<AnnotationMirror>();
                    for (AnnotationMirror a1IExtra : a1IExtrasList) {
                        String a1IExtraKey = IntentUtils.getKeyName(a1IExtra);
                        AnnotationMirror a2IExtra = IntentUtils.getIExtra(
                                a2, a1IExtraKey);
                        if (a2IExtra != null) {
                            // If we have found matching keys:
                            // If the the values with same key have different types, return bottom.
                            if (!hostKeysMapToSameType(a1IExtra, a1IExtraKey, a2IExtra)) {
                                return BOTTOM_INTENT_MAP;
                            } else {
                                //Else add this IExtra in the results
                                IExtraOutputSet.add(a1IExtra);
                            }
                        } else {
                            // If we could not find the key in a2, add the
                            // @Extra
                            // with this key to the resulting @IntentMap
                            IExtraOutputSet.add(a1IExtra);
                        }

                    }
                    // Now we need to fill the resulting set with @Extra
                    // containing keys
                    // that are in a2 but not in a1.
                    for (AnnotationMirror a2IExtra : a2IExtrasList) {
                        String a2IExtraKey = IntentUtils.getKeyName(a2IExtra);
                        if (IntentUtils.getIExtra(a1, a2IExtraKey) == null) {
                            IExtraOutputSet.add(a2IExtra);
                        }
                    }

                    AnnotationMirror output = IntentUtils
                            .addIExtrasToIntentExtras(TOP_INTENT_MAP,
                                    IExtraOutputSet, processingEnv);
                    return output;
                } else if (AnnotationUtils.areSameIgnoringValues(a1, IEXTRA)) {
                    //Bottom IExtra?
                }
            } else if ((AnnotationUtils.areSameIgnoringValues(a1, POLY_INTENT_MAP) &&
                    AnnotationUtils.areSameIgnoringValues(a2, INTENT_MAP)) ||
                    (AnnotationUtils.areSameIgnoringValues(a2, POLY_INTENT_MAP) &&
                    AnnotationUtils.areSameIgnoringValues(a1, INTENT_MAP))) {
                return BOTTOM_INTENT_MAP;
            }

            return super.greatestLowerBound(a1, a2);
        }

        /**
         * temporary auxiliary method that return true if a certain key maps
         * to the same type for two different @Extra.
         */

        private boolean hostKeysMapToSameType(
                AnnotationMirror a1IExtra, String a1IExtraKey,
                AnnotationMirror a2IExtra) {
            Set<PFPermission> a1Sources = IntentUtils.
                    getSourcesPFP(a1IExtra);
            Set<PFPermission> a2Sources = IntentUtils.
                    getSourcesPFP(a1IExtra);
            Set<PFPermission> a1Sinks = IntentUtils.
                    getSinksPFP(a1IExtra);
            Set<PFPermission> a2Sinks = IntentUtils.
                    getSinksPFP(a2IExtra);
            return a1Sources.equals(a2Sources) && a1Sinks.equals(a2Sinks);
        }

    }

    /**
     * Changes the return type of a
     * <code>Intent.getExtra(key)</code> call depending on the @IntentMap type
     * of Intent and <code>key</code>.
     * 
     * @param tree
     *            The method tree
     * @param origResult
     *            The original result
     */

    public void changeMethodReturnType(MethodInvocationTree tree,
            Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> origResult) {
        ExpressionTree receiver = TreeUtils.getReceiverTree(tree);
        AnnotatedTypeMirror receiverType = getAnnotatedType(receiver);
        AnnotationMirror iExtra = getLUBIExtraFromPutExtraOrGetExtra(tree,
                receiverType);
        if (iExtra == null) {
            iExtra = HOST_IEXTRA_BOTTOM; // Couldn't find key, set to bottom.
        }
        // Calling the visitor that will change the method return type.
        hostSetTypeVisitor.visit(origResult.first.getReturnType(), iExtra);
    }

/**
 * 
 * @param tree MethodInvocationTree of putExtra or getExtra
 * @return a list of strings containing the key or possible keys in this call.  T
 * The empty list means that the keys could not be resolved at compile time.
 */
    public  List<String> getKeysFromPutExtraOrGetExtraCall(MethodInvocationTree tree) {
        List<String> keys = new ArrayList<>();
        //If consant prop. is turned on, get the stringval anno
        AnnotationMirror stringValAnno = getValueATF().getAnnotationMirror(tree
                .getArguments().get(0), StringVal.class);
        if (stringValAnno != null) {
            keys = AnnotationUtils.getElementValueArray(stringValAnno, "value",
                    String.class, true);
        } else {
            //Either const prop is turned off, the value is unknown
            //If const prop is turned off, we still get the value of
            //literal strings and public static finals.
            keys = new ArrayList<>();
            ExpressionTree t = tree.getArguments().get(0);
            if (t instanceof JCTree.JCLiteral) {
                String k = t.toString();
                keys.add(k.substring(1, k.length()-1));
            } else {
                Element elt = InternalUtils.symbol(t);
                if (ElementUtils.isCompileTimeConstant(elt)) {
                    keys.add(((VariableElement) elt).getConstantValue()
                            .toString());
                }
            }
        }
        return keys;
    }

    /**
     * Visitor that replaces the type of an ATM based on the content of the
     * @Extra passed as argument.
     *
     */
    private class HostSetTypeVisitor extends AnnotatedTypeScanner<Void,
            AnnotationMirror> {
        @Override
        protected Void scan(AnnotatedTypeMirror type, AnnotationMirror iExtra) {
            if (type != null && iExtra != null) {
                hostSetType(type, iExtra);
            }
            return super.scan(type, iExtra);
        }

        /**
         * Sets the source and sink type of <code>iExtraAM</code> to be the source
         * and sink type of <code>atm</code>.
         * @param atm
         * @param iExtraAM
         */
        private void hostSetType(AnnotatedTypeMirror atm,
                AnnotationMirror iExtraAM) {
            Set<PFPermission> annotatedSources = IntentUtils
                    .getSourcesPFP(iExtraAM);
            Set<PFPermission> annotatedSinks = IntentUtils
                    .getSinksPFP(iExtraAM);
            AnnotationMirror sourceAnnotation = createAnnoFromSource(annotatedSources);
            AnnotationMirror sinkAnnotation = createAnnoFromSink(annotatedSinks);
            atm.replaceAnnotation(sourceAnnotation);
            atm.replaceAnnotation(sinkAnnotation);
        }
    }

    public ComponentMap getComponentMap() {
        return componentMap;
    }

    // TODO: CFAnalysis must have the getTypeFactoryofSubchecker method.
    // Called by IntentTransfer since CFAnalysis doesn't have it.
    public AnnotatedTypeFactory getAliasingTypeFactory() {
        return checker.getTypeFactoryOfSubchecker(AliasingChecker.class);
    }
    public AnnotatedTypeFactory getValueATF() {
        return checker.getTypeFactoryOfSubchecker(ValueChecker.class);
    }
}

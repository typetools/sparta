package sparta.checkers;

import checkers.quals.DefaultLocation;
import checkers.types.AnnotatedTypeFactory;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.types.BasicAnnotatedTypeFactory;


import checkers.util.QualifierDefaults;
import checkers.util.QualifierDefaults.DefaultApplierElement;

import javacutils.AnnotationUtils;
import javacutils.ElementUtils;
import javacutils.InternalUtils;
import javacutils.Pair;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.PolyFlow;
import sparta.checkers.quals.PolyFlowReceiver;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;

public class FlowAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<FlowChecker> {


    // List of methods that are not in a stub file
    private final Map<String, Map<String, Map<Element, Integer>>> notInStubFile;
    public FlowAnnotatedTypeFactory(FlowChecker checker, CompilationUnitTree root) {
        super(checker, root);

        // Use the bottom type as default for everything but local variables.
        defaults.addAbsoluteDefault(checker.LITERALSOURCE, DefaultLocation.OTHERWISE);
        // Use the top type for local variables and let flow refine the type.
        defaults.addAbsoluteDefault(checker.ANYSOURCE, DefaultLocation.LOCAL_VARIABLE);

        // Default is LITERAL -> (ALL MAPPED SINKS) for everything but local
        // variables.
        defaults.addAbsoluteDefault(checker.FROMLITERALSINK, DefaultLocation.OTHERWISE);
        // Use the top type for local variables and let flow refine the type.
        defaults.addAbsoluteDefault(checker.NOSINK, DefaultLocation.LOCAL_VARIABLE);

        // Top Type for Receivers
        defaults.addAbsoluteDefault(checker.NOSINK, DefaultLocation.RECEIVERS);
        defaults.addAbsoluteDefault(checker.ANYSOURCE, DefaultLocation.RECEIVERS);

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
    protected QualifierDefaults createQualifierDefaults() {
        return new FlowCompletingDefaults(elements, this);
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
                // Only apply these annotations if this method has not been
                // marked as not reviewed.
                if (!reviewed) {
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

    class FlowCompletingDefaults extends QualifierDefaults {

        // Instantiated lazily
        private HashSet<FlowPermission> sinksFromAny = null;
        private HashSet<FlowPermission> sourcesToAny = null;

        public FlowCompletingDefaults(Elements elements, AnnotatedTypeFactory atypeFactory) {
            super(elements, atypeFactory);
        }

        protected void completePolicyFlows(final Element element, final AnnotatedTypeMirror type) {

            if (type == null || type.getKind() == TypeKind.NONE || type.getKind() == TypeKind.NULL) {
                return;
            }

            boolean isLocal = (element == null || element.getKind() == ElementKind.LOCAL_VARIABLE);


            if (!isLocal /*&& element != null*/
                    && (element.getKind() == ElementKind.METHOD || element.getKind() == ElementKind.CONSTRUCTOR)
                    && type.getKind() == TypeKind.EXECUTABLE) {
                final AnnotatedExecutableType exeType = (AnnotatedExecutableType) type;
                for (final AnnotatedTypeMirror atm : exeType.getParameterTypes()) {
                    completePolicyFlows(false, atm);
                }

                completePolicyFlows(false, exeType.getReturnType());
                completePolicyFlows(false, exeType.getReceiverType());

            } else {
                if (type instanceof AnnotatedTypeMirror.AnnotatedDeclaredType) {
                    AnnotatedDeclaredType dec = ((AnnotatedTypeMirror.AnnotatedDeclaredType) type);
                    List<AnnotatedTypeMirror> typArgs = dec.getTypeArguments();
                    if (typArgs != null) {
                        for (final AnnotatedTypeMirror atm : typArgs ) {
                            completePolicyFlows(false, atm);
                        }
                    }
                }
                completePolicyFlows(isLocal, type);
            }
        }

        private void completePolicyFlows(final boolean isLocal, final AnnotatedTypeMirror type) {
            if (type.getKind() == TypeKind.ARRAY) {
                final Pair<Set<AnnotationMirror>, Set<AnnotationMirror>> arrayToComponentAnnos = getArrayAnnos(
                        (AnnotatedTypeMirror.AnnotatedArrayType) type, isLocal);

                completePolicyFlows(type, arrayToComponentAnnos.first);
                completePolicyFlows(
                        ((AnnotatedTypeMirror.AnnotatedArrayType) type).getComponentType(),
                        arrayToComponentAnnos.second);
            } else {
                if (type instanceof AnnotatedTypeMirror.AnnotatedDeclaredType) {
                    AnnotatedDeclaredType dec = ((AnnotatedTypeMirror.AnnotatedDeclaredType) type);
                    List<AnnotatedTypeMirror> typArgs = dec.getTypeArguments();
                    if (typArgs != null) {
                        for (final AnnotatedTypeMirror atm : typArgs ) {
                            completePolicyFlows(false, atm);
                        }
                    }
                }
           
                completePolicyFlows(type, type.getAnnotations());
            }
        }

        private Pair<Set<AnnotationMirror>, Set<AnnotationMirror>> getArrayAnnos(
                final AnnotatedTypeMirror.AnnotatedArrayType aat, boolean isLocal) {
            return Pair.of(aat.getAnnotations(), aat.getComponentType().getAnnotations());
        }

        // TODO: THIS SHOULD REALLY RETURN AN EITHER TYPE
        protected Pair<AnnotationMirror, AnnotationMirror> explicitAnnosToFlowQuals(
                final Set<AnnotationMirror> explicitAnnos) {
            AnnotationMirror flowSourceQual = null;
            AnnotationMirror flowSinkQuals = null;
            for (final AnnotationMirror am : explicitAnnos) {
                if (flowSourceQual == null
                        && AnnotationUtils.areSameIgnoringValues(am, checker.SOURCE)) {
                    flowSourceQual = am;
                } else if (flowSourceQual == null
                        && AnnotationUtils.areSameIgnoringValues(am, checker.POLYSOURCE)) {
                    flowSourceQual = am;
                } else if (flowSinkQuals == null
                        && AnnotationUtils.areSameIgnoringValues(am, checker.SINK)) {
                    flowSinkQuals = am;
                } else if (flowSinkQuals == null
                        && AnnotationUtils.areSameIgnoringValues(am, checker.POLYSINK)) {
                    flowSinkQuals = am;
                }

                if (flowSourceQual != null && flowSinkQuals != null) {
                    break;
                }
            }

            return Pair.of(flowSourceQual, flowSinkQuals);
        }

        protected Pair<Set<FlowPermission>, Set<FlowPermission>> getNewSourceOrSink(
                final Pair<AnnotationMirror, AnnotationMirror> sourceToSinkQuals) {
            final FlowPolicy flowPolicy = checker.getFlowPolicy();
            if (AnnotationUtils.areSameIgnoringValues(sourceToSinkQuals.first, checker.POLYSOURCE)
                    || AnnotationUtils.areSameIgnoringValues(sourceToSinkQuals.second,
                            checker.POLYSINK)) {
                return Pair.of(null, null);
            }
            final Set<FlowPermission> sources = FlowUtil.getSourceOrEmpty(sourceToSinkQuals.first,
                    false);
            final Set<FlowPermission> sinks = FlowUtil.getSinkOrEmpty(sourceToSinkQuals.second,
                    false);

            final Set<FlowPermission> newSink;
            final Set<FlowPermission> newSource;

            if (!sources.isEmpty() && sourceToSinkQuals.second == null) {
                newSource = null;
                newSink = flowPolicy.getIntersectingSink(sources);
                newSink.addAll(sinksFromAny);
                if (newSink.contains(FlowPermission.ANY) && newSink.size() > 1) {
                    System.out.println("Drop extra sinks");
                    newSink.clear();
                    newSink.add(FlowPermission.ANY);
                }

            } else if (sourceToSinkQuals.first == null && !sinks.isEmpty()) {
                newSource = checker.getFlowPolicy().getIntersectingSource(sinks);
                newSource.addAll(sourcesToAny);
                newSink = null;
                if (newSource.contains(FlowPermission.ANY) && newSource.size() > 1) {
                    System.out.println("Drop extra sources");
                    newSource.clear();
                    newSource.add(FlowPermission.ANY);
                }

            } else {
                newSource = null;
                newSink = null;
            }

            return Pair.of(newSource, newSink);
        }

        protected void completePolicyFlows(final AnnotatedTypeMirror type,
                final Set<AnnotationMirror> explicitAnnos) {
            if (type.getKind() == TypeKind.VOID || explicitAnnos == null || explicitAnnos.isEmpty()) {
                return;
            }

            final FlowPolicy flowPolicy = checker.getFlowPolicy();
            if (sinksFromAny == null) {
                sinksFromAny = new HashSet<FlowPermission>();
                sinksFromAny.addAll(flowPolicy.getSinkFromSource(FlowPermission.ANY, false));

                sourcesToAny = new HashSet<FlowPermission>();
                sourcesToAny.addAll(flowPolicy.getSourceFromSink(FlowPermission.ANY, false));
            }

            // Using pairs like Either -- the rest of this method would be much
            // shorter with Eithers
            Pair<AnnotationMirror, AnnotationMirror> sourceToSinkQuals = explicitAnnosToFlowQuals(explicitAnnos);

            Pair<Set<FlowPermission>, Set<FlowPermission>> newSourceOrSink = getNewSourceOrSink(sourceToSinkQuals);

            final AnnotationMirror newAnno;
            if (newSourceOrSink.first != null) {
                newAnno = FlowUtil.createAnnoFromSource(processingEnv, newSourceOrSink.first);

            } else if (newSourceOrSink.second != null) {
                newAnno = FlowUtil.createAnnoFromSink(processingEnv, newSourceOrSink.second);

            } else {
                newAnno = null;
            }

            if (newAnno != null) {
                type.replaceAnnotation(newAnno);
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

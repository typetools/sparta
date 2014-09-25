package sparta.checkers.poly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Name;
import javax.lang.model.util.Elements;

import org.checkerframework.framework.qual.PolyAll;
import org.checkerframework.framework.qual.PolymorphicQualifier;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.util.AnnotatedTypes;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ErrorReporter;
import org.checkerframework.javacutil.TreeUtils;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;

/**
 * This implementation currently only supports polymorphism for method
 * invocations, for which the return type depends on the unification of the
 * parameter/receiver types.
 *
 * Adapted from org.checkerframework.framework.util.QualifierPolymorphism
 */
public class StandardQualifierPolymorphism extends QualifierPolymorphism {

    /**
     * Creates a {@link StandardQualifierPolymorphism} instance that uses the
     * given checker for querying type qualifiers and the given factory for
     * getting annotated types.
     *
     * @param env
     *            the processing environment
     * @param factory
     *            the factory for the current checker
     */
    public StandardQualifierPolymorphism(ProcessingEnvironment env,
            AnnotatedTypeFactory factory) {
        super(env, factory);
    }

    public Map<AnnotationMirror, AnnotationMirror> getPolyQualifiers(
            Elements elements) {
        Map<AnnotationMirror, AnnotationMirror> polys = new HashMap<AnnotationMirror, AnnotationMirror>();
        for (AnnotationMirror aam : qualhierarchy.getTypeQualifiers()) {
            if (isPolyAll(aam)) {
                polys.put(null, aam);
                continue;
            }
            for (AnnotationMirror aa : aam.getAnnotationType().asElement()
                    .getAnnotationMirrors()) {
                if (aa.getAnnotationType().toString()
                        .equals(PolymorphicQualifier.class.getCanonicalName())) {
                    Name plval = AnnotationUtils.getElementValueClassName(aa,
                            "value", true);
                    AnnotationMirror ttreetop;
                    if (PolymorphicQualifier.class.getCanonicalName()
                            .contentEquals(plval)) {
                        Set<? extends AnnotationMirror> tops = qualhierarchy
                                .getTopAnnotations();
                        if (tops.size() != 1) {
                            ErrorReporter
                                    .errorAbort("QualifierPolymorphism: PolymorphicQualifier has to specify type hierarchy, if more than one exist; top types: "
                                            + tops);
                        }
                        ttreetop = tops.iterator().next();
                    } else {
                        AnnotationMirror ttree = AnnotationUtils.fromName(
                                elements, plval);
                        ttreetop = qualhierarchy.getTopAnnotation(ttree);
                    }
                    if (polys.containsKey(ttreetop)) {
                        ErrorReporter
                                .errorAbort("QualifierPolymorphism: checker has multiple polymorphic qualifiers: "
                                        + polys.get(ttreetop) + " and " + aam);
                    }
                    polys.put(ttreetop, aam);
                }
            }
        }
        return polys;
    }

    public static boolean isPolyAll(AnnotationMirror qual) {
        return AnnotationUtils.areSameByClass(qual, PolyAll.class);
    }

    /**
     * Resolves polymorphism annotations for the given type.
     *
     * @param tree
     *            the tree associated with the type
     * @param type
     *            the type to annotate
     */
    public void annotate(MethodInvocationTree tree, AnnotatedExecutableType type) {
        if (polyQuals.isEmpty())
            return;
        // javac produces enum super calls with zero arguments even though the
        // method element requires two.
        // See also BaseTypeVisitor.visitMethodInvocation and
        // CFGBuilder.CFGTranslationPhaseOne.visitMethodInvocation
        if (TreeUtils.isEnumSuper(tree))
            return;
        List<AnnotatedTypeMirror> requiredArgs = AnnotatedTypes.expandVarArgs(
                atypeFactory, type, tree.getArguments());
        List<AnnotatedTypeMirror> arguments = AnnotatedTypes.getAnnotatedTypes(
                atypeFactory, requiredArgs, tree.getArguments());

        Map<AnnotationMirror, Set<? extends AnnotationMirror>> matchingMapping = collector
                .visit(arguments, requiredArgs);

        if (type.getReceiverType() != null) {
            matchingMapping = collector.reduce(
                    matchingMapping,
                    collector.visit(atypeFactory.getReceiverType(tree),
                            type.getReceiverType()));
        }

        if (matchingMapping != null && !matchingMapping.isEmpty()) {
            replacer.visit(type, matchingMapping);
        } else {
            completer.visit(type);
        }
    }

    public void annotate(NewClassTree tree, AnnotatedExecutableType type) {
        if (polyQuals.isEmpty())
            return;
        List<AnnotatedTypeMirror> requiredArgs = AnnotatedTypes.expandVarArgs(
                atypeFactory, type, tree.getArguments());
        List<AnnotatedTypeMirror> arguments = AnnotatedTypes.getAnnotatedTypes(
                atypeFactory, requiredArgs, tree.getArguments());

        Map<AnnotationMirror, Set<? extends AnnotationMirror>> matchingMapping = collector
                .visit(arguments, requiredArgs);
        // TODO: poly on receiver for constructors?
        // matchingMapping = collector.reduce(matchingMapping,
        // collector.visit(factory.getReceiverType(tree),
        // type.getReceiverType()));

        if (matchingMapping != null && !matchingMapping.isEmpty()) {
            replacer.visit(type, matchingMapping);
        } else {
            completer.visit(type);
        }
    }

}

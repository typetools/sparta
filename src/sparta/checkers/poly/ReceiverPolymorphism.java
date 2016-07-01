package sparta.checkers.poly;

import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.javacutil.TreeUtils;

import sparta.checkers.FlowAnnotatedTypeFactory;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;

/**
 * Replaces types in a method signature annotated with @PolySourceR and/or @PolySinkR 
 *  with the type of the receiver expression that invoked
 * the method.
 * 
 * Standard Qualifier Polymorphism replaces the types with the least upper
 * bounds of the receiver expression and/or arguments used in the method call
 * that correspond to declared receiver and/or formal parameters that have a
 * polymorphic annotation.
 * 
 */
public class ReceiverPolymorphism extends QualifierPolymorphism {

    public ReceiverPolymorphism(ProcessingEnvironment env,
            AnnotatedTypeFactory factory) {
        super(env, factory);
    }

    // Called to set polyQuals
    @Override
    public Map<AnnotationMirror, AnnotationMirror> getPolyQualifiers(
            Elements elements) {
        if (this.atypeFactory instanceof FlowAnnotatedTypeFactory) {
            return ((FlowAnnotatedTypeFactory) atypeFactory)
                    .getPolyReceiverQuals();
        }
        return null;
    }

    /**
     * Replaces types in annotatedExecType annotated with @PolySourceR and/or @PolySinkR
     * with the type of the receiver expression of methodInvocTree
     * 
     * @param methodInvocTree MethodInvocationTree of the method call
     * @param annotatedExecType AnnotatedExecutableType of method declaration.
     */
    @Override
    public void annotate(MethodInvocationTree methodInvocTree, AnnotatedExecutableType annotatedExecType) {
        if (polyQuals.isEmpty())
            return;
        // javac produces enum super calls with zero arguments even though the
        // method element requires two.
        // See also BaseTypeVisitor.visitMethodInvocation and
        // CFGBuilder.CFGTranslationPhaseOne.visitMethodInvocation
        if (TreeUtils.isEnumSuper(methodInvocTree))
            return;

        Map<AnnotationMirror, Set<? extends AnnotationMirror>> matchingMapping = null;

        if (annotatedExecType.getReceiverType() != null) {
            matchingMapping = collector.visit(
                    atypeFactory.getReceiverType(methodInvocTree), annotatedExecType.getReceiverType());
        }

        if (matchingMapping != null && !matchingMapping.isEmpty()) {
            replacer.visit(annotatedExecType, matchingMapping);
        } else {
            completer.visit(annotatedExecType);
        }

    }

    /**
     * Currently has no effect because the receiver type of a newClassTree isn't
     * computed even if one exists
     */
    @Override
    public void annotate(NewClassTree newClassTree, AnnotatedExecutableType annotatedExecType) {
        return;
    }

}

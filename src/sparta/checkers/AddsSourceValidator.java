package sparta.checkers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.visitor.AnnotatedTypeComparer;
import org.checkerframework.framework.util.AnnotatedTypes;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.TreeUtils;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;

import sparta.checkers.quals.AddsSourceData;
import sparta.checkers.quals.ParameterizedFlowPermission;
import sparta.checkers.validator.BaseValidator;

/**
 * Issues a warning if source data is added to an argument of a method call, but
 * that argument does not have the required sources as specified by the methods @AddsSourceData
 * Annotation. Also verifies that methods with are @AddsSourceData have
 * parameters they list and/or receivers.
 * 
 * @author smillst
 *
 */
public class AddsSourceValidator extends BaseValidator {
    private AnnotatedTypeFactory atypeFactory;
    private BaseTypeChecker checker;

    public AddsSourceValidator(AnnotatedTypeFactory atypeFactory,
            BaseTypeChecker checker) {
        this.atypeFactory = atypeFactory;
        this.checker = checker;
    }

    @Override
    public void visitMethod(MethodTree node) {
        ExecutableElement methElem = TreeUtils.elementFromDeclaration(node);
        isValidAddsSourceData(node, methElem, true);
    }

    /**
     * If methodElem has @AddsSourceData annotation, this method checks whether
     * it is well formed. Issues a warning if true is passed.
     * 
     * @param node
     * @param methElem
     * @param shouldWarn
     * @return boolean indicating whether the m
     */
    private boolean isValidAddsSourceData(Tree node,
            ExecutableElement methElem, boolean shouldWarn) {

        List<Integer> paramIndex = getParamIndexFromAddsSourceData(methElem);
        for (int index : paramIndex) {
            if (index == 0) {
                if (!ElementUtils.hasReceiver(methElem)
                        || methElem.getKind() == ElementKind.CONSTRUCTOR) {
                    if (shouldWarn)
                        checker.report(
                                Result.warning("addssource.no.receiver"), node);
                    return false;
                }
            } else {
                if (index - 1 >= methElem.getParameters().size()) {
                    if (shouldWarn)
                        checker.report(Result.warning(
                                "addssource.index.outofbounds", index, methElem
                                        .getParameters().size()), node);
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void visitNewClass(NewClassTree node) {
        ExecutableElement methodEle = TreeUtils.elementFromUse(node);

        List<Integer> paramIndex = getParamIndexFromAddsSourceData(methodEle);

        AnnotatedExecutableType invokedMethod = atypeFactory
                .constructorFromUse(node).first;
        List<AnnotatedTypeMirror> params = AnnotatedTypes.expandVarArgs(
                atypeFactory, invokedMethod, node.getArguments());
        for (int index : paramIndex) {
            if (isValidAddsSourceData(node, methodEle,
                    atypeFactory.isFromStubFile(methodEle))) {
                index--;
                checkAddsSourcesData(params.get(index), node.getArguments()
                        .get(index), "missing.source.argument");
            }
        }
    }

    @Override
    public void visitMethodInvocation(MethodInvocationTree node) {
        ExecutableElement methodEle = TreeUtils.elementFromUse(node);

        List<Integer> paramIndex = getParamIndexFromAddsSourceData(methodEle);
        AnnotatedExecutableType invokedMethod = atypeFactory
                .methodFromUse(node).first;
        List<AnnotatedTypeMirror> params = AnnotatedTypes.expandVarArgs(
                atypeFactory, invokedMethod, node.getArguments());
        for (int index : paramIndex) {
            if (isValidAddsSourceData(node, methodEle,
                    atypeFactory.isFromStubFile(methodEle))) {
                if (index == 0) {
                    checkAddsSourcesData(invokedMethod.getReceiverType(),
                            TreeUtils.getReceiverTree(node),
                            "missing.source.receiver");
                } else {
                    index--;
                    checkAddsSourcesData(params.get(index), node.getArguments()
                            .get(index), "missing.source.argument");
                }
            }
        }
    }

    private List<Integer> getParamIndexFromAddsSourceData(
            ExecutableElement methodEle) {
        AnnotationMirror addSourceAnno = atypeFactory.getDeclAnnotation(
                methodEle, AddsSourceData.class);
        List<Integer> paramIndex;
        if (null != addSourceAnno) {
            paramIndex = AnnotationUtils.getElementValueArray(addSourceAnno,
                    "value", Integer.class, true);
        } else {
            paramIndex = new ArrayList<>();
        }
        return paramIndex;
    }

    /**
     * The argument must have at least all the sources of the formal parameter.
     * (If the argument has more than the formal parameter, then it is a super
     * type, and an argument type incompatible warning will be issued.)
     * 
     * @param formalParam
     * @param argumentTree
     * @param warning
     */
    private void checkAddsSourcesData(AnnotatedTypeMirror formalParam,
            final ExpressionTree argumentTree, final String warningKey) {
        AnnotatedTypeComparer<Void> comparer = new AnnotatedTypeComparer<Void>() {
            @Override
            protected Void compare(AnnotatedTypeMirror type,
                    AnnotatedTypeMirror p) {
                checkforMissingSource(argumentTree, warningKey, type, p);
                return null;
            }

            @Override
            protected Void combineRs(Void r1, Void r2) {
                return r1;
            }
        };
        AnnotatedTypeMirror argument = atypeFactory
                .getAnnotatedType(argumentTree);
        checkforMissingSource(argumentTree, warningKey, formalParam, argument);
        formalParam.accept(comparer, argument);
    }

    private void checkforMissingSource(final ExpressionTree argumentTree,
            final String warningKey, AnnotatedTypeMirror type,
            AnnotatedTypeMirror p) {
        Set<ParameterizedFlowPermission> mustSources = Flow.getSources(type);
        Set<ParameterizedFlowPermission> hasSources = Flow.getSources(p);
        Set<ParameterizedFlowPermission> missingSources = new TreeSet<>();
        for (ParameterizedFlowPermission must : mustSources) {
            if (!hasSources.contains(must)) {
                missingSources.add(must);
            }
        }
        if (!missingSources.isEmpty()) {
            checker.report(Result.failure(warningKey, missingSources),
                    argumentTree);
        }
    }
}

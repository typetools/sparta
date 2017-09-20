package sparta.checkers.util;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.javacutil.InternalUtils;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;

import sparta.checkers.quals.IntentMapNew;
import sparta.checkers.quals.PutExtra;
import sparta.checkers.quals.SendIntent;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;

public class IntentStatsVisitor extends
        BaseTypeVisitor<IntentStatsAnnotatedTypeFactory> {
    IntentStatsChecker peseChecker = (IntentStatsChecker) checker;

    private int possiblePutExtrasSideEffectFree = 0;

    public IntentStatsVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    public Void visitMethod(MethodTree arg0, Void arg1) {
        possiblePutExtrasSideEffectFree = 0;
        Void output = super.visitMethod(arg0, arg1);
        return output;
    }

    @Override
    protected void checkMethodInvocability(AnnotatedExecutableType method,
            MethodInvocationTree node) {
        if (hasDeclAnnotation(node, PutExtra.class)) {
            peseChecker.numPutExtra++;
            possiblePutExtrasSideEffectFree++;
            if (node.getArguments().get(0) instanceof MethodInvocationTree
                    || node.getArguments().get(1) instanceof MethodInvocationTree) {
                possiblePutExtrasSideEffectFree = 0;
            }
            return;
        } else if (hasDeclAnnotation(node, SendIntent.class)) {
            peseChecker.putExtraSideEffectFree += possiblePutExtrasSideEffectFree;
            possiblePutExtrasSideEffectFree = 0;
            return;
        } else {
            possiblePutExtrasSideEffectFree = 0;
        }
        super.checkMethodInvocability(method, node);
    }

    public boolean hasDeclAnnotation(MethodInvocationTree tree,
            Class<? extends Annotation> anno) {
        Element ele = InternalUtils.symbol(tree);
        return atypeFactory.getDeclAnnotation(ele, anno) != null;
    }

    @Override
    protected void commonAssignmentCheck(Tree lhs, ExpressionTree rhs,
            String arg2) {
        if (rhs != null && lhs != null) {
            TypeMirror typeLHS = InternalUtils.typeOf(lhs);
            if (isTypeOf(typeLHS, android.content.Intent.class)) {
                // lhs has type Intent.
                peseChecker.numIntentAssignments++;
                Element eltExp = TreeUtils.elementFromUse(rhs);
                if (eltExp != null) {
                    TypeMirror expType = eltExp.asType();
                    AnnotatedTypeMirror atm = atypeFactory.getAnnotatedType(rhs);
                    if (isTypeOf(expType, android.content.Intent.class)
                            || (rhs instanceof MethodInvocationTree)
                            || (rhs instanceof NewClassTree &&
                                    atm.getAnnotation(IntentMapNew.class) != null)) {
                        // Assumes that anything other than the intent
                        // constructor makes aliases.
                        peseChecker.intentAliasing++;
                    }
                }
            }
        }
        super.commonAssignmentCheck(lhs, rhs, arg2);
    }

    @Override
    public Void visitAnnotation(AnnotationTree node, Void p) {
        return null;
    }

    private boolean isTypeOf(TypeMirror type, Class<?> clazz) {
        String classname = clazz.getCanonicalName();
        return TypesUtils.isDeclaredOfName(type, classname);
    }

}

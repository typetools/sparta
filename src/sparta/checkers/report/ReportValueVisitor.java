package sparta.checkers.report;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.value.ValueVisitor;
import org.checkerframework.common.value.qual.StringVal;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedPrimitiveType;
import org.checkerframework.javacutil.AnnotationUtils;

import com.sun.source.tree.Tree;

public class ReportValueVisitor extends ValueVisitor {

    public ReportValueVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    private void reportStrings(AnnotatedTypeMirror type, Tree tree) {
        List<String> strings = getStringValues(type);
        for (String string : strings) {
            for (Category regex : ReportValueChecker.CATEGORIES) {
                if (regex.match(string)) {
                    checker.report(Result.warning(regex.errorKey, string), tree);
                    // Each string should only be assigned to one category
                    break;
                }
            }
        }

    }

    private List<String> getStringValues(AnnotatedTypeMirror type) {
        AnnotationMirror anno = type.getAnnotation(StringVal.class);
        if (anno != null) {
            List<String> strings = AnnotationUtils.getElementValueArray(anno, "value", String.class, true);
            return strings;
        }
        return new ArrayList<String>();
    }

    @Override
    public boolean isValidUse(AnnotatedArrayType type, Tree tree) {
        reportStrings(type, tree);
        return super.isValidUse(type, tree);
    }

    @Override
    public boolean isValidUse(AnnotatedDeclaredType declarationType, AnnotatedDeclaredType useType, Tree tree) {
        reportStrings(declarationType, tree);
        reportStrings(useType, tree);
        return super.isValidUse(declarationType, useType, tree);
    }

    @Override
    public boolean isValidUse(AnnotatedPrimitiveType type, Tree tree) {
        reportStrings(type, tree);
        return super.isValidUse(type, tree);
    }
}

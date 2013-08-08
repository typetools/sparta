package sparta.checkers;

import checkers.basetype.BaseTypeChecker;
import checkers.fenum.quals.FenumTop;
import checkers.fenum.quals.FenumUnqualified;
import checkers.quals.Bottom;
import checkers.quals.StubFiles;
import checkers.quals.TypeQualifiers;
import checkers.source.Result;
import checkers.types.QualifierHierarchy;
import checkers.util.GraphQualifierHierarchy;
import checkers.util.MultiGraphQualifierHierarchy.MultiGraphFactory;

import javacutils.AnnotationUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

import sparta.checkers.quals.DependentPermissions;

/**
 * Checker for dependentpermissions, based on the fenum checker
 * 
 * @author edwardwu
 * 
 */
@TypeQualifiers({ DependentPermissions.class, FenumTop.class, FenumUnqualified.class, Bottom.class })
@StubFiles("permission.astub")
public class DependentPermissionsChecker extends
        BaseTypeChecker<DependentPermissionsAnnotatedTypeFactory> {
    protected AnnotationMirror DP, BOTTOM;

    @Override
    public void initChecker() {
        Elements elements = processingEnv.getElementUtils();
        BOTTOM = AnnotationUtils.fromClass(elements, Bottom.class);
        DP = AnnotationUtils.fromClass(elements, DependentPermissions.class);
        super.initChecker();
    }

    /*
     * Used to overwrite the warning messages through by the source checker
     * 
     * @see checkers.source.SourceChecker#report(checkers.source.Result,
     * java.lang.Object)
     */
    @Override
    public void report(final Result r, final Object src) {

        if (r.isSuccess())
            return;

        for (Result.DiagMessage msg : r.getDiagMessages()) {

            String s = ((String) (msg.getArgs()[0]));

            super.report(Result.failure(
                    "dependent.permissions",
                    src.toString(),
                    s.contains("[") ? s.substring(s.indexOf('[') + 1, s.indexOf(']')) : s
                            .subSequence(s.indexOf('(') + 2, s.indexOf(')') - 1)), src);
        }
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new DPQualifierHierarchy(factory);
    }

    protected class DPQualifierHierarchy extends GraphQualifierHierarchy {

        /*
         * We use the constructor of GraphQualifierHierarchy that allows us to
         * set a dedicated bottom qualifier.
         */
        public DPQualifierHierarchy(MultiGraphFactory factory) {
            super(factory, BOTTOM);
        }

        @Override
        public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
            if (AnnotationUtils.areSameIgnoringValues(lhs, DP)
                    && AnnotationUtils.areSameIgnoringValues(rhs, DP)) {
                return AnnotationUtils.areSame(lhs, rhs);
            }
            // Ignore annotation values to ensure that annotation is in
            // supertype map.
            if (AnnotationUtils.areSameIgnoringValues(lhs, DP)) {
                lhs = DP;
            }
            if (AnnotationUtils.areSameIgnoringValues(rhs, DP)) {
                rhs = DP;
            }
            return super.isSubtype(rhs, lhs);
        }
    }
}

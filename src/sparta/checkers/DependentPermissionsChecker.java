package sparta.checkers;

import checkers.basetype.BaseTypeChecker;
import checkers.quals.Bottom;
import checkers.quals.StubFiles;
import checkers.quals.TypeQualifiers;
import checkers.source.Result;

import sparta.checkers.quals.DependentPermissions;
import sparta.checkers.quals.DependentPermissionsTop;
import sparta.checkers.quals.DependentPermissionsUnqualified;

/**
 * Checker for dependentpermissions, based on the fenum checker
 * It examines constant strings used as URIs and Intent actions to
 * determine if the method call would require additional permissions.
 *
 * @author edwardwu
 *
 */

@TypeQualifiers({ DependentPermissions.class, DependentPermissionsTop.class,
    DependentPermissionsUnqualified.class, Bottom.class })
@StubFiles("permission.astub")
public class DependentPermissionsChecker extends BaseTypeChecker {

    /*
    @Override
    public void initChecker() {
        super.initChecker();
    }
    */

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
}
package sparta.checkers.permission;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.StubFiles;
import org.checkerframework.framework.source.Result;

/**
 * Checker for dependentpermissions, based on the fenum checker
 * It examines constant strings used as URIs and Intent actions to
 * determine if the method call would require additional permissions.
 *
 * @author edwardwu
 *
 */

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
     * @see org.checkerframework.framework.source.SourceChecker#report(checkers.source.Result,
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

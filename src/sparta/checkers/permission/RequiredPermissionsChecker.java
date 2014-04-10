package sparta.checkers.permission;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.StubFiles;
import org.checkerframework.framework.qual.TypeQualifiers;
import org.checkerframework.framework.qual.Unqualified;


@TypeQualifiers(Unqualified.class)
@StubFiles("permission.astub")
public class RequiredPermissionsChecker extends BaseTypeChecker {
}
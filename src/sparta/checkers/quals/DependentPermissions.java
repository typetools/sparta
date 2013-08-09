package sparta.checkers.quals;

/**
 * List of Android dependent permissions required in some cases to use a method.
 * 
 * Propagated from the constants in Android Framework
 *
 */

import checkers.quals.SubtypeOf;
import checkers.quals.TypeQualifier;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER, ElementType.CONSTRUCTOR,
        ElementType.METHOD })
@TypeQualifier
@SubtypeOf(DependentPermissionsTop.class)
public @interface DependentPermissions {
    String value();
}

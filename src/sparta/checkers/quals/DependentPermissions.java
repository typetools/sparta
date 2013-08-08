package sparta.checkers.quals;

/**
 * List of Android dependent permissions required in some cases to use a method.
 * 
 * Propagated from the constants in Android Framework
 *
 */

import java.lang.annotation.*;

import checkers.fenum.quals.FenumTop;
import checkers.quals.*;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER, ElementType.CONSTRUCTOR, ElementType.METHOD })
@TypeQualifier
@SubtypeOf(FenumTop.class)
public @interface DependentPermissions {
    String value();
}

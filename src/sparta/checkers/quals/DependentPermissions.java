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

/**
 * A generic fake enumeration qualifier that is parameterized by a name. It is
 * written in source code as, for example, <tt>@Fenum("cardSuit")</tt> and
 * <tt>@Fenum("faceValue")</tt>, which would be distinct fake enumerations.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@TypeQualifier
@SubtypeOf(FenumTop.class)
public @interface DependentPermissions {
    String value();
}

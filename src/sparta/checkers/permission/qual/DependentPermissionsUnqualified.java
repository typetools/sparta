package sparta.checkers.permission.qual;

import java.lang.annotation.*;

import org.checkerframework.framework.qual.*;

/**
 * An dependent permissions unqualified type.  Such a type is incomparable to (that is, neither a
 * subtype nor a supertype of) any fake enum type.
 * <p>
 *
 * This annotation may not be written in source code; it is an
 * implementation detail of the checker.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({}) // empty target prevents programmers from writing this in a program
@TypeQualifier
@SubtypeOf( { DependentPermissionsTop.class } )
public @interface DependentPermissionsUnqualified {}

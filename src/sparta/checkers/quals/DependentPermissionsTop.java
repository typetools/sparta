package sparta.checkers.quals;

import java.lang.annotation.*;

import org.checkerframework.framework.qual.*;

/**
 * The top of the fake enumeration type hierarchy for dependent permissions.
 * <p>
 *
 * This annotation may not be written in source code; it is an
 * implementation detail of the checker.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({}) // empty target prevents programmers from writing this in a program
@TypeQualifier
@SubtypeOf( { } )
public @interface DependentPermissionsTop {}

package sparta.checkers.quals;

import java.lang.annotation.*;

/**
 * A declaration annotation to mark that no flow happens.
 * This is shorthand for specifying no flow sources and
 * no flow sinks for the return type and all parameter types of
 * any contained methods.
 *
 * @see NotReviewed
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR,
    ElementType.TYPE, ElementType.PACKAGE})
public @interface PolyFlow {}
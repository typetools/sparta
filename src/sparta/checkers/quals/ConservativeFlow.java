package sparta.checkers.quals;

import java.lang.annotation.*;

/**
 * A declaration annotation to mark that conservative assumptions
 * about the annotated element and enclosed elements should be made.
 * This is shorthand for specifying flow sources and
 * flow sinks for the return type and all parameter types of
 * any contained methods.
 *
 * TODO: should field types be influenced?
 *
 * @see DefaultFlow
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR,
    ElementType.TYPE, ElementType.PACKAGE})
public @interface ConservativeFlow {}
package sparta.checkers.quals;

import java.lang.annotation.*;

/**
 * A method/constructor declaration annotation to mark that no
 * flow happens. This is shorthand for specifying no flow sources and
 * no flow sinks for the return type and all parameter types.
 * TODO: should we also support classes and packages?
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface NoFlow {}
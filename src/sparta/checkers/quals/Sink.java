package sparta.checkers.quals;

import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * List of data flow sinks that are attached to a certain piece of data.
 * FlowPermission.ANY is the bottom type. The empty set is the top type.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_PARAMETER, ElementType.TYPE_USE })
@SubtypeOf({})
public @interface Sink {

    /**
     * By default we allow no sinks. There is always a @Sink annotation and this
     * default ensures that the annotation has no effect.
     */
    String[] value() default {};
}

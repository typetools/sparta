package sparta.checkers.quals;

import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * List of information flow sources that are attached to a certain piece of data.
 * FlowPermission.ANY is the top type. The empty set is the bottom type.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_PARAMETER, ElementType.TYPE_USE })
@SubtypeOf({})
public @interface Source {

    /**
     * By default we allow no sources. There is always a @Source annotation and
     * this default ensures that the annotation has no effect.
     */
    String[] value() default {};
}

package sparta.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declaration annotation that specifies the formal parameters whose value
 * should be used to parameterized the given permission.
 * 
 * @author smillst
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PACKAGE })
public @interface InferParameterizedPermission {
    /**
     * FlowPermission for which parameters should be inferred
     */
    String value();

    /**
     * Index of formal parameter whose value should be used as the permission
     * parameter. If more than one index is given, then the values are appended
     * in the order listed using the separator specified. 0 indicates that the
     * receiver should be used. The default is 1.
     */
    int[] param() default 1;

    /**
     * Indicates whether the permission is a source or a sink or both. Valid
     * values are Source, Sink, or Both
     */
    String isA() default "Source";

    /**
     * A string used to separator more multiple values.
     */
    String separator() default "";
}
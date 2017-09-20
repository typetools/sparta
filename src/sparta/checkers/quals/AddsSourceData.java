package sparta.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A declaration annotation specifying that a method adds source data to the
 * list parameters or the receiver.
 *
 * A value of 0 means the receiver. A value of 1 or means the parameter at the
 * index.
 *
 * @author smillst
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PACKAGE })
public @interface AddsSourceData {
    /**
     * Indexes of parameters (starting at 1) that add source data. An index of 0
     * means that the receiver is supplied source data.
     *
     * @return
     */
    int[] value() default 1;
}
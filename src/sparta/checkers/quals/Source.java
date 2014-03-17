package sparta.checkers.quals;

import checkers.quals.SubtypeOf;
import checkers.quals.TypeQualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * List of data flow sources that are attached to a certain piece of data.
 * FlowPermission.ANY is the top type. The empty set is the bottom type.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_PARAMETER, ElementType.TYPE_USE,
/* The following only added to make Eclipse work. */
ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE })
@TypeQualifier
@SubtypeOf({})
public @interface Source {

    /**
     * By default we allow no sources. There is always a @Source annotation and
     * this default ensures that the annotation has no effect.
     */
    FlowPermission[] value() default {};
    FineSource[] finesources() default {};
}

package sparta.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * List of Android permissions required in some cases to use a method.
 * 
 * Sometimes only one of the permissions are required, sometimes all, sometimes
 * none.
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.CONSTRUCTOR, ElementType.METHOD })
public @interface RequiredPermissions {
    // TODO: the annotation is not recognized if it's in a comment!
    /* @Permission */String[] value() default {};
}
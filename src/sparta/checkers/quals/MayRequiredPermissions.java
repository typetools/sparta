package sparta.checkers.quals;

import java.lang.annotation.*;

/**
 * List of Android permissions that may be required in some cases to use a method. 
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface MayRequiredPermissions {
    // TODO: the annotation is not recognized if it's in a comment!
    /*@Permission*/ String[] value() default {};
    //Method notes() should contain an explanation on when the permission is required
    String notes() default "";
}
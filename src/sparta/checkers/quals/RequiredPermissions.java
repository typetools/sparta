package sparta.checkers.quals;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface RequiredPermissions {
    // TODO: the annotation is not recognized if it's in a comment!
    /*@Permission*/ String[] value() default {};
}
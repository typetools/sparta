package sparta.checkers.quals;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface RequiredPermissions {
    /*@Permission*/ String[] value();
}
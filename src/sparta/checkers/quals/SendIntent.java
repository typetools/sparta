package sparta.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.checkerframework.framework.qual.InheritedAnnotation;

/*
 * A @SendIntent annotation is used to identify methods that sends an intent.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
@InheritedAnnotation
public @interface SendIntent {
    String value() default "";
}
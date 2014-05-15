package sparta.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeQualifier;

/**
 * Annotation @IntentMapBottom is used to represent the bottom type of @IntentMap,
 * an intent with a map containing all possible keys and mapping to all possible values.
 * 
 * @see @IntentMap
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_PARAMETER, ElementType.TYPE_USE,
/* The following only added to make Eclipse work. */
ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE })
@TypeQualifier
@SubtypeOf({IntentMap.class})
public @interface IntentMapBottom {
}
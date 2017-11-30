package sparta.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Annotation @IntentMapBottom is used to represent the bottom type of @IntentMap,
 * an intent with a map containing all possible keys and mapping to all possible values.
 *
 * @see @IntentMap
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
@SubtypeOf({IntentMap.class})
public @interface IntentMapBottom {
}
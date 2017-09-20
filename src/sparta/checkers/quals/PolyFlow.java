package sparta.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation @PolyFlow expresses that each contained method should be annotated
 * as @PolySource @PolySink for both the return types and all parameters. It
 * should be used to express a relationship between the parameters and the
 * return types.
 *
 * @see @PolyFlowReceiver
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE, ElementType.PACKAGE })
public @interface PolyFlow {
}
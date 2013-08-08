package sparta.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A declaration annotation to mark that all the parameters, the return the
 * receiver as polymorphic. This is shorthand for specifying @PolySource @PolySink
 * on all parameters, returns and receivers
 * 
 * @see PolyFlow
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE, ElementType.PACKAGE })
public @interface PolyFlowReceiver {
}
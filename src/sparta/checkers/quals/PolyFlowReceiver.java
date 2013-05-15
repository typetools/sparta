package sparta.checkers.quals;

import java.lang.annotation.*;

/**
 * A declaration annotation to mark that all the parameters, the return
 * the receiver as polymorphic.
 * This is shorthand for specifying @PolyFlowSources @PolyFlowSinks on all
 * parameters, returns and receivers
 *
 * @see PolyFlow
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR,
    ElementType.TYPE, ElementType.PACKAGE})
public @interface PolyFlowReceiver {}
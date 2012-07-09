package sparta.checkers.quals;

import java.lang.annotation.*;

import checkers.quals.*;

/**
 * Polymorphic qualifier for flow sinks.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@TypeQualifier
@PolymorphicQualifier(FlowSinks.class)
public @interface PolyFlowSinks {}
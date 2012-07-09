package sparta.checkers.quals;

import java.lang.annotation.*;

import checkers.quals.*;

/**
 * Polymorphic qualifier for flow sources.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@TypeQualifier
@PolymorphicQualifier(FlowSources.class)
public @interface PolyFlowSources {}
package sparta.checkers.quals;

import java.lang.annotation.*;

import checkers.quals.*;

/**
 * Polymorphic qualifier for flow sinks.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE, 
    /* The following only added to make Eclipse work. */
    ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE})
@TypeQualifier
@PolymorphicQualifier(FlowSinks.class)
public @interface PolyFlowSinks {}
package sparta.checkers.quals;

import java.lang.annotation.*;

import checkers.quals.*;

/**
 * List of data flow sinks that are attached to a certain piece of data.
 * FlowSink.ANY is the bottom type.
 * The empty set is the top type.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE, 
    /* The following only added to make Eclipse work. */
    ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE})
@TypeQualifier
@SubtypeOf({})
public @interface FlowSinks {

    /**
     * Data flow sinks.
     * TODO: will we also use this in other annotations? Then we should refactor and
     * make this class top-level.
     */
    public enum FlowSink {
        /**
         * This special constant is shorthand for all sinks, that is, the
         * data can go to any possible sink.
         * Using this constant is preferred to listing all constants, because it's future safe.
         */
        ANY,

        NETWORK,
        TEXTMESSAGE,
        EMAIL
    }

    /**
     * By default we allow no sinks.
     * There is always a @FlowSinks annotation and this default
     * ensures that the annotation has no effect.
     */
    FlowSink[] value() default {};
}
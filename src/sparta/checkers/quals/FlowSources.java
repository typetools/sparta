package sparta.checkers.quals;

import java.lang.annotation.*;

import checkers.quals.*;

/**
 * List of data flow sources that are attached to a certain piece of data.
 * FlowSource.ANY is the top type.
 * The empty set is the bottom type.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE, 
    /* The following only added to make Eclipse work. */
    ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE})
@TypeQualifier
@SubtypeOf({})
public @interface FlowSources {

    /**
     * Data flow sources.
     * TODO: will we also use this in other annotations? Then we should refactor and
     * make this class top-level.
     */
    public enum FlowSource {
        /**
         * This special constant is shorthand for all sources, that is, the
         * data can come from any possible source.
         * Using this constant is preferred to listing all constants, because it's future safe.
         */
        ANY,
        
        ACCOUNTS,
        CAMERA,
        FILESYSTEM,
        IMEI,
        LOCATION,
        MICROPHONE,
        NETWORK,
        PHONE_NUMBER,
        TIME,
        USER_INPUT
    }

    /**
     * By default we allow no sources.
     * There is always a @FlowSources annotation and this default
     * ensures that the annotation has no effect.
     */
    FlowSource[] value() default {};
}
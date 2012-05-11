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
@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
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

        CAMERA,
        MICROPHONE,
        LOCATION
    }

    /**
     * By default we allow no sources. As this is the only qualifier in
     * this hierarchy, there is always a @FlowSources annotation. This default
     * ensures that the annotation has no effect.
     */
    FlowSource[] value() default {};
}
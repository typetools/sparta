package sparta.checkers.quals;

import java.lang.annotation.*;

/**
 * This a declaration annotation that means that a method, class, or package has not been reviewed, 
 * but should be.  It will annotate the return and parameter types in a method, class, or package 
 * as @Sources(NOT_REVIEWED) @Sinks(NOT_REVIEWED). Using a method with this annotation will always
 * cause a type error.
 * 
 * It should only be used in stub files to indicate packages in jar files that have not been reviewed. 
 *
 * TODO: should field types be influenced?
 *
 * @see Reviewed
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR,
    ElementType.TYPE, ElementType.PACKAGE})
public @interface NotReviewed {}
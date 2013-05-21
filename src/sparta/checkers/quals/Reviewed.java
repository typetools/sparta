package sparta.checkers.quals;

import java.lang.annotation.*;

/**
 * This a declaration annotation used by the StubParser to indicate that a method was written 
 * in the stub file.  It is used so that the @NotReviewed annotation is not applied to the method.  
 * It does not add any annotations to the method signature.  It should only be used by the StubParser.
 *
 * @see NotReviewed
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR,
    ElementType.TYPE, ElementType.PACKAGE})
public @interface Reviewed {}
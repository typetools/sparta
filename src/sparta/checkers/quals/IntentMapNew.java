package sparta.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeQualifier;

/**
 * Annotation @IntentMapNew is used as the return type of a "new Intent()"
 * call. No type refinement will happen when this type is used.
 *
 * @IntentMapNew details:
 * Not present in the intent map type hierarchy.
 * Assignable and copyable anywhere.
 * No flow sensitive type refinement.
 * No expression other than "new Intent()" ever has this type - Intent
 *  constructors will be annotated in the stub files with @IntentMapNew.
 * As a receiver to putExtra(...): treat as top. We don't want @IntentMapNew to
 *  always be treated as top, because the user must be able to add explicit
 *   annotations on the lhs of a "new Intent()" call.
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)

//This annotation should only be used in StubFiles, not in real code.
@Target({ElementType.TYPE_USE})

@SubtypeOf({})
@TypeQualifier
public @interface IntentMapNew {
}
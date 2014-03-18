package sparta.checkers.quals;

import org.checkerframework.checker.fenum.qual.FenumTop;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeQualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Android permissions are String constants in
 * {@link android.Manifest.permission}. This fake enumeration annotation ensures
 * correct usage of those strings.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_PARAMETER, ElementType.TYPE_USE })
@TypeQualifier
@SubtypeOf(FenumTop.class)
public @interface Permission {
}
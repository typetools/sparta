package sparta.checkers.quals;

import java.lang.annotation.*;

import checkers.fenum.quals.FenumTop;
import checkers.quals.*;

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
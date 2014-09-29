package sparta.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PACKAGE })
public @interface InferPermissionParameter {
    /**
     * FlowPermission for which parameters should be infer
     */
    FlowPermission value();
    int[] param() default 1; //Receiver is 0.
    //multiple parameters will be appended in order listed.
    //The appends will be separated with an appropriate delineator for the permission or a space,
    // For FILESYSTEM, this will be File.separator

    String isA() default "Source";
    
    String seperator() default "";
}
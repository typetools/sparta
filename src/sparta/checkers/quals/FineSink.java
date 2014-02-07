package sparta.checkers.quals;

import checkers.quals.SubtypeOf;
import checkers.quals.TypeQualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An optional parameter to a flow permission represents a subset of its possible locations. 
 * For example, INTERNET("maps.google.com") represents just the portion of the Internet that lies within the “maps.google.com” domain.  
 * Only flow permissions that represent a class of locations will be parameterized.  
 * To begin with, only FILESYSTEM and INTERNET will be parameterized so that annotations will 
 * specify domains or file paths where information may flow. 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_PARAMETER, ElementType.TYPE_USE,
/* The following only added to make Eclipse work. */
ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE })
@TypeQualifier
@SubtypeOf({})
public @interface FineSink {

    /**
     * Returns the CoarseFlowPermission associated with this FineSink
     */
    FlowPermission value();
    
    /**
     * By default sinks will not have any parameters.
     */
    String[] params() default {};
}

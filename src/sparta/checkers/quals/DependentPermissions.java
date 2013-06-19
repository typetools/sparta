package sparta.checkers.quals;

import java.lang.annotation.*;
/**
 * List of Android permissions required in some cases to use a method.
 * 
 * Sometimes only one of the permissions are required, sometimes all, sometimes none. 
 *
 */


/**
 * Constants that couldn't be found on API-15:
 *	android.intent.action.MASTER_CLEAR android.permission.MASTER_CLEAR S
 *	android.intent.action.PRELOAD com.android.browser.permission.PRELOAD S
 *	com.android.email.ACCOUNT_INTENT com.android.email.permission.ACCESS_PROVIDER S
 *  com.android.email.EXCHANGE_INTENT com.android.email.permission.ACCESS_PROVIDER S
 *	com.android.launcher.action.INSTALL_SHORTCUT com.android.launcher.permission.INSTALL_SHORTCUT S
 *	com.android.launcher.action.UNINSTALL_SHORTCUT com.android.launcher.permission.UNINSTALL_SHORTCUT S
 *	com.android.internal.telephony.IWapPushManager com.android.smspush.WAPPUSH_MANAGER_BIND S OBS: IWapPushManager is a class and not a constant.
 *	com.android.mms.intent.action.SENDTO_NO_CONFIRMATION android.permission.SEND_SMS_NO_CONFIRMATION S
 *	com.android.phone.PERFORM_CDMA_PROVISIONING android.permission.PERFORM_CDMA_PROVISIONING S
 * @author pbsf
 *
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface DependentPermissions {
    // TODO: the annotation is not recognized if it's in a comment!
    /*@Permission*/ String[] value() default {};
}
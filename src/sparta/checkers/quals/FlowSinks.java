package sparta.checkers.quals;

import java.lang.annotation.*;

import checkers.quals.*;

/**
 * List of data flow sinks that are attached to a certain piece of data.
 * FlowSink.ANY is the bottom type.
 * The empty set is the top type.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE, 
    /* The following only added to make Eclipse work. */
    ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE})
@TypeQualifier
@SubtypeOf({})
public @interface FlowSinks {

    /**
     * Data flow sinks.
     * TODO: will we also use this in other annotations? Then we should refactor and
     * make this class top-level.
     */
    public enum FlowSink {
        /**
         * This special constant is shorthand for all sinks, that is, the
         * data can go to any possible sink.
         * Using this constant is preferred to listing all constants, because it's future safe.
         */
        ANY,
        CONDITIONAL,
        
        ACCOUNT_MANAGER,
        ACCOUNTS,
        ACTIVITY_WATCHER,
        ALARM,
        ALWAYS_FINISH,
        ANIMATION_SCALE,
        APN_SETTINGS,
        APP_TOKENS,
        AUDIO_SETTINGS,
        AUDIO,
        BLUETOOTH_ADMIN,
        BLUETOOTH,
        BROADCAST_PACKAGE_REMOVED,
        BROADCAST_SMS,
        BROADCAST_STICKY,
        BROADCAST_WAP_PUSH,
        CACHE_FILES,
        CALENDAR,
        CALL_LOG,
        CALL_PHONE,
        CAMERA_SETTINGS,
        CLEAR_APP_CACHE,
        CLEAR_APP_USER_DATA,
        COMPONENT_ENABLED_STATE,
        CONFIGURATION,
        CONTACTS,
        DEBUG_APP,
        DEVICE_POWER,
        DIAGNOSTIC,
        DISABLE_KEYGUARD,
        DISPLAY,
        EMAIL,
        EXTERNAL_STORAGE,
        FILESYSTEM,
        FLASHLIGHT,
        GSERVICES,
        HISTORY_BOOKMARKS,
        INSTALL_LOCATION_PROVIDER,
        INSTALL_PACKAGES,
        INTERNET,
        KILL_BACKGROUND_PROCESSES,
        LOCATION_UPDATES,
        LOG,
        NETWORK_STATE,
        NETWORK,
        NFC,
        ORIENTATION,
        OUTGOING_CALLS,
        PACKAGES,
        PHONE_STATE,
        POINTER_SPEED,
        PREFERRED_APPLICATIONS,
        PROCESS_LIMIT,
        PROFILE,
        RANDOM,
        REBOOT,
        REORDER_TASKS,
        SECURE_SETTINGS,
        SETTINGS,
        SHARED_PREFERENCES,
        SIGNAL_PERSISTENT_PROCESSES,
        SIP,
        SMS,
        SOCIAL_STREAM,
        STATUS_BAR,
        SUBSCRIBED_FEEDS,
        SYNC_SETTINGS,
        TIME_ZONE,
        TIME,
        USER_DICTIONARY,
        VIBRATE,
        VOICEMAIL,
        WAKE_LOCK,
        WALLPAPER_HINTS,
        WALLPAPER,
        WIFI_MULTICAST_STATE,
        WIFI_STATE,
}

    /**
     * By default we allow no sinks.
     * There is always a @FlowSinks annotation and this default
     * ensures that the annotation has no effect.
     */
    FlowSink[] value() default {};
}
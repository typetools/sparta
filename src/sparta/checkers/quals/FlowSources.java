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
@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE, 
    /* The following only added to make Eclipse work. */
    ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE})
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
        LITERAL,
        
        ACCELEROMETER,
        ACCOUNT_MANAGER,
        ACCOUNTS,
        BROWSER_HISTORY,
        APP_TOKENS,
        AUDIO_SETTINGS,
        AUDIO,
        BATTERY_STATS,
        BLUETOOTH_ADMIN,
        BLUETOOTH,
        BOOT_COMPLETED,
        BROADCAST_PACKAGE_REMOVED,
        BROADCAST_SMS,
        BROADCAST_STICKY,
        BROADCAST_WAP_PUSH,
        CALENDAR,
        CALL_LOG,
        CAMERA,
        CHECKIN_PROPERTIES,
        CLEAR_APP_CACHE,
        CLEAR_APP_USER_DATA,
        COARSE_LOCATION,
        CONTACTS,
        CREDENTIALS,
        DIAGNOSTIC,
        DUMP,
        EXTERNAL_STORAGE,
        FILESYSTEM,
        FINE_LOCATION,
        FRAME_BUFFER,
        HARDWARE_TEST,
        HISTORY_BOOKMARKS,
        IMEI,
        INJECT_EVENTS,
        INPUT_STATE,
        INTERNET,
        LOCATION_EXTRA_COMMANDS,
        LOCATION,
        LOG,
        MASTER_CLEAR,
        MICROPHONE,
        MMS,
        MOCK_LOCATION,
        NETWORK_STATE,
        NETWORK,
        NFC,
        OUTGOING_CALLS,
        PACKAGE_SIZE,
        PACKAGES,
        PHONE_NUMBER,
        PHONE_STATE,
        PROFILE,
        RANDOM,
        SERIAL_NUMBER,
        SHARED_PREFERENCES,
        SIP,
        SMS,
        SOCIAL_STREAM,
        SUBSCRIBED_FEEDS,
        SURFACE_FLINGER,
        SYNC_SETTINGS,
        SYNC_STATS,
        TASKS,
        TIME,
        USER_DICTIONARY,
        USER_INPUT,
        WAP_PUSH,
        WIFI_STATE, CAMERA_SETTINGS,


    }

    /**
     * By default we allow no sources.
     * There is always a @FlowSources annotation and this default
     * ensures that the annotation has no effect.
     */
    FlowSource[] value() default {};
}

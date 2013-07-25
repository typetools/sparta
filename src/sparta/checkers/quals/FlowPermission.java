package sparta.checkers.quals;



/**
 * This enum contains the possible sources or sinks. 
 * 
 * Most of them are exactly the Android permission used to grant access to 
 * sensitive system resources. The rest of them govern the access to source or sinks
 * determined to be sensitive for security purposes.  
 * 
 * Each permission is either a source of sensitive information or a sensitive sink.  
 * Some permissions are both. 
 * Tip: you can statically import the enum constants so that you don't have to write 
 * FlowPermission in the annotations. 
 * (@Sink(EMAIL) rather than @Sink(FlowPermission.EMAIL))
 * import static sparta.checkers.quals.FlowPermission.*;
 *
 */
public enum FlowPermission {

/**
 * This is a special constant used to force a type error
 * for API methods that have not been reviewed.  It should
 * not appear in policy files
 */
	NOT_REVIEWED(T.BOTH),
    /**
     * This special constant is shorthand for all sources, that is, the
     * data can come from any possible source.
     * Using this constant is preferred to listing all constants, because it's future safe.
     */
    ANY(T.BOTH),
    
    /**
     * The following are special permissions added by SPARTA
     * Make sure that whatever permission you add is not the same as any permission already added.
     */
    LITERAL(T.SOURCE),
    CONDITIONAL(T.SINK),
    
    
    CAMERA_SETTINGS(T.BOTH),
    DISPLAY (T.SINK),
    FILESYSTEM (T.BOTH),
    RANDOM(T.SOURCE),
    READ_TIME (T.SOURCE), //WRITE_TIME is an Android Permission, but read time isn't
    SQLITE_DATABASE (T.BOTH), 
    USER_INPUT (T.SOURCE),
    WRITE_LOGS (T.SINK), //READ_LOGS is an Android Permission, but there is no WRITE_LOGS
    DATABASE (T.BOTH), //This is an Android database that could be any of the Content database.
    SYSTEM_PROPERTIES(T.BOTH),//This is for java.lang.System
    MEDIA(T.SOURCE),
    READ_EMAIL (T.SOURCE),
    WRITE_EMAIL (T.SINK),
    WRITE_CLIPBOARD(T.SINK),
    READ_CLIPBOARD(T.SOURCE),
    
    /**
     * These are old sources or sinks that may or may not be of use
     */
    PHONE_NUMBER (T.SOURCE), 
    SHARED_PREFERENCES (T.BOTH),
    ACCELEROMETER(T.SOURCE),
    
    /**
     * The following permissions are temporary and implemented 
     * now in a simple way for an upcoming engagement. 
     */
    
    REFLECTION(T.SOURCE), //The caller of the invoke method should have this permission.
    INTENT(T.BOTH), 
    BUNDLE(T.SOURCE), 
    PROCESS_BUILDER(T.BOTH), //The ProcessBuilder variable should have this permission.
    PARCEL(T.BOTH),
    SECURE_HASH(T.BOTH),  //Use only for one way hashes (MD5 for example)


    /**
     * Android Manifest.permissions (Do not add new permissions below here)
     * I was mostly guessing whether the permissions should be source, sink or both, 
     * so feel free to change the T of the permission. -SOM
     */
  //Allows read/write access to the "properties" table in the checkin database, to change values that get uploaded. 
		  ACCESS_CHECKIN_PROPERTIES  (T.BOTH) , 
  //Allows an app to access approximate location derived from network location sources such as cell towers and Wi-Fi. 
          ACCESS_COARSE_LOCATION  (T.SOURCE) , 
  //Allows an app to access precise location from location sources such as GPS, cell towers, and Wi-Fi. 
          ACCESS_FINE_LOCATION  (T.SOURCE) , 
  //Allows an application to access extra location provider commands 
          ACCESS_LOCATION_EXTRA_COMMANDS  (T.SOURCE) , 
  //Allows an application to create mock location providers for testing 
          ACCESS_MOCK_LOCATION  (T.SOURCE) , 
  //Allows applications to access information about networks 
          ACCESS_NETWORK_STATE  (T.SOURCE) , 
  //Allows an application to use SurfaceFlinger's low level features 
          ACCESS_SURFACE_FLINGER  (T.BOTH) , 
  //Allows applications to access information about Wi-Fi networks 
          ACCESS_WIFI_STATE  (T.SOURCE) , 
  //Allows applications to call into AccountAuthenticators. 
          ACCOUNT_MANAGER  (T.SOURCE) , 
  //Allows an application to add voicemails into the system. 
          ADD_VOICEMAIL  (T.SINK) , 
  //Allows an application to act as an AccountAuthenticator for the AccountManager 
          AUTHENTICATE_ACCOUNTS  (T.BOTH) , 
  //Allows an application to collect battery statistics 
          BATTERY_STATS  (T.SOURCE) , 
  //Must be required by an AccessibilityService, to ensure that only the system can bind to it. 
          BIND_ACCESSIBILITY_SERVICE  (T.BOTH) , 
  //Allows an application to tell the AppWidget service which application can access AppWidget's data. 
          BIND_APPWIDGET  (T.BOTH) , 
  //Must be required by device administration receiver, to ensure that only the system can interact with it. 
          BIND_DEVICE_ADMIN  (T.BOTH) , 
  //Must be required by an InputMethodService, to ensure that only the system can bind to it. 
          BIND_INPUT_METHOD  (T.BOTH) , 
  //Must be required by a RemoteViewsService, to ensure that only the system can bind to it. 
          BIND_REMOTEVIEWS  (T.BOTH) , 
  //Must be required by a TextService (e.g. 
          BIND_TEXT_SERVICE  (T.BOTH) , 
  //Must be required by an VpnService, to ensure that only the system can bind to it. 
          BIND_VPN_SERVICE  (T.BOTH) , 
  //Must be required by a WallpaperService, to ensure that only the system can bind to it. 
          BIND_WALLPAPER  (T.BOTH) , 
  //Allows applications to connect to paired bluetooth devices 
          BLUETOOTH  (T.BOTH) , 
  //Allows applications to discover and pair bluetooth devices 
          BLUETOOTH_ADMIN  (T.BOTH) , 
  //Required to be able to disable the device (very dangerous!). 
          BRICK  (T.SINK) , 
  //Allows an application to broadcast a notification that an application package has been removed. 
          BROADCAST_PACKAGE_REMOVED  (T.SINK) , 
  //Allows an application to broadcast an SMS receipt notification 
          BROADCAST_SMS  (T.SINK) , 
  //Allows an application to broadcast sticky intents. 
          BROADCAST_STICKY  (T.SINK) , 
  //Allows an application to broadcast a WAP PUSH receipt notification 
          BROADCAST_WAP_PUSH  (T.SINK) , 
  //Allows an application to initiate a phone call without going through the Dialer user interface for the user to confirm the call being placed. 
          CALL_PHONE  (T.SINK) , 
  //Allows an application to call any phone number, including emergency numbers, without going through the Dialer user interface for the user to confirm the call being placed. 
          CALL_PRIVILEGED  (T.SINK) , 
  //Required to be able to access the camera device. 
          CAMERA  (T.BOTH) , 
  //Allows an application to change whether an application component (other than its own) is enabled or not. 
          CHANGE_COMPONENT_ENABLED_STATE  (T.SINK) , 
  //Allows an application to modify the current configuration, such as locale. 
          CHANGE_CONFIGURATION  (T.SINK) , 
  //Allows applications to change network connectivity state 
          CHANGE_NETWORK_STATE  (T.SINK) , 
  //Allows applications to enter Wi-Fi Multicast mode 
          CHANGE_WIFI_MULTICAST_STATE  (T.SINK) , 
  //Allows applications to change Wi-Fi connectivity state 
          CHANGE_WIFI_STATE  (T.SINK) , 
  //Allows an application to clear the caches of all installed applications on the device. 
          CLEAR_APP_CACHE  (T.SINK) , 
  //Allows an application to clear user data 
          CLEAR_APP_USER_DATA  (T.SINK) , 
  //Allows enabling/disabling location update notifications from the radio. 
          CONTROL_LOCATION_UPDATES  (T.SINK) , 
  //Allows an application to delete cache files. 
          DELETE_CACHE_FILES  (T.SINK) , 
  //Allows an application to delete packages. 
          DELETE_PACKAGES  (T.SINK) , 
  //Allows low-level access to power management 
          DEVICE_POWER  (T.SOURCE) , 
  //Allows applications to RW to diagnostic resources. 
          DIAGNOSTIC  (T.BOTH) , 
  //Allows applications to disable the keyguard 
          DISABLE_KEYGUARD  (T.SINK) , 
  //Allows an application to retrieve state dump information from system services. 
          DUMP  (T.SOURCE) , 
  //Allows an application to expand or collapse the status bar. 
          EXPAND_STATUS_BAR  (T.SINK) , 
  //Run as a manufacturer test application, running as the root user. 
          FACTORY_TEST  (T.NONE) , 
  //Allows access to the flashlight 
          FLASHLIGHT  (T.SINK) , 
  //Allows an application to force a BACK operation on whatever is the top activity. 
          FORCE_BACK  (T.SINK) , 
  //Allows access to the list of accounts in the Accounts Service 
          GET_ACCOUNTS  (T.SOURCE) , 
  //Allows an application to find out the space used by any package. 
          GET_PACKAGE_SIZE  (T.SOURCE) , 
  //Allows an application to get information about the currently or recently running tasks. 
          GET_TASKS  (T.SOURCE) , 
  //This permission can be used on content providers to allow the global search system to access their data. 
          GLOBAL_SEARCH  (T.BOTH) , 
  //Allows access to hardware peripherals. 
          HARDWARE_TEST  (T.BOTH) , 
  //Allows an application to inject user events (keys, touch, trackball) into the event stream and deliver them to ANY window. 
          INJECT_EVENTS  (T.SINK) , 
  //Allows an application to install a location provider into the Location Manager 
          INSTALL_LOCATION_PROVIDER  (T.SINK) , 
  //Allows an application to install packages. 
          INSTALL_PACKAGES  (T.SINK) , 
  //Allows an application to open windows that are for use by parts of the system user interface. 
          INTERNAL_SYSTEM_WINDOW  (T.SINK) , 
  //Allows applications to open network sockets. 
          INTERNET  (T.SINK) , 
  //Allows an application to call killBackgroundProcesses(String). 
          KILL_BACKGROUND_PROCESSES  (T.SINK) , 
  //Allows an application to manage the list of accounts in the AccountManager 
          MANAGE_ACCOUNTS  (T.BOTH) , 
  //Allows an application to manage (create, destroy, Z-order) application tokens in the window manager. 
          MANAGE_APP_TOKENS  (T.SINK) , 
  // 
          MASTER_CLEAR  (T.NONE) , 
  //Allows an application to modify global audio settings 
          MODIFY_AUDIO_SETTINGS  (T.SINK) , 
  //Allows modification of the telephony state - power on, mmi, etc. 
          MODIFY_PHONE_STATE  (T.SINK) , 
  //Allows formatting file systems for removable storage. 
          MOUNT_FORMAT_FILESYSTEMS  (T.BOTH) , 
  //Allows mounting and unmounting file systems for removable storage. 
          MOUNT_UNMOUNT_FILESYSTEMS  (T.BOTH) , 
  //Allows applications to perform I/O operations over NFC 
          NFC  (T.BOTH) , 
  //This constant was deprecated in API level 9. This functionality will be removed in the future; please do not use. 
          //Allow an application to make its activities persistent. 
          PERSISTENT_ACTIVITY  (T.SINK) , 
  //Allows an application to monitor, modify, or abort outgoing calls. 
          PROCESS_OUTGOING_CALLS  (T.BOTH) , 
  //Allows an application to read the user's calendar data. 
          READ_CALENDAR  (T.SOURCE) , 
  //Allows an application to read the user's call log. 
          READ_CALL_LOG  (T.SOURCE) , 
  //Allows an application to read the user's contacts data. 
          READ_CONTACTS  (T.SOURCE) , 
  //Allows an application to read from external storage. 
          READ_EXTERNAL_STORAGE  (T.SOURCE) , 
  //Allows an application to take screen shots and more generally get access to the frame buffer data 
          READ_FRAME_BUFFER  (T.SOURCE) , 
  //Allows an application to read (but not write) the user's browsing history and bookmarks. 
          READ_HISTORY_BOOKMARKS  (T.SOURCE) , 
  //This constant was deprecated in API level 16. The API that used this permission has been removed. 
          READ_INPUT_STATE  (T.SOURCE) , 
  //Allows an application to read the low-level system log files. 
          READ_LOGS  (T.SOURCE) , 
  //Allows read only access to phone state. 
          READ_PHONE_STATE  (T.SOURCE) , 
  //Allows an application to read the user's personal profile data. 
          READ_PROFILE  (T.SOURCE) , 
  //Allows an application to read SMS messages. 
          READ_SMS  (T.SOURCE) , 
  //Allows an application to read from the user's social stream. 
          READ_SOCIAL_STREAM  (T.SOURCE) , 
  //Allows applications to read the sync settings 
          READ_SYNC_SETTINGS  (T.SOURCE) , 
  //Allows applications to read the sync stats 
          READ_SYNC_STATS  (T.SOURCE) , 
  //Allows an application to read the user dictionary. 
          READ_USER_DICTIONARY  (T.SOURCE) , 
  //Required to be able to reboot the device. 
          REBOOT  (T.SINK) , 
  //Allows an application to receive the ACTION_BOOT_COMPLETED that is broadcast after the system finishes booting. 
          RECEIVE_BOOT_COMPLETED  (T.SOURCE) , 
  //Allows an application to monitor incoming MMS messages, to record or perform processing on them. 
          RECEIVE_MMS  (T.SOURCE) , 
  //Allows an application to monitor incoming SMS messages, to record or perform processing on them. 
          RECEIVE_SMS  (T.SOURCE) , 
  //Allows an application to monitor incoming WAP push messages. 
          RECEIVE_WAP_PUSH  (T.SOURCE) , 
  //Allows an application to record audio 
          RECORD_AUDIO  (T.BOTH) , 
  //Allows an application to change the Z-order of tasks 
          REORDER_TASKS  (T.SINK) , 
  //This constant was deprecated in API level 8. The restartPackage(String) API is no longer supported. 
          RESTART_PACKAGES  (T.SINK) , 
  //Allows an application to send SMS messages. 
          SEND_SMS  (T.SINK) , 
  //Allows an application to watch and control how activities are started globally in the system. 
          SET_ACTIVITY_WATCHER  (T.SINK) , 
  //Allows an application to broadcast an Intent to set an alarm for the user. 
          SET_ALARM  (T.SINK) , 
  //Allows an application to control whether activities are immediately finished when put in the background. 
          SET_ALWAYS_FINISH  (T.SINK) , 
  //Modify the global animation scaling factor. 
          SET_ANIMATION_SCALE  (T.SINK) , 
  //Configure an application for debugging. 
          SET_DEBUG_APP  (T.SINK) , 
  //Allows low-level access to setting the orientation (actually rotation) of the screen. 
          SET_ORIENTATION  (T.SINK) , 
  //Allows low-level access to setting the pointer speed. 
          SET_POINTER_SPEED  (T.SINK) , 
  //This constant was deprecated in API level 7. No longer useful, see addPackageToPreferred(String) for details. 
          SET_PREFERRED_APPLICATIONS  (T.SINK) , 
  //Allows an application to set the maximum number of (not needed) application processes that can be running. 
          SET_PROCESS_LIMIT  (T.SINK) , 
  //Allows applications to set the system time 
          SET_TIME  (T.SINK) , 
  //Allows applications to set the system time zone 
          SET_TIME_ZONE  (T.SINK) , 
  //Allows applications to set the wallpaper 
          SET_WALLPAPER  (T.SINK) , 
  //Allows applications to set the wallpaper hints 
          SET_WALLPAPER_HINTS  (T.SINK) , 
  //Allow an application to request that a signal be sent to all persistent processes 
          SIGNAL_PERSISTENT_PROCESSES  (T.SINK) , 
  //Allows an application to open, close, or disable the status bar and its icons. 
          STATUS_BAR  (T.SINK) , 
  //Allows an application to allow access the subscribed feeds ContentProvider. 
          SUBSCRIBED_FEEDS_READ  (T.SOURCE) , 
  // 
          SUBSCRIBED_FEEDS_WRITE  (T.SINK) , 
  //Allows an application to open windows using the type TYPE_SYSTEM_ALERT, shown on top of all other applications. 
          SYSTEM_ALERT_WINDOW  (T.SINK) , 
  //Allows an application to update device statistics. 
          UPDATE_DEVICE_STATS  (T.SINK) , 
  //Allows an application to request authtokens from the AccountManager 
          USE_CREDENTIALS  (T.SOURCE) , 
  //Allows an application to use SIP service 
          USE_SIP  (T.BOTH) , 
  //Allows access to the vibrator 
          VIBRATE  (T.SINK) , 
  //Allows using PowerManager WakeLocks to keep processor from sleeping or screen from dimming 
          WAKE_LOCK  (T.BOTH) , 
  //Allows applications to write the apn settings 
          WRITE_APN_SETTINGS  (T.SINK) , 
  //Allows an application to write (but not read) the user's calendar data. 
          WRITE_CALENDAR  (T.SINK) , 
  //Allows an application to write (but not read) the user's contacts data. 
          WRITE_CALL_LOG  (T.SINK) , 
  //Allows an application to write (but not read) the user's contacts data. 
          WRITE_CONTACTS  (T.SINK) , 
  //Allows an application to write to external storage. 
          WRITE_EXTERNAL_STORAGE  (T.SINK) , 
  //Allows an application to modify the Google service map. 
          WRITE_GSERVICES  (T.SINK) , 
  //Allows an application to write (but not read) the user's browsing history and bookmarks. 
          WRITE_HISTORY_BOOKMARKS  (T.SINK) , 
  //Allows an application to write (but not read) the user's personal profile data. 
          WRITE_PROFILE  (T.SINK) , 
  //Allows an application to read or write the secure system settings. 
          WRITE_SECURE_SETTINGS  (T.SINK) , 
  //Allows an application to read or write the system settings. 
          WRITE_SETTINGS  (T.SINK) , 
  //Allows an application to write SMS messages. 
          WRITE_SMS  (T.SINK) , 
  //Allows an application to write (but not read) the user's social stream data. 
          WRITE_SOCIAL_STREAM  (T.SINK) , 
  //Allows applications to write the sync settings 
          WRITE_SYNC_SETTINGS  (T.SINK) , 
          WRITE_TIME(T.SINK),
  //Allows an application to write to the user dictionary. 
          WRITE_USER_DICTIONARY  (T.SINK) , 
 

    
    /**
     * Old Source or Sink 
     * These are the names of previous sinks and sources.  They are the names
     * of permissions that have been truncated 
     */
         /*
          NETWORK (T.BOTH), now INTERNET
          TIME (T.SOURCE), now READ_TIME or SET_TIME
          SERIAL_NUMBER (T.SOURCE), now READ_PHONE_STATE
          IMEI (T.SOURCE), now  READ_PHONE_STATE
          MICROPHONE (T.SOURCE), now RECORD_AUDIO 
          APP_TOKENS (T.UNKNOWN), now MANAGE_APP_TOKENS
          AUDIO_SETTINGS (T.UNKNOWN), now MODIFY_AUDIO_SETTINGS
          AUDIO (T.UNKNOWN), now RECORD_AUDIO
          BOOT_COMPLETED (T.UNKNOWN), now RECEIVE_BOOT_COMPLETED
          CALENDAR (T.UNKNOWN), now WRITE_CALENDAR or READ_CALENDAR
          CALL_LOG (T.UNKNOWN), now READ_CALL_LOG or WRITE_CALL_LOG
          CHECKIN_PROPERTIES (T.UNKNOWN), now ACCESS_CHECKIN_PROPERTIES
          COARSE_LOCATION (T.UNKNOWN), now ACCESS_COARSE_LOCATION
          CONTACTS (T.UNKNOWN), now READ_CONTACTS or WRITE_CONTACTS
          CREDENTIALS (T.UNKNOWN), now USE_CREDENTIALS
          EXTERNAL_STORAGE (T.UNKNOWN), now READ_EXTERNAL_STORAGE or WRITE_EXTERNAL_STORAGE
          FINE_LOCATION (T.UNKNOWN), now ACCESS_FINE_LOCATION
          FRAME_BUFFER (T.UNKNOWN), now READ_FRAME_BUFFER
          HISTORY_BOOKMARKS (T.UNKNOWN), now READ_HISTORY_BOOKMARKS
          INPUT_STATE (T.UNKNOWN), now READ_INPUT_STATE
          LOCATION_EXTRA_COMMANDS (T.UNKNOWN), now ACCESS_LOCATION_EXTRA_COMMANDS
          LOCATION (T.UNKNOWN), now ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION
         
          MMS (T.UNKNOWN), now RECEIVE_MMS
          MOCK_LOCATION (T.UNKNOWN), now ACCESS_MOCK_LOCATION
          NETWORK_STATE (T.UNKNOWN), now ACCESS_NETWORK_STATE or CHANGE_NETWORK_STATE
          NETWORK (T.UNKNOWN), now INTERNET
          OUTGOING_CALLS (T.UNKNOWN), now PROCESS_OUTGOING_CALLS
          PACKAGE_SIZE (T.UNKNOWN), now GET_PACKAGE_SIZE
          PACKAGES (T.UNKNOWN), now DELETE_PACKAGES or INSTALL_PACKAGES or RESTART_PACKAGES
         ,
          PHONE_STATE (T.UNKNOWN), now MODIFY_PHONE_STATE or READ_PHONE_STATE
          PROFILE (T.UNKNOWN), now READ_PROFILE or WRITE_PROFILE
          
          SIP (T.UNKNOWN), now USE_SIP
          SMS (T.UNKNOWN), now BROADCAST_SMS or READ_SMS or WRITE_SMS or RECEIVE_SMS or SEND_SMS
          SOCIAL_STREAM (T.UNKNOWN), now READ_SOCIAL_STREAM or WRITE_SOCIAL_STREAM
          SUBSCRIBED_FEEDS (T.UNKNOWN), now READ_SUBSCRIBED_FEEDS or WRITE_SUBSCRIBED_FEEDS
          SURFACE_FLINGER (T.UNKNOWN), now ACCESS_SURFACE_FLINGER
          SYNC_SETTINGS (T.UNKNOWN), now READ_SYNC_SETTINGS or WRITE_SYNC_SETTINGS
          SYNC_STATS (T.UNKNOWN), now READ_SYNC_STATS
          TASKS (T.UNKNOWN), now GET_TASK or REORDER_TASKS
          USER_DICTIONARY (T.UNKNOWN), now READ_USER_DICTIONARY or WRITE_USER_DICTIONARY
          WAP_PUSH (T.UNKNOWN), now BROADCAST_WAP_PUSH or RECEIVE_WAP_PUSH
          WIFI_STATE (T.UNKNOWN), now CHANGE_WIFI_STATE or ACCESS_WIFI_STATE
 
          ACTIVITY_WATCHER (T.UNKNOWN), now SET_ACTIVITY_WATCHER
          ALARM (T.UNKNOWN), now SET_ALARM
          ALWAYS_FINISH (T.UNKNOWN), now SET_ALWAYS_FINISH
          ANIMATION_SCALE (T.UNKNOWN), now SET_ANIMATION_SCALE
          APN_SETTINGS (T.UNKNOWN), now WRITE_APN_SETTINGS
          CACHE_FILES (T.UNKNOWN), now DELETE_CACHE_FILES
          COMPONENT_ENABLED_STATE (T.UNKNOWN), now CHANGE_COMPONENT_ENABLED_STATE
          CONFIGURATION (T.UNKNOWN), now CHANGE_CONFIGURATION
          DEBUG_APP (T.UNKNOWN), now SET_DEBUG_APP
          GSERVICES (T.UNKNOWN), now WRITE_GSERVICES
          LOCATION_UPDATES (T.UNKNOWN), now CONTROL_LOCATION_UPDATES
          ORIENTATION (T.UNKNOWN), now SET_ORIENATION
          POINTER_SPEED (T.UNKNOWN), now SET_POINTER_SPEED
          PREFERRED_APPLICATIONS (T.UNKNOWN), now SET_PREFERRED_APPLICATIONS
          PROCESS_LIMIT (T.UNKNOWN), now SET_PROCESS_LIMIT
          SECURE_SETTINGS (T.UNKNOWN), now WRITE_SECURE_SETTINGS
          SETTINGS (T.UNKNOWN), now WRITE_SETTINGS
          TIME_ZONE (T.UNKNOWN), now SET_TIME_ZONE
          VOICEMAIL (T.UNKNOWN), now ADD_VOICEMAIL
          WALLPAPER_HINTS (T.UNKNOWN), now WALLPAPER_HINTS
          WALLPAPER (T.UNKNOWN), now BIND_WALLPAPER or SET_WALLPAPER
          WIFI_MULTICAST_STATE (T.UNKNOWN), now CHANGE_WIFI_MULTICAST_STATE
          */
    
;
    private final T sourceOrSink;
    
	 FlowPermission(T sourceOrSink) {
		this. sourceOrSink= sourceOrSink;
	}
	 /**
	  *  enum used to indicate if a permission is a source, sink, both, neither.
	  *
	  */
	 enum T {
	 	/**
	 	 * The permission is for sure a source
	 	 * further investigation might show that is also as sink
	 	 */
	 	 SOURCE, 
	 	
	 	/**
	 	 * The permission is for sure a sink
	 	 * further investigation might show that is also as source
	 	 */
	 	 SINK,
	     
	 	 /**
	 	  * The permission is both a source and a sink
	 	  * TODO:refactor to SOURCE_SINK for clarity 
	 	  */
	 	 BOTH, 
	 	 
	 	 
	 	 /**
	 	  * The permission is neither a source nor a sink
	 	  * If a permission is of this type, it should be removed from the enum,
	 	  * but we might want to keep all of the Android Permissions that fall into 
	 	  * this type for completeness 
	 	  */
	 	 NONE;
	 			 
	 	 }
}




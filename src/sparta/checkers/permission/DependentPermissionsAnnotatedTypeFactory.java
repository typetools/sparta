package sparta.checkers.permission;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.Bottom;
import org.checkerframework.framework.qual.DefaultLocation;
import org.checkerframework.framework.type.*;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;

import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.Pair;

import java.util.HashMap;
import java.util.LinkedList;

import javax.lang.model.element.AnnotationMirror;

import sparta.checkers.permission.qual.DependentPermissions;
import sparta.checkers.permission.qual.DependentPermissionsTop;
import sparta.checkers.permission.qual.DependentPermissionsUnqualified;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;

public class DependentPermissionsAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
    protected final AnnotationMirror DP, BOTTOM;

    public static final HashMap<String, String> intentConstTable = new HashMap<String, String>();
    public static final LinkedList<Pair<String, String>> contentURIPatternList = new LinkedList<Pair<String, String>>();
    public static final LinkedList<Pair<String, String>> contentURIConstList = new LinkedList<Pair<String, String>>();

    static {

            // intent constant tables
            // sed command used to convert intent constant strings from pscout
            // format
            // sed -e "s/ . / /" | sed -e "s/ .$/\")\;/" | sed -e "s/ /\",\"/" |
            // sed -e "s/^/intentConstTable.put(\"/"
            intentConstTable.put("android.app.action.DEVICE_ADMIN_ENABLED",
                    "android.permission.BIND_DEVICE_ADMIN");
            intentConstTable.put("android.app.cts.activity.SERVICE_LOCAL_DENIED",
                    "android.app.cts.permission.TEST_DENIED");
            intentConstTable.put("android.app.cts.activity.SERVICE_LOCAL_GRANTED",
                    "android.app.cts.permission.TEST_GRANTED");
            intentConstTable.put("android.bluetooth.adapter.action.REQUEST_DISCOVERABLE",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.adapter.action.REQUEST_ENABLE",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.intent.action.ACTION_REQUEST_SHUTDOWN",
                    "android.permission.SHUTDOWN");
            intentConstTable.put("android.intent.action.CALL", "android.permission.CALL_PHONE");
            intentConstTable.put("android.intent.action.CALL_EMERGENCY",
                    "android.permission.CALL_PRIVILEGED");
            intentConstTable.put("android.intent.action.CALL_PRIVILEGED",
                    "android.permission.CALL_PRIVILEGED");
            intentConstTable.put("android.intent.action.MASTER_CLEAR",
                    "android.permission.MASTER_CLEAR");
            intentConstTable.put("android.intent.action.PRELOAD",
                    "com.android.browser.permission.PRELOAD");
            intentConstTable.put("android.intent.action.REBOOT", "android.permission.SHUTDOWN");
            intentConstTable.put("android.intent.action.SET_ALARM",
                    "com.android.alarm.permission.SET_ALARM");
            intentConstTable.put("android.provider.Telephony.SMS_CB_RECEIVED",
                    "android.permission.BROADCAST_SMS");
            intentConstTable.put("android.provider.Telephony.SMS_EMERGENCY_CB_RECEIVED",
                    "android.permission.BROADCAST_SMS");
            intentConstTable.put("android.provider.Telephony.SMS_RECEIVED",
                    "android.permission.BROADCAST_SMS");
            intentConstTable.put("android.provider.Telephony.WAP_PUSH_RECEIVED",
                    "android.permission.BROADCAST_WAP_PUSH");
            intentConstTable.put("android.service.textservice.SpellCheckerService",
                    "android.permission.BIND_TEXT_SERVICE");
            intentConstTable.put("android.service.wallpaper.WallpaperService",
                    "android.permission.BIND_WALLPAPER");
            intentConstTable
                    .put("android.view.InputMethod", "android.permission.BIND_INPUT_METHOD");
            intentConstTable.put("com.android.email.ACCOUNT_INTENT",
                    "com.android.email.permission.ACCESS_PROVIDER");
            intentConstTable.put("com.android.email.EXCHANGE_INTENT",
                    "com.android.email.permission.ACCESS_PROVIDER");
            intentConstTable.put("com.android.email.POLICY_INTENT",
                    "com.android.email.permission.ACCESS_PROVIDER");
            intentConstTable.put(
                    "com.android.frameworks.coretests.activity.BROADCAST_LOCAL_DENIED",
                    "com.android.frameworks.coretests.permission.TEST_DENIED");
            intentConstTable.put(
                    "com.android.frameworks.coretests.activity.BROADCAST_LOCAL_GRANTED",
                    "com.android.frameworks.coretests.permission.TEST_GRANTED");
            intentConstTable.put(
                    "com.android.frameworks.coretests.activity.BROADCAST_REMOTE_DENIED",
                    "com.android.frameworks.coretests.permission.TEST_DENIED");
            intentConstTable.put(
                    "com.android.frameworks.coretests.activity.BROADCAST_REMOTE_GRANTED",
                    "com.android.frameworks.coretests.permission.TEST_GRANTED");
            intentConstTable.put("com.android.frameworks.coretests.activity.SERVICE_LOCAL_DENIED",
                    "com.android.frameworks.coretests.permission.TEST_DENIED");
            intentConstTable.put("com.android.frameworks.coretests.activity.SERVICE_LOCAL_GRANTED",
                    "com.android.frameworks.coretests.permission.TEST_GRANTED");
            intentConstTable.put("com.android.internal.telephony.IWapPushManager",
                    "com.android.smspush.WAPPUSH_MANAGER_BIND");
            intentConstTable.put("com.android.launcher.action.INSTALL_SHORTCUT",
                    "com.android.launcher.permission.INSTALL_SHORTCUT");
            intentConstTable.put("com.android.launcher.action.UNINSTALL_SHORTCUT",
                    "com.android.launcher.permission.UNINSTALL_SHORTCUT");
            intentConstTable.put("com.android.mms.intent.action.SENDTO_NO_CONFIRMATION",
                    "android.permission.SEND_SMS_NO_CONFIRMATION");
            intentConstTable.put("com.android.phone.PERFORM_CDMA_PROVISIONING",
                    "android.permission.PERFORM_CDMA_PROVISIONING");
            intentConstTable.put("com.google.android.c2dm.intent.RECEIVE",
                    "android.permission.MASTER_CLEAR");
            intentConstTable.put("android.intent.action.DROPBOX_ENTRY_ADDED",
                    "android.permission.READ_LOGS");
            intentConstTable.put("android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.bluetooth.device.action.CONNECTION_ACCESS_REPLY",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.bleutooth.device.action.UUID",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.bluetooth.device.action.PAIRING_REQUEST",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.bluetooth.device.action.PAIRING_REQUEST",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.bluetooth.device.action.PAIRING_REQUEST",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.bluetooth.device.action.PAIRING_REQUEST",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.bluetooth.device.action.PAIRING_REQUEST",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.bluetooth.device.action.PAIRING_REQUEST",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.bluetooth.device.action.PAIRING_REQUEST",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.bluetooth.device.action.PAIRING_CANCEL",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.bluetooth.device.action.CONNECTION_ACCESS_REPLY",
                    "android.permission.BLUETOOTH_ADMIN");
            intentConstTable.put("android.intent.action.BOOT_COMPLETED",
                    "android.permission.RECEIVE_BOOT_COMPLETED");
            intentConstTable.put("android.intent.action.PHONE_STATE",
                    "android.permission.READ_PHONE_STATE");
            intentConstTable.put("android.intent.action.NEW_OUTGOING_CALL",
                    "android.permission.PROCESS_OUTGOING_CALLS");
            intentConstTable.put("android.provider.Telephony.SMS_REJECTED",
                    "android.permission.RECEIVE_SMS");
            intentConstTable.put("android.provider.Telephony.SIM_FULL",
                    "android.permission.RECEIVE_SMS");
            intentConstTable.put("android.provider.Telephony.SMS_RECEIVED",
                    "android.permission.RECEIVE_SMS");
            intentConstTable.put("android.intent.action.DATA_SMS_RECEIVED",
                    "android.permission.RECEIVE_SMS");
            intentConstTable.put("android.provider.Telephony.SMS_CB_RECEIVED",
                    "android.permission.RECEIVE_SMS");
            intentConstTable.put("android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.pan.profile.action.CONNECTION_STATE_CHANGED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.adapter.action.SCAN_MODE_CHANGED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.device.action.BOND_STATE_CHANGED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.device.action.FOUND",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.device.action.DISAPPEARED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.adapter.action.SCAN_MODE_CHANGED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.adapter.action.DISCOVERY_FINISHED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.device.action.NAME_CHANGED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.device.action.CLASS_CHANGED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.device.action.ACL_DISCONNECTED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.adapter.action.STATE_CHANGED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.pbap.intent.action.PBAP_STATE_CHANGED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put(
                    "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED",
                    "android.permission.BLUETOOTH");
            intentConstTable.put("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED",
                    "android.permission.BLUETOOTH");

            // contentURIPatternList
            // grep "pathPrefix" | sed "s/ . /\.\*\",\"/" | sed
            // "s/ pathPrefix/\"));/" | sed "s/ /\.\*\",\"/" | sed
            // "s/^/contentURIPatternList.add(Pair.of(\"\^/"

            contentURIPatternList.add(Pair.of(
                    "^content://com.android.contacts/search_suggest_query.*",
                    "android.permission.GLOBAL_SEARCH"));
            contentURIPatternList.add(Pair.of(
                    "^content://com.android.contacts/search_suggest_shortcut.*",
                    "android.permission.GLOBAL_SEARCH"));
            contentURIPatternList.add(Pair.of(
                    "^content://com.android.mms.SuggestionsProvider/search_suggest_query.*",
                    "android.permission.GLOBAL_SEARCH"));
            contentURIPatternList.add(Pair.of(
                    "^content://com.android.mms.SuggestionsProvider/search_suggest_shortcut.*",
                    "android.permission.GLOBAL_SEARCH"));
            contentURIPatternList.add(Pair.of(
                    "^content://contacts/search_suggest_query.*",
                    "android.permission.GLOBAL_SEARCH"));
            contentURIPatternList.add(Pair.of(
                    "^content://contacts/search_suggest_shortcut.*",
                    "android.permission.GLOBAL_SEARCH"));
            contentURIPatternList.add(Pair.of(
                    "^content://ctspermissionwithsignaturepath/foo.*",
                    "com.android.cts.permissionWithSignature"));
            contentURIPatternList.add(Pair.of(
                    "^content://ctspermissionwithsignaturepath/yes.*",
                    "com.android.cts.permissionWithSignature"));
            contentURIPatternList.add(Pair.of(
                    "^content://downloads/all_downloads/.*", "grant-uri-permission"));
            contentURIPatternList.add(Pair.of(
                    "^content://downloads/all_downloads.*",
                    "android.permission.ACCESS_ALL_DOWNLOADS"));
            contentURIPatternList.add(Pair.of(
                    "^content://downloads/all_downloads.*",
                    "android.permission.ACCESS_ALL_DOWNLOADS"));
            contentURIPatternList.add(Pair.of("^content://downloads/download.*",
                    "android.permission.INTERNET"));
            contentURIPatternList.add(Pair.of("^content://downloads/download.*",
                    "android.permission.INTERNET"));
            contentURIPatternList.add(Pair.of(
                    "^content://downloads/my_downloads.*", "android.permission.INTERNET"));
            contentURIPatternList.add(Pair.of(
                    "^content://downloads/my_downloads.*", "android.permission.INTERNET"));
            contentURIPatternList.add(Pair.of("^content://mms/drm/.*",
                    "grant-uri-permission"));
            contentURIPatternList.add(Pair.of("^content://mms/part/.*",
                    "grant-uri-permission"));

            // grep "Pattern" | sed "s/ pathPattern/\"));/" | sed "s/ . / /" |
            // sed "s/ /\",\"/" | sed "s/^/contentURIPatternList.add(new
            // Pair<String, String>(\"\^/"
            contentURIPatternList.add(Pair.of(
                    "^content://com.android.contacts/contacts/.*/photo",
                    "android.permission.GLOBAL_SEARCH"));
            contentURIPatternList.add(Pair.of("^content://com.android.contacts.*",
                    "grant-uri-permission"));
            contentURIPatternList.add(Pair.of(
                    "^content://com.google.provider.NotePad.*", "grant-uri-permission"));
            contentURIPatternList.add(Pair.of(
                    "^content://contacts/contacts/.*/photo", "android.permission.GLOBAL_SEARCH"));
            contentURIPatternList.add(Pair.of("^content://contacts.*",
                    "grant-uri-permission"));
            contentURIPatternList.add(Pair.of(
                    "^content://ctspermissionwithsignaturegranting/foo.*", "grant-uri-permission"));
            contentURIPatternList.add(Pair.of(
                    "^content://ctspermissionwithsignaturegranting/yes.*", "grant-uri-permission"));
            contentURIPatternList.add(Pair.of(
                    "^content://ctspermissionwithsignaturepath.*", "grant-uri-permission"));
            contentURIPatternList.add(Pair.of(
                    "^content://ctsprivateprovidergranting/foo.*", "grant-uri-permission"));
            contentURIPatternList.add(Pair.of(
                    "^content://ctsprivateprovidergranting/yes.*", "grant-uri-permission"));

            // contentURIConstList
            // grep -v "Pattern" | grep -v "Prefix" | sed "s/ path//" | sed
            // "s/$/\"));/" | sed "s/ . / /" | sed "s/ /\",\"/" | sed
            // "s/^/contentURIConstList.add(Pair.of(\"/"
            contentURIConstList.add(Pair.of(
                    "content://browser/bookmarks/search_suggest_query",
                    "android.permission.GLOBAL_SEARCH"));
            contentURIConstList.add(Pair.of("content://browser",
                    "com.android.browser.permission.READ_HISTORY_BOOKMARKS"));
            contentURIConstList.add(Pair.of("content://browser",
                    "com.android.browser.permission.WRITE_HISTORY_BOOKMARKS"));
            contentURIConstList.add(Pair.of("content://call_log",
                    "android.permission.READ_CONTACTS"));
            contentURIConstList.add(Pair.of("content://call_log",
                    "android.permission.WRITE_CONTACTS"));
            contentURIConstList.add(Pair.of(
                    "content://com.android.bluetooth.opp/btopp",
                    "android.permission.ACCESS_BLUETOOTH_SHARE"));
            contentURIConstList.add(Pair.of(
                    "content://com.android.bluetooth.opp/btopp",
                    "android.permission.ACCESS_BLUETOOTH_SHARE"));
            contentURIConstList.add(Pair.of(
                    "content://com.android.browser/bookmarks/search_suggest_query",
                    "android.permission.GLOBAL_SEARCH"));
            contentURIConstList.add(Pair.of("content://com.android.browser.home",
                    "com.android.browser.permission.READ_HISTORY_BOOKMARKS"));
            contentURIConstList.add(Pair.of("content://com.android.browser",
                    "com.android.browser.permission.READ_HISTORY_BOOKMARKS"));
            contentURIConstList.add(Pair.of("content://com.android.browser",
                    "com.android.browser.permission.WRITE_HISTORY_BOOKMARKS"));
            contentURIConstList.add(Pair.of("content://com.android.calendar",
                    "android.permission.READ_CALENDAR"));
            contentURIConstList.add(Pair.of("content://com.android.calendar",
                    "android.permission.WRITE_CALENDAR"));
            contentURIConstList.add(Pair.of("content://com.android.contacts",
                    "android.permission.READ_CONTACTS"));
            contentURIConstList.add(Pair.of("content://com.android.contacts",
                    "android.permission.WRITE_CONTACTS"));
            contentURIConstList.add(Pair.of(
                    "content://com.android.email.attachmentprovider",
                    "com.android.email.permission.READ_ATTACHMENT"));
            contentURIConstList.add(Pair.of(
                    "content://com.android.email.notifier",
                    "com.android.email.permission.ACCESS_PROVIDER"));
            contentURIConstList.add(Pair.of(
                    "content://com.android.email.notifier",
                    "com.android.email.permission.ACCESS_PROVIDER"));
            contentURIConstList.add(Pair.of(
                    "content://com.android.email.provider",
                    "com.android.email.permission.ACCESS_PROVIDER"));
            contentURIConstList.add(Pair.of(
                    "content://com.android.email.provider",
                    "com.android.email.permission.ACCESS_PROVIDER"));
            contentURIConstList.add(Pair.of(
                    "content://com.android.exchange.directory.provider",
                    "android.permission.READ_CONTACTS"));
            contentURIConstList.add(Pair.of(
                    "content://com.android.launcher2.settings",
                    "com.android.launcher.permission.READ_SETTINGS"));
            contentURIConstList.add(Pair.of(
                    "content://com.android.launcher2.settings",
                    "com.android.launcher.permission.WRITE_SETTINGS"));
            contentURIConstList
                    .add(Pair.of("content://com.android.mms.SuggestionsProvider",
                            "android.permission.READ_SMS"));
            contentURIConstList.add(Pair.of("content://com.android.social",
                    "android.permission.READ_CONTACTS"));
            contentURIConstList.add(Pair.of("content://com.android.social",
                    "android.permission.WRITE_CONTACTS"));
            contentURIConstList.add(Pair.of("content://com.android.voicemail",
                    "com.android.voicemail.permission.ADD_VOICEMAIL"));
            contentURIConstList.add(Pair.of("content://com.android.voicemail",
                    "com.android.voicemail.permission.ADD_VOICEMAIL"));
            contentURIConstList.add(Pair.of("content://contacts",
                    "android.permission.READ_CONTACTS"));
            contentURIConstList.add(Pair.of("content://contacts",
                    "android.permission.WRITE_CONTACTS"));
            contentURIConstList.add(Pair.of(
                    "content://ctspermissionwithsignaturegranting",
                    "com.android.cts.permissionWithSignature"));
            contentURIConstList.add(Pair.of(
                    "content://ctspermissionwithsignaturegranting",
                    "com.android.cts.permissionWithSignature"));
            contentURIConstList.add(Pair.of(
                    "content://ctspermissionwithsignaturepath",
                    "com.android.cts.permissionNotUsedWithSignature"));
            contentURIConstList.add(Pair.of(
                    "content://ctspermissionwithsignaturepath",
                    "com.android.cts.permissionNotUsedWithSignature"));
            contentURIConstList.add(Pair.of(
                    "content://ctspermissionwithsignature",
                    "com.android.cts.permissionWithSignature"));
            contentURIConstList.add(Pair.of(
                    "content://ctspermissionwithsignature",
                    "com.android.cts.permissionWithSignature"));
            contentURIConstList.add(Pair.of("content://icc",
                    "android.permission.READ_CONTACTS"));
            contentURIConstList.add(Pair.of("content://icc",
                    "android.permission.WRITE_CONTACTS"));
            contentURIConstList.add(Pair.of("content://mms",
                    "android.permission.READ_SMS"));
            contentURIConstList.add(Pair.of("content://mms-sms",
                    "android.permission.READ_SMS"));
            contentURIConstList.add(Pair.of("content://mms-sms",
                    "android.permission.WRITE_SMS"));
            contentURIConstList.add(Pair.of("content://mms",
                    "android.permission.WRITE_SMS"));
            contentURIConstList.add(Pair.of("content://settings",
                    "android.permission.WRITE_SETTINGS"));
            contentURIConstList.add(Pair.of("content://sms",
                    "android.permission.READ_SMS"));
            contentURIConstList.add(Pair.of("content://sms",
                    "android.permission.WRITE_SMS"));
            contentURIConstList.add(Pair.of("content://user_dictionary",
                    "android.permission.READ_USER_DICTIONARY"));
            contentURIConstList.add(Pair.of("content://user_dictionary",
                    "android.permission.WRITE_USER_DICTIONARY"));
        }

    public DependentPermissionsAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);

        BOTTOM = AnnotationUtils.fromClass(elements, Bottom.class);
        DP = AnnotationUtils.fromClass(elements, DependentPermissions.class);

        this.postInit();

        // Reuse the framework Bottom annotation and make it the default for the
        // null literal.

        //  typeAnnotator.addTypeName(java.lang.Void.class, checker.BOTTOM);

        defaults.addCheckedCodeDefault(AnnotationUtils.fromClass(elements,
                                                                        DependentPermissionsTop.class),
                                              DefaultLocation.LOCAL_VARIABLE);

        defaults.addCheckedCodeDefault(AnnotationUtils.fromClass(elements,
                                                                        DependentPermissionsUnqualified.class),
                                              DefaultLocation.OTHERWISE);

        // flow.setDebug(System.err);
    }

    @Override
    public ListTreeAnnotator createTreeAnnotator() {
        ImplicitsTreeAnnotator implicits = new ImplicitsTreeAnnotator(this);
        implicits.addTreeKind(Tree.Kind.NULL_LITERAL, BOTTOM);
        return new ListTreeAnnotator(
                new PropagationTreeAnnotator(this),
                implicits,
                new DPTreeAnnotator(this)
        );
    }

    AnnotationMirror createDependentPermAnnotation(String s) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
                DependentPermissions.class.getCanonicalName());
        builder.setValue("value", s);
        return builder.build();
    }

    private class DPTreeAnnotator extends TreeAnnotator {

        public DPTreeAnnotator(DependentPermissionsAnnotatedTypeFactory atypeFactory) {
            super(atypeFactory);
            // TODO Auto-generated constructor stub
        }

        @Override
        public Void visitLiteral(LiteralTree tree, AnnotatedTypeMirror type) {

            if (!type.hasAnnotation(((DependentPermissionsAnnotatedTypeFactory)atypeFactory).DP)) {
                if (tree.getKind() == Tree.Kind.STRING_LITERAL) {
                    String s = (String) tree.getValue();

                    // check the string against known constants
                    // intent actions
                    if (!s.matches("^content://.*")) {
                        if (intentConstTable.containsKey(s)) {
                            type.addAnnotation(createDependentPermAnnotation(intentConstTable
                                    .get(s)));
                        }

                    } else {
                        // iterate through patterns List
                        LinkedList<String> perms = new LinkedList<String>();
                        for (Pair<String, String> p : contentURIPatternList) {
                            if (s.matches(p.first)) {
                                perms.add(p.second);

                            }
                        }

                        for (Pair<String, String> p : contentURIConstList) {
                            if (s.equalsIgnoreCase(p.first)) {
                                perms.add(p.second);

                            }
                        }
                        // add annotations
                        type.addAnnotation(createDependentPermAnnotation(perms.toString()));

                    }

                }
            }
            return super.visitLiteral(tree, type);
        }

    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new DPQualifierHierarchy(factory);
    }

    protected class DPQualifierHierarchy extends GraphQualifierHierarchy {

        /*
         * We use the constructor of GraphQualifierHierarchy that allows us to
         * set a dedicated bottom qualifier.
         */
        public DPQualifierHierarchy(MultiGraphFactory factory) {
            super(factory, BOTTOM);
        }

        @Override
        public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
            if (AnnotationUtils.areSameIgnoringValues(lhs, DP)
                    && AnnotationUtils.areSameIgnoringValues(rhs, DP)) {
                return AnnotationUtils.areSame(lhs, rhs);
            }
            // Ignore annotation values to ensure that annotation is in
            // supertype map.
            if (AnnotationUtils.areSameIgnoringValues(lhs, DP)) {
                lhs = DP;
            }
            if (AnnotationUtils.areSameIgnoringValues(rhs, DP)) {
                rhs = DP;
            }
            return super.isSubtype(rhs, lhs);
        }
    }

}

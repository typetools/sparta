import android.Manifest.permission;

import sparta.checkers.quals.*;

class FirstCall {
    @RequiredPermissions(permission.NFC)
    void doNFC() {}

    void badUse1() {
        //:: error: (all.unsatisfied.permissions)
        doNFC();
    }

    @RequiredPermissions(permission.SEND_SMS)
    void badUse2() {
        //:: error: (unsatisfied.permission)
        doNFC();
    }

    @RequiredPermissions(permission.NFC)
    void goodUse1() {
        doNFC();
    }

    @RequiredPermissions({permission.NFC, permission.SEND_SMS})
    void goodUse2() {
        doNFC();
    }
}
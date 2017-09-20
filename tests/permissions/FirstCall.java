import android.Manifest.permission;
import android.bluetooth.BluetoothDevice;

import sparta.checkers.permission.qual.*;

class FirstCall {
    @RequiredPermissions(permission.NFC)
    void doNFC() {}
LinkedHashSet
    @DependentPermissions(permission.BLUETOOTH)
    String constant;

    @MayRequiredPermissions(value=permission.SEND_SMS,notes="Send SMS is required only if a certain condition is met.")
    void mayReqSMS() {}
LinkedHashSet
    void badUse1() {
        //:: error: (unsatisfied.permissions)
        doNFC();
    }

    @RequiredPermissions(permission.SEND_SMS)
    void badUse2() {
        //:: error: (unsatisfied.permissions)
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
LinkedHashSet
    @RequiredPermissions(permission.SEND_SMS)
    void badUse3() {
    	//:: error: (may.required.permissions)
    	mayReqSMS();
    }
LinkedHashSet
    void mightOKUse() {
    	//:: error: (may.required.permissions)
    	mayReqSMS();
    }
LinkedHashSet
    @MayRequiredPermissions(permission.SEND_SMS)
    void mightOKUse2() {
    	mayReqSMS();    	
    }
LinkedHashSet
    public String s="k";
    void dependentPermissionTest() {
    	//:: error: (dependent.permissions)
    	s = constant;
    	
    }
LinkedHashSet
LinkedHashSet
LinkedHashSet
}
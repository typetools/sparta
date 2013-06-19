import android.Manifest.permission;
import android.bluetooth.BluetoothDevice;

import sparta.checkers.quals.*;

class FirstCall {
    @RequiredPermissions(permission.NFC)
    void doNFC() {}
    
    @DependentPermissions(permission.BLUETOOTH)
    String constant;

    @MayRequiredPermissions(value=permission.SEND_SMS,notes="Send SMS is required only if a certain condition is met.")
    void mayReqSMS() {}
    
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
    
    @RequiredPermissions(permission.SEND_SMS)
    void badUse3() {
    	//:: error: (may.required.permissions)
    	mayReqSMS();
    }
    
    void mightOKUse() {
    	//:: error: (may.required.permissions)
    	mayReqSMS();
    }
    
    @MayRequiredPermissions(permission.SEND_SMS)
    void mightOKUse2() {
    	mayReqSMS();    	
    }
    
    void dependentPermissionTest() {
    	//:: error: (dependent.permissions)
    	String s = constant;
    	
    }
    
    
}
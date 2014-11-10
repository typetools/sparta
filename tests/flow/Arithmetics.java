import sparta.checkers.quals.Source;
import sparta.checkers.quals.FlowPermission;

import sparta.checkers.quals.Sink;
import static sparta.checkers.quals.FlowPermissionString.*;

class Arithmetics {
    @Source({ ACCELEROMETER }) @Sink({ FILESYSTEM }) int accel;
    @Source({ READ_SMS }) @Sink({ WRITE_SMS }) int sms = 2;
    @Source({ ACCELEROMETER, READ_SMS }) @Sink({}) int lub;

    public void checkAccelToFile(
            final @Source({ ACCELEROMETER }) @Sink({ FILESYSTEM }) int accel) {
    }

    public void checkReadSmsToWriteSms(
            final @Source({ READ_SMS }) @Sink({ WRITE_SMS }) int readsms) {
    }

    public void checkLUB(
            final @Source({ READ_SMS, ACCELEROMETER }) @Sink({}) int lub) {
    }

    public void checkTop(final @Source(ANY) @Sink({}) int test) {
    }

    public void checkBottom(final @Source({}) @Sink(ANY) int test) {
    }

    void binaryAritmeticOperateors() {
        checkLUB(accel + sms);
        //:: error: (argument.type.incompatible)
        checkAccelToFile(accel + sms);
        //:: error: (argument.type.incompatible)
        checkReadSmsToWriteSms(accel + sms);

        checkLUB(accel - sms);
        //:: error: (argument.type.incompatible)
        checkAccelToFile(accel - sms);
        //:: error: (argument.type.incompatible)
        checkReadSmsToWriteSms(accel - sms);

        checkLUB(accel / sms);
        //:: error: (argument.type.incompatible)
        checkAccelToFile(accel / sms);
        //:: error: (argument.type.incompatible)
        checkReadSmsToWriteSms(accel / sms);

        checkLUB(accel * sms);
        //:: error: (argument.type.incompatible)
        checkAccelToFile(accel * sms);
        //:: error: (argument.type.incompatible)
        checkReadSmsToWriteSms(accel * sms);

        checkLUB(accel % sms);
        //:: error: (argument.type.incompatible)
        checkAccelToFile(accel % sms);
        //:: error: (argument.type.incompatible)
        checkReadSmsToWriteSms(accel % sms);
    }

    @Source(READ_SMS) @Sink(WRITE_SMS) boolean smsBool = true;

    void unaryOperators() {
        checkAccelToFile(+accel);
        // Make sure that this above call didn't pass
        // because +accel is bottom.
        //:: error: (argument.type.incompatible)
        checkBottom(+accel);

        checkAccelToFile(-accel);
        //:: error: (argument.type.incompatible)
        checkBottom(-accel);

        checkAccelToFile(++accel);
        //:: error: (argument.type.incompatible)
        checkBottom(++accel);

        checkAccelToFile(--accel);
        //:: error: (argument.type.incompatible)
        checkBottom(--accel);

        checkAccelToFile(~accel);
        //:: error: (argument.type.incompatible)
        checkBottom(~accel);

        @Source(READ_SMS) @Sink(WRITE_SMS) boolean smsBoolTest = !smsBool;
        //:: error: (assignment.type.incompatible)
        @Source({}) @Sink(ANY) boolean bot = !smsBool;
    }

    void bitOperators() {
        checkLUB(accel & sms);
        //:: error: (argument.type.incompatible)
        checkAccelToFile(accel & sms);
        //:: error: (argument.type.incompatible)
        checkReadSmsToWriteSms(accel & sms);

        checkLUB(accel | sms);
        //:: error: (argument.type.incompatible)
        checkAccelToFile(accel | sms);
        //:: error: (argument.type.incompatible)
        checkReadSmsToWriteSms(accel | sms);

        checkLUB(accel ^ sms);
        //:: error: (argument.type.incompatible)
        checkAccelToFile(accel ^ sms);
        //:: error: (argument.type.incompatible)
        checkReadSmsToWriteSms(accel ^ sms);

        checkLUB(accel << sms);
        //:: error: (argument.type.incompatible)
        checkAccelToFile(accel << sms);
        //:: error: (argument.type.incompatible)
        checkReadSmsToWriteSms(accel << sms);

        checkLUB(accel >>> sms);
        //:: error: (argument.type.incompatible)
        checkAccelToFile(accel >>> sms);
        //:: error: (argument.type.incompatible)
        checkReadSmsToWriteSms(accel >>> sms);

        checkLUB(accel >> sms);
        //:: error: (argument.type.incompatible)
        checkAccelToFile(accel >> sms);
        //:: error: (argument.type.incompatible)
        checkReadSmsToWriteSms(accel >> sms);
    }
}

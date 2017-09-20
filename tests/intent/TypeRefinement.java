import android.content.Intent;
import sparta.checkers.quals.Extra;
import sparta.checkers.quals.IntentMap;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import static sparta.checkers.quals.FlowPermission.*;

class TypeRefinementTest {
    boolean flag = false;
    @Source("READ_SMS") String sms;
    @Source("READ_TIME") String time;
    @Source({}) @Sink("ANY") String bot;
    @Source("ANY") @Sink({}) String top;
    @Source({}) @Sink({}) String value;
    String getValue(){return "";};

    // : Normal case
    void method() {
        Intent i = new Intent();
        i.putExtra("k1", top);
        i.putExtra("k3", bot);
        // Proposal 1 i: @IntentMap("k1"->t1, k3->t2) @Unique
        normalTest(i);
    }

    void normalTest(@IntentMap({ @Extra(key = "k1", source = ANY),
            @Extra(key = "k3", source = {}) }) Intent i) {
    }

    // A: Side effects
    void methodA() {
        Intent i = new Intent();
        i.putExtra("k1", top);
        // Assume that getValue() is not [declared as] side-effect-free wrt
        // intents
        i.putExtra("k3", getValue());
        // Proposal 1 i: @IntentMap("k1"->top, k3->t) @Unique
        testA(i);
    }
    void testA(@IntentMap({ @Extra(key = "k1", source = ANY),
        @Extra(key = "k3", source = {}) }) Intent i) {
    }
    // B: Aliasing of intents with type @IntentMap({})
    void methodB() {
        Intent a = new Intent();
        Intent b = a;
        //::error: (argument.type.incompatible) ::error: (intent.key.notfound)
        a.putExtra("k", value);
        // a: @IntentMap({}) @MaybeAliased
        // warning: k is not in the @IntentMap of a.
        //::error: (argument.type.incompatible)
        testB(a);
    }
    void testB(@IntentMap(@Extra(key="k", source={})) Intent i) {
    }

    // C: Aliasing of intents with type @IntentMap({k->t})
    void methodC() {
        @IntentMap(@Extra(key = "k", source = INTERNET))
        Intent a = new Intent();

        @IntentMap(@Extra(key = "k", source = INTERNET))
        Intent b = a;
        //::error: (argument.type.incompatible) ::error: (intent.key.notfound)
        a.putExtra("k2", value);
        // warning: "k2" is not in the @IntentMap of a.

    }

    // D: Aliasing of intents, one has a refined type of @IntentMap(k->t)
    void methodD() {
        Intent a = new Intent();
        a.putExtra("k", value);
        Intent b = a;
        // a: @IntentMap({}) @MaybeAliased
    }

    // E: Aliasing of intents, one has a declared type of @IntentMap(k->t)
    void methodE() {
        Intent a = new Intent();
        a.putExtra("k", value);
        @IntentMap(@Extra(key = "k", source = {}, sink = {}))
        Intent b = a;
        // a: @IntentMap({}) @MaybeAliased (assignment is legal)

    }

    // E2: Aliasing of intents, with extra declared type
    void methodE2() {
        @IntentMap(@Extra(key = "k", source = INTERNET))
        Intent a = new Intent();
        a.putExtra("k", value);
        Intent b = a;
        // a: @IntentMap(k->t) @MaybeAliased (assignment is legal)

    }

    // F: Updating refined @IntentMap type
    void methodF() {
        Intent i = new Intent();
        i.putExtra("k1", bot);
        i.putExtra("k1", top);
        // i: @IntentMap("k1"->top) @Unique

    }

    // G: Conditional
    void methodG() {
        Intent a = new Intent();
        if (flag) {
            a.putExtra("k1", top);
        } else {
            a.putExtra("k2", top);
        }
        //:: error: (argument.type.incompatible)
        testG(a);
        // a : @IntentMap({}) @Unique
    }
    void testG(@IntentMap({ @Extra(key = "k1", source = ANY),
        @Extra(key = "k2", source = ANY)}) Intent i) {
    }
    // G2: Conditional with same key used in all branches
    void methodG2() {
        Intent a = new Intent();
        if (flag) {
            a.putExtra("k1", top);
            a.putExtra("k2", bot);
        } else {
            a.putExtra("k1", top);
        }
        // a : @IntentMap({"k1"->top}) @Unique
        testG2(a);
    }
    void testG2(@IntentMap(@Extra(key = "k1", source = ANY)) Intent i) {
    }
LinkedHashSet
    // H: Aliasing in conditional
    void methodH() {
        Intent a = new Intent();
        Intent b;
        if (flag) {
            a.putExtra("k", top);
        } else {
            b = a;
        }
        // a: @IntentMap({}) @MaybeAliased
        //:: error: (argument.type.incompatible)
        testH(a);
    }
    void testH(@IntentMap(@Extra(key = "k", source = ANY)) Intent i) {
    }
}
